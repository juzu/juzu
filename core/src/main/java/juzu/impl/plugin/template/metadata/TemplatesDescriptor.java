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

package juzu.impl.plugin.template.metadata;

import juzu.impl.plugin.PluginDescriptor;
import juzu.impl.inject.BeanDescriptor;
import juzu.impl.common.JSON;
import juzu.template.Template;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class TemplatesDescriptor extends PluginDescriptor {

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
      beans.add(BeanDescriptor.createFromImpl(Template.class, null, null, descriptor.getType()));
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
