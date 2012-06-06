package juzu.plugin.less;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation trigger declares a less stylesheet. It triggers the compilation of a less stylesheet when it is
 * processed. by the compiler.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.PACKAGE)
public @interface Less {

  /**
   * The path of the less stylesheet to compile, relative to the <code>assets</code> application package.
   *
   * @return the stylesheet paths
   */
  String[] value();

  /**
   * Configure the minification of the stylesheet when they are processed. The stylesheets are not minified by
   * default.
   *
   * @return true if the stylesheets should be minidfied
   */
  boolean minify() default false;

}
