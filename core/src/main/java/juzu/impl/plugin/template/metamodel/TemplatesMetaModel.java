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

package juzu.impl.plugin.template.metamodel;

import juzu.impl.common.Name;
import juzu.impl.plugin.application.metamodel.ApplicationMetaModel;
import juzu.impl.compiler.ElementHandle;
import juzu.impl.metamodel.Key;
import juzu.impl.metamodel.MetaModelObject;
import juzu.impl.common.JSON;
import juzu.impl.common.Path;

import java.util.Iterator;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class TemplatesMetaModel extends MetaModelObject implements Iterable<TemplateMetaModel> {

  /** . */
  public final static Key<TemplatesMetaModel> KEY = Key.of(TemplatesMetaModel.class);

  /** . */
  public static final Name LOCATION = Name.parse("templates");

  /** . */
  ApplicationMetaModel application;

  /** . */
  private Name qn;

  /** . */
  TemplateResolver resolver;

  /** . */
  TemplateMetaModelPlugin plugin;

  @Override
  public JSON toJSON() {
    JSON json = new JSON();
    json.map("values", getChildren(TemplateMetaModel.class));
    json.set("qn", qn);
    return json;
  }

  public Path.Absolute resolvePath(Path path) {
    return qn.resolve(path);
  }

  public ApplicationMetaModel getApplication() {
    return application;
  }

  public Name getQN() {
    return qn;
  }

  public TemplateMetaModel get(Path path) {
    return getChild(Key.of(path, TemplateMetaModel.class));
  }

  public Iterator<TemplateMetaModel> iterator() {
    return getChildren(TemplateMetaModel.class).iterator();
  }

  public void remove(ElementHandle.Field handle) {
    Key<TemplateRefMetaModel> key = Key.of(handle, TemplateRefMetaModel.class);
    TemplateRefMetaModel ref = getChild(key);
    TemplateMetaModel template = ref.getChild(TemplateMetaModel.KEY);
    removeChild(key);
    if (template.refCount == 0) {
      template.remove();
    }
  }

  public TemplateRefMetaModel add(ElementHandle.Field handle, Path.Relative path) {
    TemplateRefMetaModel ref = addChild(Key.of(handle, TemplateRefMetaModel.class), new TemplateRefMetaModel(handle, path));
    TemplateMetaModel template = getChild(Key.of(path, TemplateMetaModel.class));
    if (template == null) {
      template = addChild(Key.of(path, TemplateMetaModel.class), new TemplateMetaModel(path));
    }
    ref.addChild(TemplateMetaModel.KEY, template);
    return ref;
  }

  public void remove(TemplateMetaModel template) {
    if (template.templates != this) {
      throw new IllegalArgumentException();
    }
    removeChild(Key.of(template.path, TemplateMetaModel.class));
  }

  @Override
  protected void postAttach(MetaModelObject parent) {
    if (parent instanceof ApplicationMetaModel) {
      this.application = (ApplicationMetaModel)parent;
      this.qn = application.getName().append(LOCATION);
      this.resolver = new TemplateResolver(application);
    }
  }
}
