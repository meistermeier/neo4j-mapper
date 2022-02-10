package org.neo4j.mapper.core.convert;

import org.neo4j.driver.types.MapAccessor;

public interface NodeReader {
    // Todo this one would be dope to have ;) Object read(MapAccessor record);

    <T> T read(Class<T> targetClass, MapAccessor record);
}
