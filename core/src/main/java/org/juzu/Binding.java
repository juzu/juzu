package org.juzu;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A binding between a name and a value.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface Binding
{

   String name();

   String value() default "";

}
