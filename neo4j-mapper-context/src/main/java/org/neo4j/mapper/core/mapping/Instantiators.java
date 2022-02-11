package org.neo4j.mapper.core.mapping;

/**
 * @author Michael J. Simons
 * @author Gerrit Meier
 * @author Philipp Tölle
 * @soundtrack The Kleptones - A Night At The Hip-Hopera
 * @since 6.0
 */
public interface Instantiators {

	Instantiator getInstantiatorFor(NodeDescription<?> nodeDescription);
}
