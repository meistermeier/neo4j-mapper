package org.neo4j.mapper.core.mapping;

public interface Neo4jPropertyAccessor<T> {

    Object getProperty(Neo4jPersistentProperty property);

    void setProperty(Neo4jPersistentProperty property, Object value);

    T getInstance();
}
