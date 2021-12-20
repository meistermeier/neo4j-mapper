/*
 * Copyright 2011-2021 the original author or authors.
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
package org.neo4j.mapper.core.schema;

import org.apiguardian.api.API;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This marker interface is used on classes to mark that they represent additional relationship properties. A class that
 * implements this interface must not be used as a or annotated with {@link Node}. It must however have exactly one field
 * of type `Long` annotated with `@Id @GeneratedValue` such as this:
 *
 * <pre>
 * &#064;RelationshipProperties
 * public class Roles {
 *
 * 	&#064;Id &#064;GeneratedValue
 *	private Long id;
 *
 *	&#064;TargetNode
 *	private final Person person;
 *
 *	// Your properties
 * }
 * </pre>
 *
 * @author Gerrit Meier
 * @author Michael J. Simons
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Documented
@Inherited
@API(status = API.Status.STABLE, since = "1.0")
public @interface RelationshipProperties {
}
