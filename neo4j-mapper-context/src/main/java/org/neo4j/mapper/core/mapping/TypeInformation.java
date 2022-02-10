package org.neo4j.mapper.core.mapping;

public class TypeInformation<T> {

    public static <T> TypeInformation<T> from(Class<T> clazz) {
        return null;
    }

    public TypeInformation<T> getRequiredActualType() {
        return null;
    }

    public boolean isCollectionLike() {
        return false;
    }

    public Class<T> getType() {
        return null;
    }

    public TypeInformation<?> getComponentType() {
        return null;
    }

    public TypeInformation<?> getActualType() {
        return null;
    }

    public TypeInformation<?> getRequiredComponentType() {
        return null;
    }

    public TypeInformation<?> getRawTypeInformation() {
        return null;
    }

    public TypeInformation<?> getMapValueType() {
        return null;
    }

}
