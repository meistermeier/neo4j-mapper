package org.neo4j.mapper.core.mapping;

// mock exception
public class UnknownEntityException extends RuntimeException {

    public UnknownEntityException(Class<?> classy) {
        super(classy.getName() + " is an unknown entity.");
    }
}
