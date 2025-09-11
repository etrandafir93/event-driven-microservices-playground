package io.github.etr.playground;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.stereotype.Component;

/**
 * Indicates that a class is an Adapter component in the Hexagonal Architecture.
 * Adapters handle external interfaces and translate between the application's domain
 * and external systems, frameworks, or protocols.
 * 
 * @see Component
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Adapter {
    String value() default "";
}