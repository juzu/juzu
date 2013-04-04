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

package juzu.templating.mustache;

import juzu.template.TemplateRenderContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class ContextMap extends AbstractMap<String, Object> {

  /** . */
  final TemplateRenderContext renderContext;

  ContextMap(TemplateRenderContext renderContext) {
    this.renderContext = renderContext;
  }

  @Override
  public boolean containsKey(Object key) {
    return get(key) != null;
  }

  @Override
  public Object get(Object key) {
    Object value = null;
    if (key instanceof String) {
      Map<String,?> attributes = renderContext.getAttributes();
      if (attributes != null) {
        value = attributes.get(key);
      }
      if (value == null) {
        try {
          value = renderContext.resolveBean((String)key);
        }
        catch (InvocationTargetException e) {
          throw new UndeclaredThrowableException(e.getCause());
        }
      }
    }
    return value;
  }

  @Override
  public Set<Entry<String, Object>> entrySet() {
    // We don't care about listing and actually it would not be really possible with bean resolution
    return Collections.emptySet();
  }
}
