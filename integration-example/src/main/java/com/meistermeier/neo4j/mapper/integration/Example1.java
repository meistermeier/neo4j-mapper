package com.meistermeier.neo4j.mapper.integration;

import org.neo4j.mapper.core.mapping.NodeDescription;
import org.neo4j.mapper.cypher.CypherGenerator;

public class Example1 {

	public static void main(String[] args) {
		var driver = Environment.getDriver();
		driver.session().run("CREATE (:Movie{title: 'The Matrix'})").consume();

		NodeDescription<?> movieEntity = NodeDescription.of(Movie.class);

		var cypherGenerator = CypherGenerator.INSTANCE;
		var match = cypherGenerator.prepareMatchOf(movieEntity);
		var returnClause = cypherGenerator.createReturnStatementForMatch(movieEntity);
		var cypher = match.returning(returnClause).build().getCypher();

		driver
				.session()
				.run(cypher)
				.list(record -> new Movie(record.get("movie").get("title").asString()))
				.forEach(System.out::println); // Movie[title=The Matrix]

		Environment.stopContainer();
	}
}
