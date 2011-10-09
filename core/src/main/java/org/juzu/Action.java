package org.juzu;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Action
{

   /**
    * Returns the parameter bindings.
    *
    * @return the parameter bindings
    */
   Binding[] parameters() default {};

}
