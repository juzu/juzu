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

import juzu.impl.common.Logger;
import juzu.impl.common.Name;
import juzu.impl.compiler.BaseProcessor;
import juzu.impl.plugin.application.metamodel.ApplicationMetaModel;
import juzu.impl.compiler.ElementHandle;
import juzu.impl.metamodel.Key;
import juzu.impl.metamodel.MetaModelObject;
import juzu.impl.common.JSON;
import juzu.impl.common.Path;
import juzu.impl.template.spi.Template;

import javax.tools.FileObject;
import java.util.Iterator;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class TemplatesMetaModel extends MetaModelObject implements Iterable<TemplateMetaModel> {

  /** . */
  private static final Logger log = BaseProcessor.getLogger(TemplateEmitter.class);

  /** . */
  public final static Key<TemplatesMetaModel> KEY = Key.of(TemplatesMetaModel.class);

  /** . */
  public static final Name LOCATION = Name.parse("templates");

  /** . */
  ApplicationMetaModel application;

  /** . */
  private Name qn;

  /** . */
  TemplateEmitter emitter;

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

  void resolve() {

    // Evict templates that are out of date
    log.log("Synchronizing existing templates");
    for (TemplateMetaModel child : getChildren(TemplateMetaModel.class)) {
      if (child.template != null) {
        Template<?> template = child.template;
        FileObject resource = application.resolveResource(TemplatesMetaModel.LOCATION, template.getRelativePath());
        if (resource == null) {
          // That will generate a template not found error
          child.template = null;
          log.log("Detected template removal " + template.getRelativePath());
        }
        else if (resource.getLastModified() > template.getLastModified()) {
          // That will force the regeneration of the template
          child.template = null;
          log.log("Detected stale template " + template.getRelativePath());
        }
        else {
          log.log("Template " + template.getRelativePath() + " is valid");
        }
      }
    }

    //
    for (TemplateMetaModel child : getChildren(TemplateMetaModel.class)) {
      if (child.template == null) {

        MetaModelProcessContext processContext = new MetaModelProcessContext(this);

        //
        processContext.resolve(child);
      }
    }

  }

  public TemplateRefMetaModel add(ElementHandle.Field handle, Path.Relative path) {
    TemplateRefMetaModel ref = addChild(Key.of(handle, TemplateRefMetaModel.class), new ElementTemplateRefMetaModel(handle, path));
    return add(ref, path);
  }

  public TemplateMetaModel add(TemplateRefMetaModel ref, Path.Relative path) {
    TemplateMetaModel template = add(path);
    ref.addChild(TemplateMetaModel.KEY, template);
    return template;
  }

  public TemplateMetaModel add(Path.Relative path) {
    TemplateMetaModel template = getChild(Key.of(path, TemplateMetaModel.class));
    if (template == null) {
      template = addChild(Key.of(path, TemplateMetaModel.class), new TemplateMetaModel(path));
    }
    return template;
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
      this.emitter = new TemplateEmitter(this);
    }
  }
}
