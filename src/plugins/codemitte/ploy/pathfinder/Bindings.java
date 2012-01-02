/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package plugins.codemitte.ploy.pathfinder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author moritz
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface  Bindings {
    Bind[] value();
}
