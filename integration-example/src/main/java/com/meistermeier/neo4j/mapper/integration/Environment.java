package com.meistermeier.neo4j.mapper.integration;

import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * @author Gerrit Meier
 */
public class Environment {

	private static final Neo4jContainer<?> container = new Neo4jContainer<>(DockerImageName.parse("neo4j:4.4.3"))
		.withoutAuthentication()
		.withReuse(true);

	private static final Driver driver;

	static {
		container.start();
		driver = GraphDatabase.driver(container.getBoltUrl());
	}

	static Driver getDriver() {
		return driver;
	}

	static void stopContainer() {
		container.stop();
	}
}
