/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.meistermeier.neo4j.mapper.integration;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import org.neo4j.mapper.core.schema.Id;

/**
 * @author Michael J. Simons
 */
public class Quiz implements Serializable {

	/**
	 * Represents one answer with its increments for the various results.
	 *
	 * @param value      the textual value of the answer
	 * @param increments the list of increments
	 */
	public record Answer(String value, List<Integer> increments) {
		public Answer {
			increments = increments == null ? Collections.emptyList() : List.copyOf(increments);
		}
	}

	/**
	 * Represents a questions with its possible answers.
	 *
	 * @param value   the textual value of the question
	 * @param answers the list of answers
	 */
	public record Question(String value, List<Answer> answers) {
		public Question {
			answers = answers == null ? Collections.emptyList() : List.copyOf(answers);
		}
	}

	/**
	 * The possible outcome of a quiz
	 *
	 * @param name         The name of the person
	 * @param title        The persons title
	 * @param description  A description
	 * @param quote        A quote
	 * @param optionalLink An optional link
	 */
	public record Outcome(@Id String name, String title, String description, String quote, String optionalLink) {

		public String formattedQuote() {
			return "\"" + quote() + "\"\n- " + name + "\nMach auch Du das Quiz:";
		}
	}

	/**
	 * The definition of a quiz consists of
	 *
	 * @param outcomes  some possible outcomes
	 * @param questions and a number of questions
	 */
	public record Definition(List<Outcome> outcomes, List<Question> questions) {
		public Definition {
			outcomes = outcomes == null ? Collections.emptyList() : List.copyOf(outcomes);
			questions = questions == null ? Collections.emptyList() : List.copyOf(questions);
		}
	}

}
