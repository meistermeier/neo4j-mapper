package com.meistermeier.neo4j.mapper.integration;

import org.neo4j.driver.GraphDatabase;
import org.neo4j.mapper.core.mapping.NodeDescription;
import org.neo4j.mapper.core.schema.Id;
import org.neo4j.mapper.core.schema.Node;
import org.neo4j.mapper.cypher.CypherGenerator;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.utility.DockerImageName;

public class Example1 {

@Node
record Movie(@Id String title) {}

public static void main(String[] args) {
	Neo4jContainer<?> container = new Neo4jContainer<>(DockerImageName.parse("neo4j:4.4.3")).withoutAuthentication();
	container.start();

	var driver = GraphDatabase.driver(container.getBoltUrl());
	driver.session().run("CREATE (:Movie{title: 'The Matrix'})");

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

	container.stop();
}
}
