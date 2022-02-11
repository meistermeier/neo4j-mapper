package com.meistermeier.neo4j.mapper.integration;

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

/**
 * @author Gerrit Meier
 */
public class Example2 {

	public static void main(String[] args) {
		var driver = Environment.getDriver();
		driver.session().run("CREATE (:MovieWithEmptyConstructor{title: 'The Matrix'})").consume();

		var nodeDescriptionStore = new NodeDescriptionStore();
		nodeDescriptionStore.put("MovieWithEmptyConstructor", NodeDescription.of(MovieWithEmptyConstructor.class));

		driver
			.session()
			.run("MATCH (n:MovieWithEmptyConstructor) return n")
			.list(record -> new DefaultNeo4jEntityConverter(
				nodeDescription -> new Instantiator() {
					@Override public <ET> ET createInstance(NodeDescription<ET> nodeDescription,
						ParameterValueProvider<ET> parameterValueProvider) {
						EntityConstructor<ET> persistenceConstructor = nodeDescription.getPersistenceConstructor();
						return persistenceConstructor.createInstance();
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
			).read(MovieWithEmptyConstructor.class, new RecordMapAccessor(record)))
			.forEach(System.out::println); // MovieWithEmptyConstructor{title='The Matrix'}

		Environment.stopContainer();
	}
}
