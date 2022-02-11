package org.neo4j.mapper.core.mapping;

public interface ConstructorParameter<T> {

	Class<?> getType();

	String getName();
}
