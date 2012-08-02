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

package juzu.impl.plugin.template.metadata;

import juzu.impl.inject.BeanDescriptor;
import juzu.impl.metadata.Descriptor;
import juzu.impl.common.JSON;
import juzu.template.Template;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class TemplatesDescriptor extends Descriptor {

  /** . */
  private final List<TemplateDescriptor> templates;

  /** . */
  private final String packageName;

  /** . */
  private final ArrayList<BeanDescriptor> beans;

  public TemplatesDescriptor(ClassLoader loader, JSON config) throws Exception {
    ArrayList<BeanDescriptor> beans = new ArrayList<BeanDescriptor>();
    List<TemplateDescriptor> templates = new ArrayList<TemplateDescriptor>();

    // Load templates
    for (String fqn : config.getList("templates", String.class)) {
      Class<?> clazz = loader.loadClass(fqn);
      Field f = clazz.getField("DESCRIPTOR");
      TemplateDescriptor descriptor = (TemplateDescriptor)f.get(null);
      templates.add(descriptor);
      beans.add(new BeanDescriptor(Template.class, null, null, descriptor.getType()));
    }

    //
    String packageName = config.getString("package");

    //
    this.templates = templates;
    this.packageName = packageName;
    this.beans = beans;
  }

  public Iterable<BeanDescriptor> getBeans() {
    return beans;
  }

  public List<TemplateDescriptor> getTemplates() {
    return templates;
  }

  public TemplateDescriptor getTemplate(String path) throws NullPointerException {
    if (path == null) {
      throw new NullPointerException("No null path accepted");
    }
    for (TemplateDescriptor template : templates) {
      if (template.getPath().equals(path)) {
        return template;
      }
    }
    return null;
  }

  public String getPackageName() {
    return packageName;
  }
}
