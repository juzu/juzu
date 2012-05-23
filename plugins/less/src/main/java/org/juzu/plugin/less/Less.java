package org.juzu.plugin.less;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PACKAGE)
public @interface Less
{

   String[] value();

   boolean minify() default false;

}
