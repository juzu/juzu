package org.juzu;

import javax.enterprise.context.NormalScope;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
@NormalScope
@Target( { TYPE, METHOD, FIELD })
@Retention(RUNTIME)
public @interface ResourceScoped
{
}
