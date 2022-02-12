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
import org.neo4j.cypherdsl.core.Expression;
import org.neo4j.mapper.core.schema.Id;
import org.neo4j.mapper.core.schema.Node;
import org.neo4j.mapper.core.support.Lazy;
import org.neo4j.mapper.core.support.ReflectionUtils;
import org.neo4j.mapper.core.support.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Describes how a class is mapped to a node inside the database. It provides navigable links to relationships and
 * access to the nodes properties.
 *
 * @param <T> The type of the underlying class
 * @author Michael J. Simons
 * @since 6.0
 */
@API(status = API.Status.STABLE, since = "6.0")
public interface NodeDescription<T> {

	/**
	 * @return The primary label of this entity inside Neo4j.
	 */
	String getPrimaryLabel();

	String getMostAbstractParentLabel(NodeDescription<?> mostAbstractNodeDescription);

	/**
	 * @return the list of all additional labels (All labels except the {@link NodeDescription#getPrimaryLabel()}).
	 */
	List<String> getAdditionalLabels();

	/**
	 * @return The list of all static labels, that is the union of {@link #getPrimaryLabel()} +
	 * {@link #getAdditionalLabels()}. Order is guaranteed to be the primary first, then the others.
	 * @since 6.0
	 */
	default List<String> getStaticLabels() {
		List<String> staticLabels = new ArrayList<>();
		staticLabels.add(this.getPrimaryLabel());
		staticLabels.addAll(this.getAdditionalLabels());
		return staticLabels;
	}

	/**
	 * @return The concrete class to which a node with the given {@link #getPrimaryLabel()} is mapped to
	 */
	Class<T> getUnderlyingClass();

	/**
	 * @return A description how to determine primary ids for nodes fitting this description
	 */
	@Nullable
	IdDescription getIdDescription();

	/**
	 * @return A collection of persistent properties that are mapped to graph properties and not to relationships
	 */
	Collection<GraphPropertyDescription> getGraphProperties();

	/**
	 * @return All graph properties including all properties from the extending classes if this entity is a parent entity.
	 */
	Collection<GraphPropertyDescription> getGraphPropertiesInHierarchy();

	/**
	 * Retrieves a {@link GraphPropertyDescription} by its field name.
	 *
	 * @param fieldName The field name for which the graph property description should be retrieved
	 * @return An empty optional if there is no property known for the given field.
	 */
	Optional<GraphPropertyDescription> getGraphProperty(String fieldName);

	/**
	 * @return True if entities for this node use Neo4j internal ids.
	 */
	default boolean isUsingInternalIds() {
		return this.getIdDescription() != null && this.getIdDescription().isInternallyGeneratedId();
	}

	/**
	 * This returns the outgoing relationships this node has to other nodes.
	 *
	 * @return The relationships defined by instances of this node.
	 */
	Collection<RelationshipDescription> getRelationships();

	/**
	 * This returns the relationships this node, its parent and child has to other nodes.
	 *
	 * @param propertyPredicate - Predicate to filter the fields on this node description to
	 * @return The relationships defined by instances of this node.
	 */
	Collection<RelationshipDescription> getRelationshipsInHierarchy(Predicate<PropertyFilter.RelaxedPropertyPath> propertyPredicate);

	Collection<RelationshipDescription> getRelationshipsInHierarchy(Predicate<PropertyFilter.RelaxedPropertyPath> propertyFilter, PropertyFilter.RelaxedPropertyPath path);

	/**
	 * Register a direct child node description for this entity.
	 *
	 * @param child - {@link NodeDescription} that defines an extending class.
	 */
	void addChildNodeDescription(NodeDescription<?> child);

	/**
	 * Retrieve all direct child node descriptions which extend this entity.
	 *
	 * @return all direct child node description.
	 */
	Collection<NodeDescription<?>> getChildNodeDescriptionsInHierarchy();

	/**
	 * Register the direct parent node description.
	 *
	 * @param parent - {@link NodeDescription} that describes the parent entity.
	 */
	void setParentNodeDescription(NodeDescription<?> parent);

	@Nullable
	NodeDescription<?> getParentNodeDescription();

	/**
	 * @return An expression that represents the right identifier type.
	 */
	default Expression getIdExpression() {

		return this.getIdDescription().asIdExpression();
	}

	/**
	 * @param includeField A predicate used to determine the properties that need to be looked at while detecting possible circles.
	 * @return True if the domain would contain schema circles.
	 */
	boolean containsPossibleCircles(Predicate<PropertyFilter.RelaxedPropertyPath> includeField);

	/**
	 * @return True if this persistent entity has been created for an interface.
	 * @since 6.0.8
	 */
	boolean describesInterface();

	boolean hasVersionProperty();

	GraphPropertyDescription getVersionProperty();

	default GraphPropertyDescription getRequiredVersionProperty() {
		GraphPropertyDescription versionProperty = getVersionProperty();
		if (versionProperty == null) {
			throw new IllegalStateException("Where is the version property???");
		}

		return versionProperty;
	}

	GraphPropertyDescription getIdProperty();

	default GraphPropertyDescription getRequiredIdProperty() {
		GraphPropertyDescription idProperty = getIdProperty();
		if (idProperty == null) {
			throw new IllegalStateException("Where is the id property???");
		}

		return idProperty;
	}

	GraphPropertyDescription getPersistentNeo4jProperty(Class<? extends Annotation> targetNodeClass);

	static <T> NodeDescription<T> of(Class<T> entityClass) {
		return new NodeDescriptionImpl<>(entityClass);
	}

	boolean requiresPropertyPopulation();

	boolean isImmutable();

	<IT> PropertyAccessor<IT> getPropertyAccessor(IT instance);

	EntityConstructor<T> getPersistenceConstructor();

	void doWithProperties(Consumer<GraphPropertyDescription> handler);

	void doWithAssociations(Consumer<RelationshipDescription> handler);

	class NodeDescriptionImpl<T> implements NodeDescription<T> {
		private final Class<T> type;
		private final String primaryLabel;
		private final GraphPropertyDescription idProperty;
		private final NodeDescription<?> parentNodeDescription = null;
		private final Collection<GraphPropertyDescription> properties;
		private final Collection<RelationshipDescription> relationships;

		public NodeDescriptionImpl(Class<T> type) {
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
			NodeDescription<?> parentNodeDescriptionCalculated = parentNodeDescription;

			while (parentNodeDescriptionCalculated != null) {
				if (isExplicitlyAnnotatedAsEntity(parentNodeDescriptionCalculated)) {

					parentLabels.add(parentNodeDescriptionCalculated.getPrimaryLabel());
					parentLabels.addAll(parentNodeDescriptionCalculated.getAdditionalLabels());
				}
				parentNodeDescriptionCalculated = parentNodeDescriptionCalculated.getParentNodeDescription();
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
			return properties;
		}

		@Override
		public Optional<GraphPropertyDescription> getGraphProperty(String fieldName) {
			return properties.stream().filter(property -> property.getFieldName().equals(fieldName)).findFirst();
		}

		@Override
		public Collection<RelationshipDescription> getRelationships() {
			return relationships;
		}

		@Override
		public Collection<RelationshipDescription> getRelationshipsInHierarchy(Predicate<PropertyFilter.RelaxedPropertyPath> propertyPredicate) {
			return new HashSet<>(relationships);
		}

		@Override
		public Collection<RelationshipDescription> getRelationshipsInHierarchy(Predicate<PropertyFilter.RelaxedPropertyPath> propertyFilter, PropertyFilter.RelaxedPropertyPath path) {
			return new HashSet<>(relationships);
		}

		@Override
		public void addChildNodeDescription(NodeDescription<?> child) {

		}

		@Override
		public Collection<NodeDescription<?>> getChildNodeDescriptionsInHierarchy() {
			return Set.of();
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

		@Override
		public boolean requiresPropertyPopulation() {
			return !isImmutable() && properties.stream()
				.anyMatch(it -> !(isConstructorArgument(it) || it.isTransient()));
		}

		private boolean isConstructorArgument(GraphPropertyDescription property) {
			return false;
		}

		@Override public boolean isImmutable() {
			return false;
		}

		@Override
		public EntityConstructor<T> getPersistenceConstructor() {
			Constructor<?> constructor = type.getConstructors()[0];
			return new EntityConstructor<T>() {
				@Override
				public boolean isConstructorParameter(GraphPropertyDescription property) {
					return Arrays.stream(constructor.getParameters()).anyMatch(parameter ->
						parameter.getName().equals(property.getFieldName()));
				}

				@Override
				public T createInstance(ParameterValueProvider<T> parameterValueProvider) {
					try {
						Object[] parameters = new Object[constructor.getParameterCount()];
						Parameter[] constructorParameters = constructor.getParameters();
						for (int i = 0; i < constructorParameters.length; i++) {
							Parameter parameter = constructorParameters[i];
							parameters[i] = parameterValueProvider.getParameterValue(ConstructorParameter.of(parameter));
						}
						return (T) constructor.newInstance(parameters);
					} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
						e.printStackTrace();
					}
					return null;
				}
			};
		}

		@Override
		public void doWithProperties(Consumer<GraphPropertyDescription> handler) {
			properties.forEach(handler);
		}

		@Override public void doWithAssociations(Consumer<RelationshipDescription> handler) {
			relationships.forEach(handler);
		}

		@Override
		public PropertyAccessor<?> getPropertyAccessor(Object instance) {
			return new PropertyAccessor<Object>() {
				@Override
				public Object getProperty(GraphPropertyDescription graphPropertyDescription) {
					return null;
				}

				@Override
				public void setProperty(GraphPropertyDescription graphPropertyDescription, Object value) {
					try {
						getUnderlyingClass()
							.getField(graphPropertyDescription.getFieldName())
							.set(instance, value);
					} catch (IllegalAccessException | NoSuchFieldException e) {
						e.printStackTrace();
					}

				}

				@Override public Object getBean() {
					return instance;
				}
			};
		}
	}

}
