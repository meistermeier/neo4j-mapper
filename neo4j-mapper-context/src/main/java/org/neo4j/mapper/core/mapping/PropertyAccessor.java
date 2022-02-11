package org.neo4j.mapper.core.mapping;

/**
 * @author Gerrit Meier
 */
public interface PropertyAccessor<T> {
	Object getProperty(GraphPropertyDescription graphPropertyDescription);

	void setProperty(GraphPropertyDescription graphPropertyDescription, Object value);

	T getBean();
}
