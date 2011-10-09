package org.juzu.request;

import javax.enterprise.context.NormalScope;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
@NormalScope
@Target( { TYPE, METHOD, FIELD })
@Retention(RUNTIME)
public @interface RenderScoped
{
}
