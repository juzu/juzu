/*
 * Copyright (C) 2012 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package juzu.plugin.asset;

import juzu.asset.AssetLocation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares assets.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PACKAGE)
public @interface Assets {

  /**
   * The application scripts.
   *
   * @return a list of scripts
   */
  Script[] scripts() default {};

  /**
   * The application stylesheets.
   *
   * @return a list of stylesheet
   */
  Stylesheet[] stylesheets() default {};

  /**
   * Declare a set of scripts for the application, those scripts are not sent.
   *
   * @return
   */
  Stylesheet[] declaredScripts() default {};


  Script[] declaredStylesheet() default {};

  /**
   * The default asset location used by the contained assets when no location
   * is explicitly defined.
   *
   * @return the default asset location
   */
  AssetLocation location() default AssetLocation.CLASSPATH;

}
