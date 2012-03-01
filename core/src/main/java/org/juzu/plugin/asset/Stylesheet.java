package org.juzu.plugin.asset;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A script declaration.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface Stylesheet
{

   /**
    * Where the asset is located.
    * 
    * @return the location
    */
   AssetLocation location() default AssetLocation.SERVER;

   /**
    * The script sources.
    *
    * @return the source
    */
   String src();

}
