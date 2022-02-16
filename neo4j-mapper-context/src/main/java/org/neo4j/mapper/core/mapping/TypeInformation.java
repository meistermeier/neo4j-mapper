package org.neo4j.mapper.core.mapping;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;

public interface TypeInformation<T> {

    static <T> TypeInformation<T> from(Class<T> clazz) {
        return new TypeInformation<T>() {

            @Override
            public TypeInformation<T> getRequiredActualType() {
                return from(clazz);
            }

            @Override
            public boolean isCollectionLike() {
                return Collection.class.isAssignableFrom(clazz);
            }

            public boolean isMap() {
                return Map.class.isAssignableFrom(clazz);
            }

            @Override
            public Class<T> getType() {
                return clazz;
            }

            @Override
            @Nullable
            public TypeInformation<?> getComponentType() {

                boolean isCollection = isCollectionLike();
                boolean isMap = isMap();

                Class<?> componentType = clazz;

                if (isCollection || isMap) {
                    componentType =  clazz.getTypeParameters()[0].getClass();
                }

                return from(componentType);
            }

            @Override
            public TypeInformation<?> getActualType() {
                throw new UnsupportedOperationException("check with calling code");
            }

            @Override
            public TypeInformation<?> getRequiredComponentType() {
                TypeInformation<?> componentType = getComponentType();
                if (componentType == null) {
                    throw new IllegalStateException("No component type");
                }

                return componentType;
            }

            @Override
            public TypeInformation<?> getRawTypeInformation() {
                throw new UnsupportedOperationException("check with calling code");
            }

            @Override
            public TypeInformation<?> getMapValueType() {
                return from((Class<?>) clazz.getTypeParameters()[1].getClass());
            }
        };
    }

    TypeInformation<T> getRequiredActualType();

    boolean isCollectionLike();

    Class<T> getType();

    TypeInformation<?> getComponentType();

    TypeInformation<?> getActualType();

    TypeInformation<?> getRequiredComponentType();

    TypeInformation<?> getRawTypeInformation();

    TypeInformation<?> getMapValueType();

}
