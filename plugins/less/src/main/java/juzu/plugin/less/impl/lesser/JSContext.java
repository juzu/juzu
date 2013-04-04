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

package juzu.plugin.less.impl.lesser;

import juzu.plugin.less.impl.lesser.jsr223.JSR223Context;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class JSContext {

  public static JSContext create() {
    JSContext context = JSR223Context.create();
    if (context == null) {
      // No JS available via JSR223
      try {
        Class<JSContext> type = (Class<JSContext>)JSContext.class.getClassLoader().loadClass("juzu.plugin.less.impl.lesser.rhino1_7R3.Rhino1_7R3Context");
        context = type.newInstance();
      }
      catch (Throwable e) {
        e.printStackTrace();
        // Cannot load it / should we log it ?
      }
    }
    if (context == null) {
      throw new UnsupportedOperationException("No JavaScript support available");
    }
    return context;
  }

  public abstract void put(String name, Object value);

  public abstract Object eval(String script) throws Exception;

  public abstract Object invokeFunction(String name, Object... args) throws Exception;

}
