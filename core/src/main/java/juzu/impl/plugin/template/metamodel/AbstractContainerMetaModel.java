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
import juzu.impl.metamodel.Key;
import juzu.impl.metamodel.MetaModelObject;
import juzu.impl.common.JSON;
import juzu.impl.common.Path;
import juzu.impl.template.spi.Template;
import juzu.template.TagHandler;

import javax.lang.model.element.Element;
import javax.tools.FileObject;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.Callable;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class AbstractContainerMetaModel extends MetaModelObject implements Iterable<TemplateMetaModel> {

  /** . */
  private static final Logger log = BaseProcessor.getLogger(AbstractEmitter.class);

  /** . */
  ApplicationMetaModel application;

  /** . */
  private Name qn;

  /** . */
  AbstractEmitter emitter;

  /** . */
  TemplateMetaModelPlugin plugin;

  /** . */
  final Name name;

  public AbstractContainerMetaModel(Name name) {
    this.name = name;
  }

  @Override
  public JSON toJSON() {
    JSON json = new JSON();
    json.map("values", getChildren(TemplateMetaModel.class));
    json.set("qn", qn);
    return json;
  }

  final TagHandler resolveTagHandler(String name) {
    TagContainerMetaModel tags = application.getChild(TagContainerMetaModel.KEY);
    TagHandler handler = tags.resolveApplicationTagHandler(name);
    if (handler == null) {
      handler = plugin.tags.get(name);
    }
    return handler;
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

  public TemplateMetaModel get(Path.Absolute path) {
    for (TemplateMetaModel child : getChildren(TemplateMetaModel.class)) {
      Path.Absolute foo = resolvePath(child.getPath());
      if (foo.equals(path)) {
        return child;
      }
    }
    return null;
  }

  public Iterator<TemplateMetaModel> iterator() {
    return getChildren(TemplateMetaModel.class).iterator();
  }

  void resolve() {

    // Evict templates that are out of date
    log.log("Synchronizing existing templates");
    for (TemplateMetaModel child : getChildren(TemplateMetaModel.class)) {
      if (child.template != null) {
        Template<?> template = child.template;
        FileObject resource = application.resolveResource(template.getPath());
        if (resource == null) {
          // That will generate a template not found error
          child.template = null;
          log.log("Detected template removal " + template.getPath());
        }
        else if (resource.getLastModified() > template.getLastModified()) {
          // That will force the regeneration of the template
          child.template = null;
          log.log("Detected stale template " + template.getPath());
        }
        else {
          log.log("Template " + template.getPath() + " is valid");
        }
      }
    }

    //
    for (final TemplateMetaModel child : getChildren(TemplateMetaModel.class)) {
      if (child.template == null) {
        Element[] elements = getElements(child);
        application.getProcessingContext().executeWithin(elements[0], new Callable<Void>() {
          public Void call() throws Exception {
            MetaModelProcessContext processContext = new MetaModelProcessContext(
                AbstractContainerMetaModel.this,
                // Initially empty since those are the roots
                Collections.<TemplateRefMetaModel>emptyList());
            processContext.resolve(child);
            return null;
          }
        });
      }
    }
  }

  void emit() {
    // Generate missing files from template
    for (TemplateMetaModel templateMM : getChildren(TemplateMetaModel.class)) {
      Element[] elements = getElements(templateMM);
      emitter.emit(templateMM, elements);
    }
  }

  protected abstract Element[] getElements(TemplateMetaModel template);

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

  protected abstract AbstractEmitter createEmitter();

  @Override
  protected void postAttach(MetaModelObject parent) {
    if (parent instanceof ApplicationMetaModel) {
      this.application = (ApplicationMetaModel)parent;
      this.qn = application.getName().append(name);
      this.emitter = createEmitter();
    }
  }
}
