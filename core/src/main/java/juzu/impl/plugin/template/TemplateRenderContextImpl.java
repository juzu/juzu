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

package juzu.impl.plugin.template;

import juzu.PropertyMap;
import juzu.impl.plugin.application.ApplicationException;
import juzu.impl.template.spi.TemplateStub;
import juzu.template.TemplateRenderContext;

import java.util.Locale;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class TemplateRenderContextImpl extends TemplateRenderContext {

  /** . */
  private final TemplatePlugin plugin;

  TemplateRenderContextImpl(TemplatePlugin plugin, PropertyMap properties, TemplateStub stub, Map<String, ?> attributes, Locale locale) {
    super(stub, properties, attributes, locale);

    //
    this.plugin = plugin;
  }

  @Override
  public TemplateStub resolveTemplate(String path) {
    return plugin.resolveTemplateStub(path);
  }

  @Override
  public Object resolveBean(String name) throws ApplicationException {
    return plugin.application.resolveBean(name);
  }
}
