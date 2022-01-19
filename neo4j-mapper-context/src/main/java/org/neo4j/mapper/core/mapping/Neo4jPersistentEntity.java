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
import org.neo4j.mapper.core.support.Assert;

import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * @author Michael J. Simons
 * @param <T> type of the underlying class
 * @since 6.0
 */
@API(status = API.Status.STABLE, since = "6.0")
public interface Neo4jPersistentEntity<T> extends NodeDescription<T> {

	/**
	 * @return An optional property pointing to a {@link java.util.Collection Collection&lt;String&gt;} containing dynamic
	 *         "runtime managed" labels.
	 */
	Optional<Neo4jPersistentProperty> getDynamicLabelsProperty();

	/**
	 * Determines if the entity is annotated with {@link org.neo4j.mapper.core.schema.RelationshipProperties}
	 *
	 * @return true if this is a relationship properties class, otherwise false.
	 */
	boolean isRelationshipPropertiesEntity();

	default Neo4jPersistentProperty getRequiredIdProperty() {
		Neo4jPersistentProperty property = this.getIdProperty();
		if (property != null) {
			return property;
		} else {
			throw new IllegalStateException(String.format("Required identifier property not found for %s!", this.getType()));
		}
	}

	void addPersistentProperty(Neo4jPersistentProperty property);

	// todo void addAssociation(Association<Neo4jPersistentProperty> association);

	void verify() throws MappingException;

	// from persistent property
	String getName();

//	@Nullable
//	PreferredConstructor<T, Neo4jPersistentProperty> getPersistenceConstructor();

	boolean isConstructorArgument(Neo4jPersistentProperty property);

	boolean isIdProperty(Neo4jPersistentProperty property);

	boolean isVersionProperty(Neo4jPersistentProperty property);

	@Nullable
	Neo4jPersistentProperty getIdProperty();

	@Nullable
	Neo4jPersistentProperty getVersionProperty();

	default Neo4jPersistentProperty getRequiredVersionProperty() {
		Neo4jPersistentProperty property = this.getVersionProperty();
		if (property != null) {
			return property;
		} else {
			throw new IllegalStateException(String.format("Required version property not found for %s!", this.getType()));
		}
	}

	@Nullable
	Neo4jPersistentProperty getPersistentProperty(String name);

	default Neo4jPersistentProperty getRequiredPersistentProperty(String name) {
		Neo4jPersistentProperty property = this.getPersistentProperty(name);
		if (property != null) {
			return property;
		} else {
			throw new IllegalStateException(String.format("Required property %s not found for %s!", name, this.getType()));
		}
	}

	@Nullable
	default Neo4jPersistentProperty getPersistentProperty(Class<? extends Annotation> annotationType) {
		Iterator<Neo4jPersistentProperty> it = this.getPersistentProperties(annotationType).iterator();
		return it.hasNext() ? (Neo4jPersistentProperty)it.next() : null;
	}

	Iterable<Neo4jPersistentProperty> getPersistentProperties(Class<? extends Annotation> annotationType);

	boolean hasIdProperty();

	boolean hasVersionProperty();

	Class<T> getType();

//	Alias getTypeAlias();

//	TypeInformation<T> getTypeInformation();

	void doWithProperties(Consumer<Neo4jPersistentProperty> handler);

	void doWithAssociations(Consumer<Neo4jPersistentProperty> consumer);

	default void doWithAll(Consumer<Neo4jPersistentProperty> handler) {
		Assert.notNull(handler, "PropertyHandler must not be null!");
		this.doWithProperties(handler);
		this.doWithAssociations(handler);
	}

	@Nullable
	<A extends Annotation> A findAnnotation(Class<A> annotationType);

	default <A extends Annotation> A getRequiredAnnotation(Class<A> annotationType) throws IllegalStateException {
		A annotation = this.findAnnotation(annotationType);
		if (annotation != null) {
			return annotation;
		} else {
			throw new IllegalStateException(String.format("Required annotation %s not found for %s!", annotationType, this.getType()));
		}
	}

	<A extends Annotation> boolean isAnnotationPresent(Class<A> annotationType);

//	<B> PersistentPropertyAccessor<B> getPropertyAccessor(B bean);
//
//	<B> PersistentPropertyPathAccessor<B> getPropertyPathAccessor(B bean);
//
//	IdentifierAccessor getIdentifierAccessor(Object bean);

	boolean isNew(Object bean);

	boolean isImmutable();

	boolean requiresPropertyPopulation();

}
