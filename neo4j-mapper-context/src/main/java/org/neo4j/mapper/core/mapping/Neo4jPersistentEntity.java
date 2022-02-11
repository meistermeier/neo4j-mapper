/*
 * Copyright 2011-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.mapper.core.mapping;

import org.apiguardian.api.API;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.neo4j.mapper.core.schema.Id;
import org.neo4j.mapper.core.schema.Node;
import org.neo4j.mapper.core.support.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Michael J. Simons
 * @param <T> type of the underlying class
 * @since 6.0
 */
@API(status = API.Status.STABLE, since = "6.0")
public interface Neo4jPersistentEntity<T> extends NodeDescription<T> {

	static Neo4jPersistentEntity<?> of(Class<?> entityClass) {
		return new Neo4jPersistentEntityImpl<>(entityClass);
	}


	class Neo4jPersistentEntityImpl<T> implements Neo4jPersistentEntity<T> {

		private final Class<T> type;
		private final String primaryLabel;
		private final GraphPropertyDescription idProperty;
		private final NodeDescription<?> parentNodeDescription = null;
		private final Collection<GraphPropertyDescription> properties;
		private final Collection<RelationshipDescription> relationships;

		public Neo4jPersistentEntityImpl(Class<T> type) {
			this.type = type;
			this.primaryLabel = computePrimaryLabel(type);
			this.idProperty = findIdProperty(type);
			this.properties = parseProperties(type);
			this.relationships = parseRelationships(properties);
		}

		private List<GraphPropertyDescription> parseProperties(Class<T> type) {
			return Arrays.stream(type.getDeclaredFields())
					.map(GraphPropertyDescription::forField)
					.toList();
		}

		private List<RelationshipDescription> parseRelationships(Collection<GraphPropertyDescription> properties) {
			return properties.stream()
					.filter(GraphPropertyDescription::isRelationship)
					.map(RelationshipDescription::of)
					.toList();
		}

		private GraphPropertyDescription findIdProperty(Class<?> type) {
			List<Field> candidates = Arrays.stream(type.getDeclaredFields())
					.filter(field -> field.isAnnotationPresent(Id.class)).toList();
			if (candidates.size() != 1) {
				throw new IllegalStateException("No or too much id fields found for " + type + " namentlich " + candidates);
			}

			return GraphPropertyDescription.forField(candidates.get(0));
		}

		@Override
		public String getPrimaryLabel() {
			return primaryLabel;
		}

		@Nullable
		static String computePrimaryLabel(Class<?> type) {

			Node nodeAnnotation = type.getAnnotation(Node.class);
			if ((nodeAnnotation == null || hasEmptyLabelInformation(nodeAnnotation))) {
				return type.getSimpleName();
			} else if (StringUtils.hasText(nodeAnnotation.primaryLabel())) {
				return nodeAnnotation.primaryLabel();
			} else {
				return nodeAnnotation.labels().length > 0 ? nodeAnnotation.labels()[0] : nodeAnnotation.value()[0];
			}
		}

		private static boolean hasEmptyLabelInformation(Node nodeAnnotation) {
			return nodeAnnotation.labels().length < 1 && nodeAnnotation.value().length < 1 && !StringUtils.hasText(nodeAnnotation.primaryLabel());
		}

		@Override
		public String getMostAbstractParentLabel(NodeDescription<?> mostAbstractNodeDescription) {
			return null;
		}

		@Override
		public List<String> getAdditionalLabels() {
			var additionalLabels = Stream.concat(computeOwnAdditionalLabels().stream(), computeParentLabels().stream())
					.distinct() // In case the interfaces added a duplicate of the primary label.
					.filter(v -> !getPrimaryLabel().equals(v))
					.collect(Collectors.toList());

			return additionalLabels;
		}

		/**
		 * The additional labels will get computed and returned by following rules:<br>
		 * 1. If there is no {@link Node} annotation, empty {@code String} array.<br>
		 * 2. If there is an annotation but it has no properties set, empty {@code String} array.<br>
		 * 3a. If only {@link Node#labels()} property is set, use the all but the first one as the additional labels.<br>
		 * 3b. If the {@link Node#primaryLabel()} property is set, use the all but the first one as the additional labels.<br>
		 * 4. If the class has any interfaces that are explicitly annotated with {@link Node}, we take all values from them.
		 *
		 * @return computed additional labels of the concrete class
		 */
		@NotNull
		private List<String> computeOwnAdditionalLabels() {
			List<String> result = new ArrayList<>();

			Node nodeAnnotation = this.type.getAnnotation(Node.class);
			if (!(nodeAnnotation == null || hasEmptyLabelInformation(nodeAnnotation))) {
				if (StringUtils.hasText(nodeAnnotation.primaryLabel())) {
					result.addAll(Arrays.asList(nodeAnnotation.labels()));
					result.addAll(Arrays.asList(nodeAnnotation.value()));
				} else {
					if (nodeAnnotation.labels().length > 0) {
						result.addAll(Arrays.asList(Arrays.copyOfRange(nodeAnnotation.labels(), 1, nodeAnnotation.labels().length)));
					} else {
						result.addAll(Arrays.asList(Arrays.copyOfRange(nodeAnnotation.value(), 1, nodeAnnotation.value().length)));
					}
				}
			}

			// Add everything we find on _direct_ interfaces
			// We don't traverse interfaces of interfaces
			for (Class<?> anInterface : this.type.getInterfaces()) {
				nodeAnnotation = anInterface.getAnnotation(Node.class);
				if (nodeAnnotation == null) {
					continue;
				}
				if (hasEmptyLabelInformation(nodeAnnotation)) {
					result.add(anInterface.getSimpleName());
				} else {
					if (StringUtils.hasText(nodeAnnotation.primaryLabel())) {
						result.add(nodeAnnotation.primaryLabel());
					}
					result.addAll(Arrays.asList(nodeAnnotation.labels()));
					result.addAll(Arrays.asList(nodeAnnotation.value()));
				}
			}

			return Collections.unmodifiableList(result);
		}

		@NotNull
		private List<String> computeParentLabels() {
			List<String> parentLabels = new ArrayList<>();
			Neo4jPersistentEntity<?> parentNodeDescriptionCalculated = (Neo4jPersistentEntity<?>) parentNodeDescription;

			while (parentNodeDescriptionCalculated != null) {
				if (isExplicitlyAnnotatedAsEntity(parentNodeDescriptionCalculated)) {

					parentLabels.add(parentNodeDescriptionCalculated.getPrimaryLabel());
					parentLabels.addAll(parentNodeDescriptionCalculated.getAdditionalLabels());
				}
				parentNodeDescriptionCalculated = (Neo4jPersistentEntity<?>) parentNodeDescriptionCalculated.getParentNodeDescription();
			}
			return parentLabels;
		}

		/**
		 * @param entity The entity to check for annotation
		 * @return True if the type is explicitly annotated as entity and as such eligible to contribute to the list of labels
		 * and required to be part of the label lookup.
		 */
		private static boolean isExplicitlyAnnotatedAsEntity(NodeDescription<?> entity) {
			return entity.getUnderlyingClass().isAnnotationPresent(Node.class);
		}

		@Override
		public Class<T> getUnderlyingClass() {
			return type;
		}

		@Override
		public @Nullable IdDescription getIdDescription() {
			return null;
		}

		@Override
		public Collection<GraphPropertyDescription> getGraphProperties() {
			return null;
		}

		@Override
		public Collection<GraphPropertyDescription> getGraphPropertiesInHierarchy() {
			return null;
		}

		@Override
		public Optional<GraphPropertyDescription> getGraphProperty(String fieldName) {
			return Optional.empty();
		}

		@Override
		public Collection<RelationshipDescription> getRelationships() {
			return relationships;
		}

		@Override
		public Collection<RelationshipDescription> getRelationshipsInHierarchy(Predicate<PropertyFilter.RelaxedPropertyPath> propertyPredicate) {
			return null;
		}

		@Override
		public Collection<RelationshipDescription> getRelationshipsInHierarchy(Predicate<PropertyFilter.RelaxedPropertyPath> propertyFilter, PropertyFilter.RelaxedPropertyPath path) {
			return null;
		}

		@Override
		public void addChildNodeDescription(NodeDescription<?> child) {

		}

		@Override
		public Collection<NodeDescription<?>> getChildNodeDescriptionsInHierarchy() {
			return null;
		}

		@Override
		public void setParentNodeDescription(NodeDescription<?> parent) {

		}

		@Override
		public @Nullable NodeDescription<?> getParentNodeDescription() {
			return null;
		}

		@Override
		public boolean containsPossibleCircles(Predicate<PropertyFilter.RelaxedPropertyPath> includeField) {
			return false;
		}

		@Override
		public boolean describesInterface() {
			return false;
		}

		@Override
		public boolean hasVersionProperty() {
			return false;
		}

		@Override
		public GraphPropertyDescription getVersionProperty() {
			return null;
		}

		@Override
		public GraphPropertyDescription getIdProperty() {
			return idProperty;
		}

		@Override
		public GraphPropertyDescription getPersistentNeo4jProperty(Class<? extends Annotation> targetNodeClass) {
			return null;
		}
	}
}
