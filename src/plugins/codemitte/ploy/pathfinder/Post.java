package plugins.codemitte.ploy.pathfinder;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Post {

    String value();
    
    int priority() default -1;

    String format() default "";

    String accept() default "";
}