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

package juzu.templating.mustache;

import juzu.template.TemplateRenderContext;

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
        value = renderContext.resolveBean((String)key);
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
