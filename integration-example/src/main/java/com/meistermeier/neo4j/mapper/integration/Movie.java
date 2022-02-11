package com.meistermeier.neo4j.mapper.integration;

import org.neo4j.mapper.core.schema.Id;
import org.neo4j.mapper.core.schema.Node;

@Node
record Movie(@Id String title) {
}
