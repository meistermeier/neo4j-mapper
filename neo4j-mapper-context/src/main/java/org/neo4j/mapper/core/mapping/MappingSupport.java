/*
 * Copyright 2011-2022 the original author or authors.
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

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apiguardian.api.API;
import org.jetbrains.annotations.Nullable;
import org.neo4j.driver.Value;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Relationship;
import org.neo4j.driver.types.Type;
import org.neo4j.mapper.core.mapping.Neo4jPersistentProperty;
import org.neo4j.mapper.core.mapping.NodeDescription;
import org.neo4j.mapper.core.schema.TargetNode;

/**
 * @author Michael J. Simons
 * @author Philipp Tölle
 * @author Gerrit Meier
 * @since 6.0
 */
@API(status = API.Status.INTERNAL, since = "6.0")
public final class MappingSupport {

	/**
	 * The value for a relationship can be a scalar object (1:1), a collection (1:n), a map (1:n, but with dynamic
	 * relationship types) or a map (1:n) with properties for each relationship. This method unifies the type into
	 * something iterable, depending on the given inverse type.
	 *
	 * @param rawValue The raw value to unify
	 * @return A unified collection (Either a collection of Map.Entry for dynamic and relationships with properties or a
	 *         list of related values)
	 */
	public static Collection<?> unifyRelationshipValue(Neo4jPersistentProperty property, @Nullable Object rawValue) {

		if (rawValue == null) {
			return Collections.emptyList();
		}

		Collection<?> unifiedValue;
		if (property.isDynamicAssociation()) {
			if (property.isDynamicOneToManyAssociation()) {
				unifiedValue = ((Map<?, ?>) rawValue)
						.entrySet().stream()
						.flatMap(e -> ((Collection<?>) e.getValue()).stream().map(v -> new SimpleEntry<>(e.getKey(), v)))
						.collect(Collectors.toList());
			} else {
				unifiedValue = ((Map<?, ?>) rawValue).entrySet();
			}
		} else if (property.isCollectionLike()) {
			unifiedValue = (Collection<?>) rawValue;
		} else {
			unifiedValue = Collections.singleton(rawValue);
		}
		return unifiedValue;
	}

	/**
	 * A helper that produces a predicate to check whether a {@link Value} is a list value and contains only other
	 * values with a given type.
	 *
	 * @param collectionType The required collection type system
	 * @param requiredType   The required type
	 * @return A predicate
	 */
	public static Predicate<Value> isListContainingOnly(Type collectionType, Type requiredType) {

		Predicate<Value> containsOnlyRequiredType = entry -> {
			// either this is a list containing other list of possible the same required type
			// or the type exists directly in the list
			for (Value listEntry : entry.values()) {
				if (listEntry.hasType(collectionType)) {
					boolean listInListCorrectType = true;
					for (Value listInListEntry : entry.asList(Function.identity())) {
						listInListCorrectType = listInListCorrectType && isListContainingOnly(collectionType, requiredType)
								.test(listInListEntry);
					}
					return listInListCorrectType;
				} else if (!listEntry.hasType(requiredType)) {
					return false;
				}
			}
			return true;
		};

		Predicate<Value> isList = entry -> entry.hasType(collectionType);
		return isList.and(containsOnlyRequiredType);
	}

	static Collection<Relationship> extractRelationshipsFromCollection(Type collectionType, Value entry) {

		Collection<Relationship> relationships = new HashSet<>();
		if (entry.hasType(collectionType)) {
			for (Value listWithRelationshipsOrRelationship : entry.values()) {
				if (listWithRelationshipsOrRelationship.hasType(collectionType)) {
					relationships.addAll(listWithRelationshipsOrRelationship.asList(Value::asRelationship));
				} else {
					relationships.add(listWithRelationshipsOrRelationship.asRelationship());
				}
			}
		} else {
			relationships.add(entry.asRelationship());
		}
		return relationships;
	}

	static Collection<Node> extractNodesFromCollection(Type collectionType, Value entry) {

		// There can be multiple relationships leading to the same node.
		// Thus, we need a collection implementation that supports duplicates.
		Collection<Node> nodes = new ArrayList<>();
		if (entry.hasType(collectionType)) {
			for (Value listWithNodesOrNode : entry.values()) {
				if (listWithNodesOrNode.hasType(collectionType)) {
					nodes.addAll(listWithNodesOrNode.asList(Value::asNode));
				} else {
					nodes.add(listWithNodesOrNode.asNode());
				}
			}
		} else {
			nodes.add(entry.asNode());
		}
		return nodes;
	}

	private MappingSupport() {}

	/**
	 * Class that defines a tuple of relationship with properties and the connected target entity.
	 */
	@API(status = API.Status.INTERNAL)
	public final static class RelationshipPropertiesWithEntityHolder {
		private final PropertyAccessor<?> relationshipPropertiesPropertyAccessor;
		private final Object relationshipProperties;
		private final Object relatedEntity;

		RelationshipPropertiesWithEntityHolder(
				NodeDescription<?> relationshipPropertiesEntity,
				Object relationshipProperties, Object relatedEntity
		) {
			this.relationshipPropertiesPropertyAccessor = relationshipPropertiesEntity.getPropertyAccessor(relationshipProperties);
			this.relationshipProperties = relationshipProperties;
			this.relatedEntity = relatedEntity;
		}

		public PropertyAccessor<?> getRelationshipPropertiesPropertyAccessor() {
			return relationshipPropertiesPropertyAccessor;
		}

		public Object getRelationshipProperties() {
			return relationshipProperties;
		}

		public Object getRelatedEntity() {
			return relatedEntity;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			RelationshipPropertiesWithEntityHolder that = (RelationshipPropertiesWithEntityHolder) o;
			return relationshipProperties.equals(that.relationshipProperties) && relatedEntity.equals(that.relatedEntity);
		}

		@Override
		public int hashCode() {
			return Objects.hash(relationshipProperties, relatedEntity);
		}
	}
}
