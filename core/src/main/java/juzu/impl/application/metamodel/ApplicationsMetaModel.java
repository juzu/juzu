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

package juzu.impl.application.metamodel;

import juzu.Application;
import juzu.impl.application.ApplicationDescriptor;
import juzu.impl.compiler.ProcessingException;
import juzu.impl.compiler.ElementHandle;
import juzu.impl.compiler.ProcessingContext;
import juzu.impl.metamodel.Key;
import juzu.impl.metamodel.MetaModel;
import juzu.impl.metamodel.MetaModelContext;
import juzu.impl.metamodel.MetaModelEvent;
import juzu.impl.plugin.template.metamodel.TemplateMetaModel;
import juzu.impl.common.FQN;
import juzu.impl.common.JSON;
import juzu.impl.common.Tools;

import javax.lang.model.element.PackageElement;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ApplicationsMetaModel extends MetaModel<ApplicationsMetaModelPlugin, ApplicationsMetaModel> implements Iterable<ApplicationMetaModel> {

  /** . */
  private static final ThreadLocal<ApplicationsMetaModel> current = new ThreadLocal<ApplicationsMetaModel>();

  /** . */
  private Set<Class<? extends java.lang.annotation.Annotation>> supportedAnnotations;

  /** . */
  static final FQN APPLICATION = new FQN(Application.class);

  /** . */
  private static final String APPLICATION_DESCRIPTOR = ApplicationDescriptor.class.getSimpleName();

  /** . */
  Map<String, JSON> moduleConfig;

  /** . */
  MetaModelContext<ApplicationMetaModelPlugin, ApplicationMetaModel> applicationContext;

  public ApplicationsMetaModel() {
    this.moduleConfig = new HashMap<String, JSON>();
  }

  public void init(ProcessingContext env) {
    supportedAnnotations = new HashSet<Class<? extends java.lang.annotation.Annotation>>(context.getSupportedAnnotations());
  }

  public Set<Class<? extends java.lang.annotation.Annotation>> getSupportedAnnotations() {
    return supportedAnnotations;
  }

  @Override
  public JSON toJSON() {
    JSON json = new JSON();
    json.map("values", getChildren(ApplicationMetaModel.class));
    return json;
  }

  public Iterator<ApplicationMetaModel> iterator() {
    return getChildren(ApplicationMetaModel.class).iterator();
  }

  public ApplicationMetaModel get(ElementHandle.Package handle) {
    return getChild(Key.of(handle, ApplicationMetaModel.class));
  }

  public final void postActivate(ProcessingContext env) {
    current.set(this);
    context.postActivate(env);
  }

  public final void postProcessAnnotations() throws ProcessingException {
    context.postProcessAnnotations();
  }

  public final void processEvents() {
    context.processEvents();
  }

  public final void postProcessEvents() {
    context.postProcessEvents();
  }

  public final void prePassivate() {
    try {
      context.prePassivate();
    }
    finally {
      current.set(null);
    }
  }

  // ****

  public ApplicationMetaModel add(ElementHandle.Package handle, String applicationName) {
    ApplicationMetaModel application = new ApplicationMetaModel(handle, applicationName);
    addChild(Key.of(handle, ApplicationMetaModel.class), application);
    return application;
  }

  void resolveApplications() {
    for (ApplicationMetaModel application : getChildren(ApplicationMetaModel.class)) {
      if (application.modified) {
        queue(MetaModelEvent.createUpdated(application));
        application.modified = false;
      }
    }
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

  void emitConfig() {
    JSON descriptor = new JSON();
    descriptor.merge(moduleConfig);

    // Module config
    Writer writer = null;
    try {
      FileObject fo = env.createResource(StandardLocation.CLASS_OUTPUT, "juzu", "config.json");
      writer = fo.openWriter();
      descriptor.toString(writer, 2);
    }
    catch (IOException e) {
      throw ApplicationMetaModel.CANNOT_WRITE_CONFIG.failure(e);
    }
    finally {
      Tools.safeClose(writer);
    }

    // Application configs
    for (ApplicationMetaModel application : this) {
      descriptor.clear();

      // Emit config
      for (ApplicationMetaModelPlugin plugin : applicationContext.getPlugins()) {
        JSON pluginDescriptor = plugin.getDescriptor(application);
        if (pluginDescriptor != null) {
          descriptor.set(plugin.getName(), pluginDescriptor);
        }
      }

      //
      writer = null;
      try {
        FileObject fo = env.createResource(StandardLocation.CLASS_OUTPUT, application.getName(), "config.json");
        writer = fo.openWriter();
        descriptor.toString(writer, 2);
      }
      catch (IOException e) {
        throw ApplicationMetaModel.CANNOT_WRITE_APPLICATION_CONFIG.failure(e, env.get(application.getHandle()), application.getName());
      }
      finally {
        Tools.safeClose(writer);
      }
    }
  }

  void added(ApplicationMetaModel application) {
    applicationContext.add(application);
    moduleConfig.put(application.handle.getQN().toString(), new JSON());
    queue(MetaModelEvent.createAdded(application));
  }

  void removed(ApplicationMetaModel application) {
    applicationContext.remove(application);
    moduleConfig.remove(application.handle.getQN().toString());
    queue(MetaModelEvent.createRemoved(application));
  }
}
