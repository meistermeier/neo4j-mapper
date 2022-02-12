package org.neo4j.mapper.core.mapping;

import java.lang.reflect.Parameter;

public interface ConstructorParameter<T> {

	static <T> ConstructorParameter<T> of(Parameter parameter) {
		return new ConstructorParameter<T>() {
			@Override public Class<?> getType() {
				return parameter.getType();
			}

			@Override public String getName() {
				return parameter.getName();
			}
		};
	}

	Class<?> getType();

	String getName();
}
