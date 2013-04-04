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

package juzu.impl.plugin.application.metamodel;

import juzu.Application;
import juzu.impl.common.Name;
import juzu.impl.plugin.application.descriptor.ApplicationDescriptor;
import juzu.impl.common.JSON;
import juzu.impl.common.Tools;
import juzu.impl.compiler.ElementHandle;
import juzu.impl.compiler.ProcessingException;
import juzu.impl.metamodel.AnnotationChange;
import juzu.impl.metamodel.AnnotationKey;
import juzu.impl.metamodel.AnnotationState;
import juzu.impl.compiler.ProcessingContext;
import juzu.impl.metamodel.EventQueue;
import juzu.impl.metamodel.Key;
import juzu.impl.metamodel.MetaModelContext;
import juzu.impl.metamodel.MetaModelEvent;
import juzu.impl.metamodel.MetaModelObject;
import juzu.impl.plugin.module.metamodel.ModuleMetaModel;
import juzu.impl.plugin.module.metamodel.ModuleMetaModelPlugin;
import juzu.impl.plugin.template.metamodel.TemplateMetaModel;

import javax.annotation.processing.Completion;
import javax.lang.model.element.PackageElement;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ApplicationModuleMetaModelPlugin extends ModuleMetaModelPlugin {

  /** . */
  private static final Name APPLICATION = Name.create(Application.class);

  /** . */
  private static final String APPLICATION_DESCRIPTOR = ApplicationDescriptor.class.getSimpleName();

  /** . */
  final MetaModelContext<ApplicationMetaModelPlugin, ApplicationMetaModel> context;

  public ApplicationModuleMetaModelPlugin() {
    super("application");

    //
    this.context = new MetaModelContext<ApplicationMetaModelPlugin, ApplicationMetaModel>(ApplicationMetaModelPlugin.class);
  }

  @Override
  public Set<Class<? extends java.lang.annotation.Annotation>> init(ProcessingContext env) {

    //
    context.init(env);

    // We are interested by the Application annotation
    HashSet<Class<? extends java.lang.annotation.Annotation>> annotationTypes = new HashSet<Class<? extends java.lang.annotation.Annotation>>();
    annotationTypes.add(Application.class);
    annotationTypes.addAll(context.getSupportedAnnotations());

    //
    return annotationTypes;
  }

  @Override
  public void postActivate(ModuleMetaModel metaModel) {
    for (ApplicationMetaModelPlugin plugin : context.getPlugins()) {
      plugin.postActivate(metaModel);
    }

    //
    context.postActivate(metaModel.processingContext);
    for (ApplicationMetaModel application : metaModel.getChildren(ApplicationMetaModel.class)) {
      for (ApplicationMetaModelPlugin plugin : context.getPlugins()) {
        plugin.postActivate(application);
      }
    }
  }

  @Override
  public void processAnnotationChange(ModuleMetaModel metaModel, AnnotationChange change) {

    // Normal processing for now
    super.processAnnotationChange(metaModel, change);

    // Forward
    context.processAnnotationChange(change);
  }

  @Override
  public Iterable<? extends Completion> getCompletions(
      ModuleMetaModel metaModel,
      AnnotationKey annotationKey,
      AnnotationState annotationState,
      String member,
      String userText) {
    Iterable<? extends Completion> completions = super.getCompletions(metaModel, annotationKey, annotationState, member, userText);

    // Forward
    if (completions == null) {
      completions = context.getCompletions(annotationKey, annotationState, member, userText);
    }

    //
    return completions;
  }

  @Override
  public void processAnnotationAdded(ModuleMetaModel metaModel, AnnotationKey key, AnnotationState added) {
    if (key.getType().equals(APPLICATION)) {
      ElementHandle.Package pkg = (ElementHandle.Package)key.getElement();
      String name = (String)added.get("name");
      add(metaModel, pkg, name);
    }
  }

  private ApplicationMetaModel add(ModuleMetaModel metaModel, ElementHandle.Package handle, String applicationName) {
    ApplicationMetaModel application = new ApplicationMetaModel(handle, applicationName);
    metaModel.addChild(Key.of(handle, ApplicationMetaModel.class), application);
    context.add(application);
    return application;
  }

  @Override
  public void processAnnotationUpdated(ModuleMetaModel metaModel, AnnotationKey key, AnnotationState removed, AnnotationState added) {
    if (key.getType().equals(APPLICATION)) {
      ElementHandle.Package pkg = (ElementHandle.Package)key.getElement();
      ApplicationMetaModel application = metaModel.getChild(Key.of(pkg, ApplicationMetaModel.class));
      application.modified = true;
    }
  }

  @Override
  public void processAnnotationRemoved(ModuleMetaModel metaModel, AnnotationKey key, AnnotationState removed) {
    if (key.getType().equals(APPLICATION)) {
      ElementHandle.Package pkg = (ElementHandle.Package)key.getElement();
      ApplicationMetaModel mm = metaModel.getChild(Key.of(pkg, ApplicationMetaModel.class));
      if (mm != null) {
        context.remove(mm);
        mm.remove();
      }
    }
  }

  @Override
  public void postProcessAnnotations(ModuleMetaModel metaModel) {

    // Resolve applications
    for (ApplicationMetaModel application : metaModel.getChildren(ApplicationMetaModel.class)) {
      if (application.modified) {
        metaModel.queue(MetaModelEvent.createUpdated(application));
        application.modified = false;
      }
    }

    //
    context.postProcessAnnotations();
  }

  @Override
  public void processEvents(ModuleMetaModel metaModel, EventQueue queue) {
    // Handle root events
    while (queue.hasEvents()) {
      MetaModelEvent event = queue.popEvent();
      MetaModelObject obj = event.getObject();
      if (obj instanceof ApplicationMetaModel) {
        ApplicationMetaModel application = (ApplicationMetaModel)obj;
        if (event.getType() == MetaModelEvent.AFTER_ADD) {
          emitApplication(metaModel.processingContext, application);
        }
      }
    }

    // Distribute per application nevents
    context.processEvents();
  }

  void emitApplication(ProcessingContext env, ApplicationMetaModel application) throws ProcessingException {
    PackageElement elt = env.get(application.getHandle());
    Name fqn = application.getName().append("Application");

    //
    Writer writer = null;
    try {
      JavaFileObject applicationFile = env.createSourceFile(fqn, elt);
      writer = applicationFile.openWriter();

      writer.append("package ").append(fqn.getParent()).append(";\n");

      // Imports
      writer.append("import ").append(ApplicationDescriptor.class.getCanonicalName()).append(";\n");

      // Open class
      writer.append("public class ").append(fqn.getIdentifier()).append(" {\n");

      // Field
      writer.append("private final ").append(APPLICATION_DESCRIPTOR).append(" descriptor;\n");

      // Constructor
      writer.append("public ").append(fqn.getIdentifier()).append("() throws Exception {\n");
      writer.append("this.descriptor = ").append(APPLICATION_DESCRIPTOR).append(".create(getClass());\n");
      writer.append("}\n");

      // Getter
      writer.append("public ").append(APPLICATION_DESCRIPTOR).append(" getDescriptor() {\n");
      writer.append("return descriptor;\n");
      writer.append("}\n");

      // Close class
      writer.append("}\n");

      //
      env.log("Generated application " + fqn + " as " + applicationFile.toUri());
    }
    catch (IOException e) {
      throw TemplateMetaModel.CANNOT_WRITE_APPLICATION.failure(e, elt, application.getName());
    }
    finally {
      Tools.safeClose(writer);
    }
  }

  @Override
  public void postProcessEvents(ModuleMetaModel metaModel) {
    context.postProcessEvents();
  }

  @Override
  public void prePassivate(ModuleMetaModel metaModel) {
    context.prePassivate();

    //
    for (ApplicationMetaModelPlugin plugin : context.getPlugins()) {
      plugin.prePassivate(metaModel);
    }

    //
    emitConfig(metaModel);
  }

  private void emitConfig(ModuleMetaModel metaModel) {
    // Application configs
    Collection<ApplicationMetaModel> applications = metaModel.getChildren(ApplicationMetaModel.class);
    if (applications != null && applications.size() > 0) {
      for (ApplicationMetaModel application : applications) {

        //
        JSON descriptor = new JSON();
        metaModel.processingContext.log("Emitting application " + application.getHandle() + " config");

        // Emit config
        for (ApplicationMetaModelPlugin plugin : context.getPlugins()) {
          JSON pluginDescriptor = plugin.getDescriptor(application);
          if (pluginDescriptor != null) {
            descriptor.set(plugin.getName(), pluginDescriptor);
          }
        }

        //
        Writer writer = null;
        try {
          FileObject fo = metaModel.processingContext.createResource(StandardLocation.CLASS_OUTPUT, application.getName(), "config.json");
          writer = fo.openWriter();
          descriptor.toString(writer, 2);
        }
        catch (IOException e) {
          throw ApplicationMetaModel.CANNOT_WRITE_APPLICATION_CONFIG.failure(e, metaModel.processingContext.get(application.getHandle()), application.getName());
        }
        finally {
          Tools.safeClose(writer);
        }
      }
    }
  }

  @Override
  public JSON getDescriptor(ModuleMetaModel metaModel) {
    JSON json = null;
    Collection<ApplicationMetaModel> applications = metaModel.getChildren(ApplicationMetaModel.class);
    if (applications != null && applications.size() > 0) {
      json = new JSON();
      for (ApplicationMetaModel application : applications) {
        json.set(application.getHandle().getPackage().toString(), new JSON());
      }
    }
    return json;
  }
}
