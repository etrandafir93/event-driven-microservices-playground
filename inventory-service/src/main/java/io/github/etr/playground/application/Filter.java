package io.github.etr.playground.application;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

/**
 * Indicates that a class is a Filter component in the Pipes and Filters architecture.
 * Filters are responsible for processing, transforming, validating, or enriching data
 * as it flows through the processing pipeline.
 * 
 * @see Component
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Filter {

    @AliasFor(annotation = Component.class)
    String value() default "";

}