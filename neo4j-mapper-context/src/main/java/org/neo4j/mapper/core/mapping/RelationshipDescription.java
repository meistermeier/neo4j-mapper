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
import org.neo4j.mapper.core.schema.Relationship;
import org.neo4j.mapper.core.support.Assert;
import org.neo4j.mapper.core.support.StringUtils;

import java.util.Locale;
import java.util.Optional;

/**
 * Description of a relationship. Those descriptions always describe outgoing relationships. The inverse direction is
 * maybe defined on the {@link NodeDescription} reachable in the {@link Schema} via it's primary label defined by
 * {@link #getTarget}.
 *
 * @author Michael J. Simons
 * @since 6.0
 */
@API(status = API.Status.INTERNAL, since = "6.0")
public interface RelationshipDescription {

	String NAME_OF_RELATIONSHIP = "__relationship__";

	String NAME_OF_RELATIONSHIP_TYPE = "__relationshipType__";

	static RelationshipDescription of(GraphPropertyDescription relationshipProperty) {
		return new RelationshipDescription() {
			private Relationship relationshipAnnotation = relationshipProperty.findNeo4jAnnotation(Relationship.class);

			@Override
			public String getType() {
				if (relationshipAnnotation != null) {
					if (StringUtils.hasText(relationshipAnnotation.value())) {
						return relationshipAnnotation.value();
					} else if (StringUtils.hasText(relationshipAnnotation.type())) {
						return relationshipAnnotation.type();
					}
				}

				return deriveRelationshipType(relationshipProperty.getFieldName());
			}

			static String deriveRelationshipType(String name) {

				Assert.hasText(name, "The name to derive the type from is required.");

				StringBuilder sb = new StringBuilder();

				int codePoint;
				int previousIndex = 0;
				int i = 0;
				while (i < name.length()) {
					codePoint = name.codePointAt(i);
					if (Character.isLowerCase(codePoint)) {
						if (i > 0 && !Character.isLetter(name.codePointAt(previousIndex))) {
							sb.append("_");
						}
						codePoint = Character.toUpperCase(codePoint);
					} else if (sb.length() > 0) {
						sb.append("_");
					}
					sb.append(Character.toChars(codePoint));
					previousIndex = i;
					i += Character.charCount(codePoint);
				}
				return sb.toString();
			}

			@Override
			public boolean isDynamic() {
				return relationshipProperty.isMap() ;
			}

			@Override
			public NodeDescription<?> getSource() {
				return null;
			}

			@Override
			public NodeDescription<?> getTarget() {
				return Neo4jPersistentEntity.of(relationshipProperty.getType());
			}

			@Override
			public String getFieldName() {
				return null;
			}

			@Override
			public Relationship.Direction getDirection() {
				return relationshipAnnotation != null && relationshipAnnotation.direction() !=null
						? relationshipAnnotation.direction()
						: Relationship.Direction.OUTGOING;
			}

			@Override
			public @Nullable NodeDescription<?> getRelationshipPropertiesEntity() {
				return null;
			}

			@Override
			public boolean hasRelationshipProperties() {
				return false;
			}

			@Override
			public void setRelationshipObverse(RelationshipDescription relationshipObverse) {

			}

			@Override
			public RelationshipDescription getRelationshipObverse() {
				return null;
			}

			@Override
			public boolean hasRelationshipObverse() {
				return false;
			}

			@Override
			public GraphPropertyDescription getInverse() {
				return relationshipProperty;
			}

			@Override
			public Neo4jPersistentProperty getObverse() {
				return null;
			}
		};
	}

	/**
	 * If this relationship is dynamic, then this method always returns the name of the inverse property.
	 *
	 * @return The type of this relationship
	 */
	String getType();

	/**
	 * A relationship is dynamic when it's modelled as a {@code Map<String, ?>}.
	 *
	 * @return True, if this relationship is dynamic
	 */
	boolean isDynamic();

	/**
	 * The source of this relationship is described by the primary label of the node in question.
	 *
	 * @return The source of this relationship
	 */
	NodeDescription<?> getSource();

	/**
	 * The target of this relationship is described by the primary label of the node in question.
	 *
	 * @return The target of this relationship
	 */
	NodeDescription<?> getTarget();

	/**
	 * The name of the property where the relationship was defined. This is used by the Cypher creation to name the return
	 * values.
	 *
	 * @return The name of the field storing the relationship property
	 */
	String getFieldName();

	/**
	 * The direction of the defined relationship. This is used by the Cypher creation to query for relationships and
	 * create them with the right directions.
	 *
	 * @return The direction of the relationship
	 */
	Relationship.Direction getDirection();

	/**
	 * If this is a relationship with properties, the properties-defining class will get returned, otherwise
	 * {@literal null}.
	 *
	 * @return The type of the relationship property class for relationship with properties, otherwise {@literal null}
	 */
	@Nullable
	NodeDescription<?> getRelationshipPropertiesEntity();

	/**
	 * Tells if this relationship is a relationship with additional properties. In such cases
	 * {@code getRelationshipPropertiesClass} will return the type of the properties holding class.
	 *
	 * @return {@literal true} if an additional properties are available, otherwise {@literal false}
	 */
	boolean hasRelationshipProperties();

	default boolean hasInternalIdProperty() {

		return hasRelationshipProperties() && Optional.ofNullable(getRelationshipPropertiesEntity().getIdDescription())
				.filter(IdDescription::isInternallyGeneratedId).isPresent();
	}

	default boolean isOutgoing() {
		return Relationship.Direction.OUTGOING.equals(this.getDirection());
	}

	default boolean isIncoming() {
		return Relationship.Direction.INCOMING.equals(this.getDirection());
	}

	@NotNull
	default String generateRelatedNodesCollectionName(NodeDescription<?> mostAbstractNodeDescription) {

		return this.getSource().getMostAbstractParentLabel(mostAbstractNodeDescription) + "_" + this.getType() + "_" + this.getTarget().getPrimaryLabel();
	}

	/**
	 * Set the relationship definition that describes the opposite side of the relationship.
	 *
	 * @param relationshipObverse logically same relationship definition in the target entity
	 */
	void setRelationshipObverse(RelationshipDescription relationshipObverse);

	/**
	 * @return logically same relationship definition in the target entity
	 */
	RelationshipDescription getRelationshipObverse();

	/**
	 * Checks if there is a relationship description describing the obverse of this relationship.
	 *
	 * @return true if a logically same relationship in the target entity exists, otherwise false.
	 */
	boolean hasRelationshipObverse();

	GraphPropertyDescription getInverse();

	Neo4jPersistentProperty getObverse();
}
