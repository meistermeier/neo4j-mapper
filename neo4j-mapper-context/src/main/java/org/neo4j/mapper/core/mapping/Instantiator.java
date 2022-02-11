package org.neo4j.mapper.core.mapping;

public interface Instantiator {

	<ET> ET createInstance(NodeDescription<ET> nodeDescription, ParameterValueProvider<ET> parameterValueProvider);
}
