package org.neo4j.mapper.cypher;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface Neo4jSort {

	Direction DEFAULT_DIRECTION = Direction.ASC;

	enum Direction {
		ASC,
		DESC
	}


	static Neo4jSort by(String... properties) {
		return new Neo4jSortImpl(DEFAULT_DIRECTION, Arrays.asList(properties));
	}

	static Neo4jSort by(Neo4jOrder... orders) {
		return new Neo4jSortImpl(Arrays.asList(orders));
	}

	static Neo4jSort unsorted() {
		return new Neo4jSortImpl(Direction.ASC, List.of());
	}

	boolean isUnsorted();

	Stream<Neo4jOrder> streamOrders();

	Neo4jSort descending();

	Neo4jSort and(Neo4jSort neo4jSort);

	Neo4jSort ascending();


	class Neo4jSortImpl implements Neo4jSort {

		private final List<Neo4jOrder> orders;

		public Neo4jSortImpl(Direction direction, List<String> properties) {
			this.orders = properties.stream().map(property -> Neo4jOrder.from(direction, property)).collect(Collectors.toList());
		}

		private Neo4jSortImpl(List<Neo4jOrder> orders) {
			this.orders = orders;
		}

		@Override
		public boolean isUnsorted() {
			return orders.isEmpty();
		}

		@Override
		public Stream<Neo4jOrder> streamOrders() {
			return orders.stream();
		}


		@Override
		public Neo4jSort and(Neo4jSort neo4jSort) {
			this.orders.addAll(neo4jSort.streamOrders().toList());
			return this;
		}

		@Override
		public Neo4jSort ascending() {
			return new Neo4jSortImpl(this.orders.stream().map(order -> new Neo4jOrder.Neo4jOrderImpl(Direction.ASC, order.getProperty())).collect(Collectors.toList()));
		}

		@Override
		public Neo4jSort descending() {
			return new Neo4jSortImpl(this.orders.stream().map(order -> new Neo4jOrder.Neo4jOrderImpl(Direction.DESC, order.getProperty())).collect(Collectors.toList()));
		}
	}
}
