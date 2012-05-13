package org.juzu.plugin.asset;

import org.juzu.asset.AssetLocation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A stylesheet asset.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface Stylesheet
{

   /**
    * Returns the stylesheet id, the value is optional and has meaning when this stylesheet must be referenced (for instance
    * as a dependency of another stylesheet).
    *
    * @return the script id
    */
   String id() default "";

   /**
    * Return the stylesheet dependencies, i.e the stylesheet that should be executed before the stylesheet determined
    * by this annotation.
    *
    * @return the dependencies
    */
   String[] depends() default {};

   /**
    * The stylesheet location.
    *
    * @return the location
    */
   AssetLocation location() default AssetLocation.SERVER;

   /**
    * The stylesheet source.
    *
    * @return the source
    */
   String src();

}
