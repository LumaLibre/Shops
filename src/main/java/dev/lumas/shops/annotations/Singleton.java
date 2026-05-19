package dev.lumas.shops.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a codec is of a singleton type and reflective operations should
 * not create new instances.
 * <p>
 * Usage:
 * <pre>
 * {@code
 * @Singleton
 * public class ExampleCodec extends Codec<Example>() {
 *
 *      public static final ExampleCodec INSTANCE = new ExampleCodec();
 *
 *      //...
 * }
 * }
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Singleton {

    /**
     * Specifies the default instance name to be used for singleton codecs.
     * @return the name of the default instance, defaulting to "INSTANCE"
     */
    String value() default "INSTANCE";
}
