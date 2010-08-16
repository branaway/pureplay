package play.cache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Cache an action's result
 * Example: @CacheFor("1h")
 * bran: an all-or-none cache, in contrast to Japid cache which enables nested cache. 
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CacheFor {
    String value() default "1h";
}

