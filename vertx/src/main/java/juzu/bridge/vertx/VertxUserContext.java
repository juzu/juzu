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
package juzu.bridge.vertx;

import juzu.impl.common.Tools;
import juzu.request.UserContext;

import java.util.Locale;

/** @author Julien Viet */
class VertxUserContext implements UserContext {

  /** . */
  static final VertxUserContext INSTANCE = new VertxUserContext();

  /** . */
  private static final Iterable<Locale> LOCALES = Tools.iterable(Locale.ENGLISH);

  public Locale getLocale() {
    return Locale.ENGLISH;
  }

  public Iterable<Locale> getLocales() {
    return LOCALES;
  }
}
