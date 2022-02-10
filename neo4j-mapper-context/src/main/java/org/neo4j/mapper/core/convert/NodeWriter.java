package org.neo4j.mapper.core.convert;

import java.util.Map;

public interface NodeWriter {
    void write(Object entity, Map<String, Object> node);
}
