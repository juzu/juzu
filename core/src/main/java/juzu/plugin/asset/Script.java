package juzu.plugin.asset;

import juzu.asset.AssetLocation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A script asset.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface Script {

  /**
   * Returns the script id, the value is optional and has meaning when this script must be referenced (for instance as
   * a dependency of another script).
   *
   * @return the script id
   */
  String id() default "";

  /**
   * Return the script dependencies, i.e the script that should be executed before the script determined by this
   * annotation.
   *
   * @return the dependencies
   */
  String[] depends() default {};

  /**
   * The script location.
   *
   * @return the location
   */
  AssetLocation location() default AssetLocation.SERVER;

  /**
   * The script source.
   *
   * @return the source
   */
  String src();

}
