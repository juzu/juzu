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

package juzu.impl.plugin.application.metamodel;

import juzu.Application;
import juzu.impl.plugin.application.descriptor.ApplicationDescriptor;
import juzu.impl.common.FQN;
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

import javax.lang.model.element.PackageElement;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ApplicationModuleMetaModelPlugin extends ModuleMetaModelPlugin {

  /** . */
  private static final FQN APPLICATION = new FQN(Application.class);

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
    context.postActivate(metaModel.env);
    for (ApplicationMetaModel application : metaModel.getChildren(ApplicationMetaModel.class)) {
      for (ApplicationMetaModelPlugin plugin : context.getPlugins()) {
        plugin.postActivate(application);
      }
    }
  }

  @Override
  public void processAnnotationChanges(ModuleMetaModel metaModel, Iterable<AnnotationChange> changes) {

    // Normal processing for now
    super.processAnnotationChanges(metaModel, changes);

    // Forward
    context.processAnnotationChanges(changes);
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
          emitApplication(metaModel.env, application);
        }
      }
    }

    // Distribute per application nevents
    context.processEvents();
  }

  void emitApplication(ProcessingContext env, ApplicationMetaModel application) throws ProcessingException {
    PackageElement elt = env.get(application.getHandle());
    FQN fqn = new FQN(application.getName(), "Application");

    //
    Writer writer = null;
    try {
      JavaFileObject applicationFile = env.createSourceFile(fqn, elt);
      writer = applicationFile.openWriter();

      writer.append("package ").append(fqn.getPackageName()).append(";\n");

      // Imports
      writer.append("import ").append(Tools.getImport(ApplicationDescriptor.class)).append(";\n");

      // Open class
      writer.append("public class ").append(fqn.getSimpleName()).append(" {\n");

      // Singleton
      writer.append("public static final ").append(APPLICATION_DESCRIPTOR).append(" DESCRIPTOR = new ").append(APPLICATION_DESCRIPTOR).append("(");
      writer.append(fqn.getSimpleName()).append(".class");
      writer.append(");\n");

      // Close class
      writer.append("}\n");

      //
      env.log("Generated application " + fqn.getName() + " as " + applicationFile.toUri());
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
    JSON descriptor = new JSON();

    // Application configs
    for (ApplicationMetaModel application : metaModel.getChildren(ApplicationMetaModel.class)) {

      //
      metaModel.env.log("Emitting application " + application.getHandle() + " config");

      // Recycle
      descriptor.clear();

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
        FileObject fo = metaModel.env.createResource(StandardLocation.CLASS_OUTPUT, application.getName(), "config.json");
        writer = fo.openWriter();
        descriptor.toString(writer, 2);
      }
      catch (IOException e) {
        throw ApplicationMetaModel.CANNOT_WRITE_APPLICATION_CONFIG.failure(e, metaModel.env.get(application.getHandle()), application.getName());
      }
      finally {
        Tools.safeClose(writer);
      }
    }
  }

  @Override
  public JSON getDescriptor(ModuleMetaModel metaModel) {
    JSON json = new JSON();
    for (ApplicationMetaModel application : metaModel.getChildren(ApplicationMetaModel.class)) {
      json.set(application.getHandle().getPackage().toString(), new JSON());
    }
    return json;
  }
}
