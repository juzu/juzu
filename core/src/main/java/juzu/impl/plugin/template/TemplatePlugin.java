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
import juzu.impl.plugin.application.ApplicationContext;
import juzu.impl.metadata.Descriptor;
import juzu.impl.plugin.application.ApplicationPlugin;
import juzu.impl.template.spi.TemplateStub;
import juzu.impl.plugin.template.metadata.TemplatesDescriptor;
import juzu.impl.common.JSON;
import juzu.impl.common.Path;
import juzu.template.Template;
import juzu.template.TemplateRenderContext;

import javax.inject.Inject;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class TemplatePlugin extends ApplicationPlugin {

  /** . */
  private TemplatesDescriptor descriptor;

  /** . */
  private final ConcurrentHashMap<Path, TemplateStub> stubs;

  @Inject
  ApplicationContext application;

  public TemplatePlugin() {
    super("plugin/template");

    //
    this.stubs = new ConcurrentHashMap<Path, TemplateStub>();
  }

  @Override
  public Descriptor init(ClassLoader loader, JSON config) throws Exception {
    return descriptor = new TemplatesDescriptor(loader, config);
  }

  public TemplateStub resolveTemplateStub(String path) {
    return resolveTemplateStub(juzu.impl.common.Path.parse(path));
  }

  public TemplateStub resolveTemplateStub(juzu.impl.common.Path path) {
    TemplateStub stub = stubs.get(path);
    if (stub == null) {

      //
      try {
        StringBuilder id = new StringBuilder(descriptor.getPackageName());
        for (String name : path) {
          if (id.length() > 0) {
            id.append('.');
          }
          id.append(name);
        }
        id.append("_");
        ClassLoader cl = application.getClassLoader();
        Class<?> stubClass = cl.loadClass(id.toString());
        stub = (TemplateStub)stubClass.newInstance();
      }
      catch (Exception e) {
        throw new UnsupportedOperationException("handle me gracefully", e);
      }

      //
      TemplateStub phantom = stubs.putIfAbsent(path, stub);
      if (phantom != null) {
        stub = phantom;
      } else {
        stub.init(application.getClassLoader());
      }
    }

    //
    return stub;
  }

  public TemplateRenderContext render(Template template, PropertyMap properties, Map<String, ?> parameters, Locale locale) {

    //
    TemplateStub stub = resolveTemplateStub(template.getPath());

    //
    return new TemplateRenderContextImpl(
        this,
        properties,
        stub,
        parameters,
        locale);
  }
}
