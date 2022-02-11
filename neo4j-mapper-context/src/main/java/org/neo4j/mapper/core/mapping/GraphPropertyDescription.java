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
import org.jetbrains.annotations.Nullable;
import org.neo4j.mapper.core.convert.Neo4jPersistentPropertyConverter;
import org.neo4j.mapper.core.schema.DynamicLabels;
import org.neo4j.mapper.core.schema.Relationship;
import org.neo4j.mapper.core.support.Neo4jSimpleTypes;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Collections;
import java.util.Map;

/**
 * Provides minimal information how to map class attributes to the properties of a node or a relationship.
 * <p>
 * Spring Data's persistent properties have slightly different semantics. They have an entity centric approach of
 * properties. Spring Data properties contain - if not marked otherwise - also associations.
 * <p>
 * Associations between different node types can be queried on the {@link Schema} itself.
 *
 * @author Michael J. Simons
 * @since 6.0
 */
@API(status = API.Status.STABLE, since = "6.0")
public interface GraphPropertyDescription {

	/**
	 * Dynamic associations are associations to non-simple types stored in a map with a key type of
	 * {@literal java.lang.String} or enum.
	 *
	 * @return True, if this association is a dynamic association.
	 */
	default boolean isDynamicAssociation() {

		return isRelationship() && isMap() && (getComponentType() == String.class || getComponentType().isEnum());
	}

	/**
	 * Dynamic one-to-many associations are associations to non-simple types stored in a map with a key type of
	 * {@literal java.lang.String} and values of {@literal java.util.Collection}.
	 *
	 * @return True, if this association is a dynamic association with multiple values per type.
	 * @since 6.0.1
	 */
	default boolean isDynamicOneToManyAssociation() {

		return this.isDynamicAssociation() && getNeo4jTypeInformation().getRequiredActualType().isCollectionLike();
	}

	/**
	 * @return whether the property is a property describing dynamic labels
	 * @since 6.0
	 */
	default boolean isDynamicLabels() {
		return this.isNeo4jAnnotationPresent(DynamicLabels.class) && this.isCollectionLike();
	}

	@Nullable
	Neo4jPersistentPropertyConverter<?> getOptionalConverter();

	/**
	 * @return True if this property targets an entity which is a container for relationship properties.
	 */
	boolean isEntityWithRelationshipProperties();

	/**
	 * Computes a prefix to be used on multiple properties on a node when this persistent property is annotated with
	 * CompositeProperty @CompositeProperty.
	 *
	 * @return A valid prefix
	 */
	default String computePrefixWithDelimiter() {
		return "ding";
//		CompositeProperty compositeProperty = getRequiredNeo4jAnnotation(CompositeProperty.class);
//		return Optional.of(compositeProperty.prefix()).map(String::trim).filter(s -> !s.isEmpty())
//				.orElseGet(this::getFieldName) + compositeProperty.delimiter();
	}

	<T extends Annotation> T getRequiredNeo4jAnnotation(Class<? extends Annotation> annotationClass);

	NodeDescription<?> getNeo4jOwner();
	/**
	 * @return {@literal true} if this is a read only property.
	 */
	default boolean isReadOnly() {
		return false;
	}

	boolean isIdProperty();

	boolean isEntity();

	boolean isVersionProperty();

	boolean isCollectionLike();

	boolean isMap();

	boolean isArray();

	boolean isTransient();

	boolean isWritable();

	boolean isImmutable();

	boolean isNeo4jAnnotationPresent(Class<? extends Annotation> annotationType);

	@Nullable
	Class<?> getComponentType();

	TypeInformation<?> getNeo4jTypeInformation();

	String getName();

	Class<?> getType();

	Class<?> getRawType();

	@Nullable
	<A extends Annotation> A findNeo4jAnnotation(Class<A> annotationType);

	Class<?> getAssociationTargetType();

	static GraphPropertyDescription forField(Field field) {
		return new GraphPropertyDescription() {
			@Override
			public String getFieldName() {
				return field.getName();
			}

			@Override
			public String getPropertyName() {
				// todo property naming from optional annotation
				return field.getName();
			}

			@Override
			public @Nullable Neo4jPersistentPropertyConverter<?> getOptionalConverter() {
				return null;
			}

			@Override
			public boolean isEntityWithRelationshipProperties() {
				return false;
			}

			@Override
			public <T extends Annotation> T getRequiredNeo4jAnnotation(Class<? extends Annotation> annotationClass) {
				return null;
			}

			@Override
			public NodeDescription<?> getNeo4jOwner() {
				return null;
			}

			@Override
			public boolean isIdProperty() {
				return false;
			}

			@Override
			public boolean isEntity() {
				return false;
			}

			@Override
			public boolean isVersionProperty() {
				return false;
			}

			@Override
			public boolean isCollectionLike() {
				return Collections.class.isAssignableFrom(field.getType());
			}

			@Override
			public boolean isMap() {
				return Map.class.isAssignableFrom(field.getType());
			}

			@Override
			public boolean isArray() {
				return false;
			}

			@Override
			public boolean isTransient() {
				return false;
			}

			@Override
			public boolean isWritable() {
				return false;
			}

			@Override
			public boolean isImmutable() {
				return false;
			}

			@Override
			public boolean isNeo4jAnnotationPresent(Class<? extends Annotation> annotationType) {
				return false;
			}

			@Override
			public @Nullable Class<?> getComponentType() {
				Class<?> fieldType = field.getType();

				boolean isCollection = isCollectionLike();
				boolean isMap = isMap();

				Class<?> componentType = fieldType;

				if (isCollection || isMap) {
					componentType = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
				}

				return componentType;
			}

			@Override
			public TypeInformation<?> getNeo4jTypeInformation() {
				return null;
			}

			@Override
			public String getName() {
				return null;
			}

			@Override
			public Class<?> getType() {
				if (isMap()) {
					return getMapValueType();
				}
				return getComponentType();
			}

			private Class<?> getMapValueType() {
				return (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[1];
			}

			@Override
			public Class<?> getRawType() {
				return null;
			}

			@Override
			public <A extends Annotation> @Nullable A findNeo4jAnnotation(Class<A> annotationType) {
				return field.getAnnotation(annotationType);
			}

			@Override
			public Class<?> getAssociationTargetType() {
				return null;
			}

			@Override
			public boolean isInternalIdProperty() {
				return false;
			}

			@Override
			public Class<?> getActualType() {
				return isMap() ? getMapValueType() : getComponentType();
			}

			@Override
			public boolean isRelationship() {

				Class<?> type = getComponentType();
				if (isMap()) {
					type = getMapValueType();
				}
				var noSimpleType = !Neo4jSimpleTypes.NEO4J_NATIVE_TYPES.contains(type);

				var hasAnnotation = field.isAnnotationPresent(Relationship.class);
				return hasAnnotation || noSimpleType;
			}

			@Override
			public boolean isComposite() {
				return false;
			}

			@Override
			public Wither getWither() {
				return null;
			}

			@Override
			public GraphPropertyDescription getInverse() {
				return this;
			}
		};
	}

	/**
	 * @return The name of the attribute of the mapped class
	 */
	String getFieldName();

	/**
	 * @return The name of the property as stored in the graph.
	 */
	String getPropertyName();

	/**
	 * @return True, if this property is the id property and the owner uses internal ids.
	 */
	boolean isInternalIdProperty();

	/**
	 * This will return the type of a simple property or the component type of a collection like property.
	 *
	 * @return The type of this property.
	 */
	Class<?> getActualType();

	/**
	 * @return Whether this property describes a relationship or not.
	 */
	boolean isRelationship();

	/**
	 * @return True if the entity's property (this object) is stored as multiple properties on a node or relationship.
	 */
	boolean isComposite();

	Wither getWither();

	GraphPropertyDescription getInverse();

	interface Wither {

	}
}
