package com.meistermeier.neo4j.mapper.integration;

import org.neo4j.mapper.core.schema.Id;
import org.neo4j.mapper.core.schema.Node;

/**
 * @author Gerrit Meier
 */
@Node
public class MovieWithEmptyConstructor {

	@Id
	public String title;

	@Override public String toString() {
		return "MovieWithEmptyConstructor{" +
			"title='" + title + '\'' +
			'}';
	}
}
