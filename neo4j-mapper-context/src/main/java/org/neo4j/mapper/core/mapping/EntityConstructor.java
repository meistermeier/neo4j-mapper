package org.neo4j.mapper.core.mapping;

public interface EntityConstructor<T> {

    boolean isConstructorParameter(Neo4jPersistentProperty property);
}
