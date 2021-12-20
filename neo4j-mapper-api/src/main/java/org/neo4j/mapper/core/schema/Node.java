package org.neo4j.mapper.core.schema;

import org.apiguardian.api.API;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The annotation to configure the mapping from a node with a given set of labels to a class and vice versa.
 *
 * @author Michael J. Simons
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Documented
@API(status = API.Status.STABLE, since = "1.0")
public @interface Node {

    /**
     * @return See {@link #labels()}.
     */
    String[] value() default {};

    /**
     * @return The labels to identify a node with that is supposed to be mapped to the class annotated with
     *         {@link Node @Node}. The first label will be the primary label if {@link #primaryLabel()} was not
     *         set explicitly.
     */
    String[] labels() default {};

    /**
     * @return The explicit primary label to identify a node.
     */
    String primaryLabel() default "";

}
