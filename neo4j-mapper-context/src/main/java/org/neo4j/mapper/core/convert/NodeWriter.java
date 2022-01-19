package org.neo4j.mapper.core.convert;

import java.util.Map;

public interface NodeWriter {
    Map<String, Object> write(Object entity, Map<String, Object> node);
}
