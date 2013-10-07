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

import juzu.Application;
import juzu.impl.common.Name;
import juzu.impl.common.Tools;
import juzu.impl.fs.spi.ReadFileSystem;
import juzu.impl.metamodel.Key;
import juzu.impl.plugin.application.metamodel.ApplicationMetaModel;
import juzu.impl.plugin.application.metamodel.ApplicationMetaModelPlugin;
import juzu.impl.plugin.module.metamodel.ModuleMetaModel;
import juzu.impl.metamodel.AnnotationKey;
import juzu.impl.metamodel.AnnotationState;
import juzu.impl.compiler.ProcessingContext;
import juzu.impl.compiler.ElementHandle;
import juzu.impl.metamodel.MetaModelProcessor;
import juzu.impl.template.spi.TemplateProvider;
import juzu.impl.common.JSON;
import juzu.impl.common.Path;
import juzu.template.TagHandler;

import javax.annotation.processing.Completion;
import javax.annotation.processing.Completions;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class TemplateMetaModelPlugin extends ApplicationMetaModelPlugin {

  /** . */
  private List<Key<? extends AbstractContainerMetaModel>> KEYS = Tools.list(TemplateContainerMetaModel.KEY, TagContainerMetaModel.KEY);

  /** . */
  private static final Set<Class<? extends Annotation>> PROCESSED = Collections.unmodifiableSet(Tools.<Class<? extends Annotation>>set(juzu.Path.class, juzu.Application.class));

  /** . */
  public static final Pattern PATH_PATTERN = Pattern.compile("([^/].*/|)([^./]+)\\.([a-zA-Z]+)");

  /** . */
  Map<String, TemplateProvider> providers;

  /** The tag loaded from the classpath. */
  final Map<String, TagHandler> tags = new HashMap<String, TagHandler>();

  public TemplateMetaModelPlugin() {
    super("template");
  }

  @Override
  public Set<Class<? extends java.lang.annotation.Annotation>> init(ProcessingContext env) {
    return PROCESSED;
  }

  @Override
  public void postActivate(ModuleMetaModel applications) {

    // Load the tag handlers
    for (TagHandler handler : applications.getProcessingContext().loadServices(TagHandler.class)) {
      applications.getProcessingContext().log("Loaded tag handler " + handler.getClass().getName() + " as " + handler.getName());
      tags.put(handler.getName(), handler);
    }

    // Load the template providers
    Iterable<TemplateProvider> loader = applications.processingContext.loadServices(TemplateProvider.class);
    Map<String, TemplateProvider> providers = new HashMap<String, TemplateProvider>();
    for (TemplateProvider provider : loader) {
      providers.put(provider.getSourceExtension(), provider);
    }

    //
    this.providers = providers;
  }

  @Override
  public void init(ApplicationMetaModel application) {
    TemplateContainerMetaModel templates = new TemplateContainerMetaModel();
    templates.plugin = this;
    application.addChild(TemplateContainerMetaModel.KEY, templates);
    TagContainerMetaModel tags = new TagContainerMetaModel();
    tags.plugin = this;
    application.addChild(TagContainerMetaModel.KEY, tags);
  }

  @Override
  public void processAnnotationRemoved(ApplicationMetaModel metaModel, AnnotationKey key, AnnotationState removed) {
    if (key.getType().toString().equals(Application.class.getName())) {
      // throw new UnsupportedOperationException("todo");
    } else if (key.getType().toString().equals(juzu.Path.class.getName())) {
      if (key.getElement() instanceof ElementHandle.Field) {
        ElementHandle.Field variableElt = (ElementHandle.Field)key.getElement();
        TemplateContainerMetaModel templates = metaModel.getChild(TemplateContainerMetaModel.KEY);
        Path removedPath = Path.parse((String)removed.get("value"));
        Path.Absolute absRemoved = templates.resolvePath(removedPath);
        metaModel.processingContext.log("Removing template ref " + variableElt.getFQN() + "#" + variableElt.getName() + " " + absRemoved);
        templates.remove(variableElt);
      }
    }
  }

  @Override
  public void processAnnotationUpdated(ApplicationMetaModel metaModel, AnnotationKey key, AnnotationState removed, AnnotationState added) {
    if (key.getType().toString().equals(Application.class.getName())) {
      // throw new UnsupportedOperationException("todo");
    } else if (key.getType().toString().equals(juzu.Path.class.getName())) {
      if (key.getElement() instanceof ElementHandle.Field) {
        TemplateContainerMetaModel templates = metaModel.getChild(TemplateContainerMetaModel.KEY);
        ElementHandle.Field variableElt = (ElementHandle.Field)key.getElement();
        Path addedPath = Path.parse((String)added.get("value"));
        Path.Absolute absAdded = templates.resolvePath(addedPath);
        Path removedPath = Path.parse((String)removed.get("value"));
        Path.Absolute absRemoved = templates.resolvePath(removedPath);
        metaModel.processingContext.log("Updating template ref " + variableElt.getFQN() + "#" + variableElt.getName() + " " + absRemoved + "->" + absAdded);
        templates.remove(variableElt);
        templates.add(variableElt, absAdded);
      }
    }
  }

  @Override
  public void processAnnotationAdded(ApplicationMetaModel application, AnnotationKey key, AnnotationState added) {
    if (key.getType().toString().equals(Application.class.getName())) {
      List<AnnotationState> tags = (List<AnnotationState>)added.get("tags");
      if (tags != null) {
        TagContainerMetaModel tagsMM = application.getChild(TagContainerMetaModel.KEY);
        for (AnnotationState tag : tags) {
          String nameValue = (String)tag.get("name");
          String pathValue = (String)tag.get("path");
          Path.Relative path = (Path.Relative)Path.parse(pathValue);
          tagsMM.add(nameValue, path);
        }
      }
    } else if (key.getType().toString().equals(juzu.Path.class.getName())) {
      if (key.getElement() instanceof ElementHandle.Field) {
        ElementHandle.Field variableElt = (ElementHandle.Field)key.getElement();
        TemplateContainerMetaModel templates = application.getChild(TemplateContainerMetaModel.KEY);
        Path addedPath = Path.parse((String)added.get("value"));
        Path.Absolute absAdded = templates.resolvePath(addedPath);
        application.processingContext.log("Adding template ref " + variableElt.getFQN() + "#" + variableElt.getName() + " " + absAdded);
        templates.add(variableElt, absAdded);
      }
      else if (key.getElement() instanceof ElementHandle.Class) {
        // We ignore it on purpose
      }
      else {
        throw MetaModelProcessor.ANNOTATION_UNSUPPORTED.failure(key);
      }
    }
  }

  @Override
  public void postActivate(ApplicationMetaModel application) {
    for (Key<? extends AbstractContainerMetaModel> key : KEYS) {
      AbstractContainerMetaModel container = application.getChild(key);
      container.plugin = this;
    }
  }

  @Override
  public void prePassivate(ApplicationMetaModel application) {
    application.processingContext.log("Passivating template resolver for " + application.getHandle());
    for (Key<? extends AbstractContainerMetaModel> key : KEYS) {
      AbstractContainerMetaModel container = application.getChild(key);
      container.emitter.prePassivate();
      container.plugin = null;
    }
  }

  @Override
  public void prePassivate(ModuleMetaModel applications) {
    applications.processingContext.log("Passivating templates");
    this.providers = null;
    this.tags.clear();
  }

  @Override
  public void postProcessEvents(ApplicationMetaModel application) {
    application.processingContext.log("Processing templates of " + application.getHandle());
    for (Key<? extends AbstractContainerMetaModel> key : KEYS) {
      AbstractContainerMetaModel container = application.getChild(key);
      container.resolve();
      container.emit();
    }
  }

  @Override
  public Iterable<? extends Completion> getCompletions(
      ApplicationMetaModel metaModel,
      AnnotationKey annotationKey,
      AnnotationState annotationState,
      String member, String
      userText) {
    List<Completion> completions = Collections.emptyList();
    ReadFileSystem<File> sourcePath = metaModel.getProcessingContext().getSourcePath(metaModel.getHandle());
    if (sourcePath != null) {
      try {
        final File root = sourcePath.getPath(metaModel.getChild(TemplateContainerMetaModel.KEY).getQN());
        if (root.isDirectory()) {
          File[] children = root.listFiles();
          if (children != null) {
            if (userText != null && userText.length() > 0) {
              try {
                Path path = Path.parse(userText);
                if (path.isRelative()) {
                  File from;
                  String suffix;
                  if (path.getExt() == null) {
                    from = sourcePath.getPath(root, path.getName());
                    if (from == null) {
                      from = sourcePath.getPath(root, path.getDirs());
                      suffix = path.getSimpleName();
                    } else {
                      suffix = "";
                    }
                  } else {
                    from = sourcePath.getPath(root, path.getDirs());
                    suffix = path.getSimpleName();
                  }
                  if (from != null) {
                    completions = list(root, from, suffix);
                  }
                }
              }
              catch (IllegalArgumentException ignore) {
              }
            } else {
              completions = list(root, root, "");
            }
          }
        }
      }
      catch (IOException ignore) {
      }
    }
    return completions;
  }

  private void foo(StringBuilder buffer, File root, File file) {
    if (file.equals(root)) {
      buffer.setLength(0);
    } else {
      foo(buffer, root, file.getParentFile());
      if (buffer.length() > 0) {
        buffer.append('/');
      }
      buffer.append(file.getName());
    }
  }

  private List<Completion> list(File root, File from, String suffix) {
    File[] children = from.listFiles();
    StringBuilder path = new StringBuilder();
    if (children != null) {
      ArrayList<Completion> completions = new ArrayList<Completion>();
      for (final File child : children) {
        if (child.getName().startsWith(suffix)) {
          foo(path, root, child);
          if (child.isDirectory()) {
            path.append('/');
          }
          completions.add(Completions.of(path.toString()));
        }
      }
      Collections.sort(completions, Tools.COMPLETION_COMPARATOR);
      return completions;
    } else {
      return Collections.emptyList();
    }
  }

  @Override
  public JSON getDescriptor(ApplicationMetaModel application) {
    JSON config = new JSON();
    AbstractContainerMetaModel metaModel = application.getChild(TemplateContainerMetaModel.KEY);
    LinkedHashSet<String> templates = new LinkedHashSet<String>();
    for (TemplateRefMetaModel ref : metaModel.getChildren(TemplateRefMetaModel.class)) {
      if (ref instanceof ElementMetaModel) {
        templates.add(((ElementMetaModel)ref).getPath().getName().toString());
      }
    }
    config.map("templates", templates);
    config.set("package", metaModel.getQN().toString());
    return config;
  }
}
