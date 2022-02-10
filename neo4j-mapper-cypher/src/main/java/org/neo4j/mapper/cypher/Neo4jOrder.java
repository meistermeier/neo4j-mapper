package org.neo4j.mapper.cypher;

interface Neo4jOrder {

	static Neo4jOrder asc(String orderByProperty) {
		return new Neo4jOrderImpl(Neo4jSort.Direction.ASC, orderByProperty);
	}

	static Neo4jOrder desc(String orderByProperty) {
		return new Neo4jOrderImpl(Neo4jSort.Direction.DESC, orderByProperty);
	}

	String getProperty();

	boolean isIgnoreCase();

	boolean isAscending();

	static Neo4jOrder from(Neo4jSort.Direction direction, String property) {
		return new Neo4jOrderImpl(direction, property);
	}

	Neo4jSort.Direction getDirection();

	class Neo4jOrderImpl implements Neo4jOrder {

		private final Neo4jSort.Direction direction;
		private final String property;

		public Neo4jOrderImpl(Neo4jSort.Direction direction, String property) {

			this.direction = direction;
			this.property = property;
		}

		@Override
		public String getProperty() {
			return property;
		}

		@Override
		public boolean isIgnoreCase() {
			return false;
		}

		@Override
		public boolean isAscending() {
			return direction == Neo4jSort.Direction.ASC;
		}

		@Override
		public Neo4jSort.Direction getDirection() {
			return direction;
		}
	}
}
