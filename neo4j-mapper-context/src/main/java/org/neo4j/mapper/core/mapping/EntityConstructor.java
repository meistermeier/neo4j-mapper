package org.neo4j.mapper.core.mapping;

public interface EntityConstructor<T> {

    boolean isConstructorParameter(GraphPropertyDescription property);

    T createInstance();
}
