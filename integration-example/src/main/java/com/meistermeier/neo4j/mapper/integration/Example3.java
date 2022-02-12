package com.meistermeier.neo4j.mapper.integration;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Value;
import org.neo4j.mapper.core.convert.Neo4jPersistentPropertyConverter;
import org.neo4j.mapper.core.mapping.DefaultNeo4jEntityConverter;
import org.neo4j.mapper.core.mapping.EntityConstructor;
import org.neo4j.mapper.core.mapping.Instantiator;
import org.neo4j.mapper.core.mapping.Neo4jConversionService;
import org.neo4j.mapper.core.mapping.NodeDescription;
import org.neo4j.mapper.core.mapping.NodeDescriptionStore;
import org.neo4j.mapper.core.mapping.ParameterValueProvider;
import org.neo4j.mapper.core.mapping.RecordMapAccessor;
import org.neo4j.mapper.cypher.CypherGenerator;

/**
 * @author Gerrit Meier
 */
public class Example3 {

	public static void main(String[] args) {
		var driver = Environment.getDriver();

		driver.session().run("""
CREATE (:Outcome{
name: 'OutcomeName',
title: 'OutcomeTitle',
description: 'OutcomeDescription',
quote: 'OutcomeQuote',
optionalLink: 'OutcomeOptionalLink'
})
 			""").consume();

		NodeDescription<Quiz.Outcome> description = NodeDescription.of(Quiz.Outcome.class);
		var cypherMatch = CypherGenerator.INSTANCE.prepareMatchOf(description);
		var cypherReturn = CypherGenerator.INSTANCE.createReturnStatementForMatch(description);

		var nodeDescriptionStore = new NodeDescriptionStore();
		nodeDescriptionStore.put("Outcome", description);

		String cypher = cypherMatch.returning(cypherReturn).build().getCypher();
		System.out.println(cypher);
		driver.session()
			.run(cypher)
			.list(record -> new DefaultNeo4jEntityConverter(
				nodeDescription -> new Instantiator() {
					@Override
					public <ET> ET createInstance(NodeDescription<ET> nodeDescription,
						ParameterValueProvider<ET> parameterValueProvider) {
						EntityConstructor<ET> persistenceConstructor = nodeDescription.getPersistenceConstructor();
						return persistenceConstructor.createInstance(parameterValueProvider);
					}
				},
				new Neo4jConversionService() {
					@Override public Object convert(String f, Class<?> componentType) {
						return null;
					}

					@Override
					public Object readValue(Value value, Class<?> type, Neo4jPersistentPropertyConverter<?> converter) {
						return value.asString();
					}
				},
				nodeDescriptionStore,
				driver.defaultTypeSystem()
			).read(Quiz.Outcome.class, new RecordMapAccessor(record)))
			.forEach(System.out::println);

		Environment.stopContainer();

	}
}
