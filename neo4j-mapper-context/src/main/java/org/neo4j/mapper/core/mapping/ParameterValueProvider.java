package org.neo4j.mapper.core.mapping;

public interface ParameterValueProvider<T> {

	T getParameterValue(ConstructorParameter<T> constructorParameter);
}
