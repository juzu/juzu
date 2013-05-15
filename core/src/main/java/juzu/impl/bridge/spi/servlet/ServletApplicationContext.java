/*
 * Copyright 2013 eXo Platform SAS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package juzu.impl.bridge.spi.servlet;

import juzu.request.ApplicationContext;

import java.util.Locale;
import java.util.ResourceBundle;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ServletApplicationContext implements ApplicationContext {

  /** The classloader associated with the resource bundle. */
  private final ClassLoader classLoader;

  /** The optional bundle name. */
  private final String bundleName;

  public ServletApplicationContext(ClassLoader classLoader, String bundleName) {
    this.classLoader = classLoader;
    this.bundleName = bundleName;
  }

  public ResourceBundle resolveBundle(Locale locale) {
    if (bundleName != null) {
      return ResourceBundle.getBundle(bundleName, locale, classLoader);
    }
    return null;
  }
}
