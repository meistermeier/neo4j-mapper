package org.neo4j.mapper.core.mapping;

import org.neo4j.driver.Value;
import org.neo4j.mapper.core.convert.Neo4jPersistentPropertyConverter;

public interface Neo4jConversionService {

	Object convert(String f, Class<?> componentType);

	Object readValue(Value value, Class<?> type, Neo4jPersistentPropertyConverter<?> converter);
}
