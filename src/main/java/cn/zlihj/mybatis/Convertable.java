package cn.zlihj.mybatis;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Target(TYPE)
@Retention(RUNTIME)
public @interface Convertable {
    /**
     * @return the method name that convert to another type
     */
    String valueMethod() default "value";

    /**
     * @return the method name that convert from another type
     */
    String ofMethod() default "of";

}
