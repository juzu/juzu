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

package juzu.impl.plugin.module.metamodel;

import juzu.impl.common.Tools;
import juzu.impl.compiler.ProcessingException;
import juzu.impl.compiler.ProcessingContext;
import juzu.impl.metamodel.MetaModel;
import juzu.impl.common.JSON;
import juzu.impl.plugin.application.metamodel.ApplicationMetaModel;

import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ModuleMetaModel extends MetaModel<ModuleMetaModelPlugin, ModuleMetaModel> {

  /** . */
  private static final ThreadLocal<ModuleMetaModel> current = new ThreadLocal<ModuleMetaModel>();

  /** . */
  private Set<Class<? extends java.lang.annotation.Annotation>> supportedAnnotations;

  public ModuleMetaModel() {
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
    for (ModuleMetaModelPlugin plugin : context.getPlugins()) {
      JSON pluginJSON = plugin.toJSON(this);
      json.set(plugin.getName(), pluginJSON);
    }
    return json;
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

    //
    ProcessingContext env = this.env;

    //
    emitConfig(env);

    //
    try {
      context.prePassivate();
    }
    finally {
      current.set(null);
    }
  }

  private void emitConfig(ProcessingContext env) {
    env.log("Emitting module config");

    // Module config
    JSON descriptor = new JSON();
    for (ModuleMetaModelPlugin plugin : context.getPlugins()) {
      JSON pluginDesc = plugin.getDescriptor(this);
      if (pluginDesc != null) {
        descriptor.set(plugin.getName(), pluginDesc);
      }
    }
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
  }
}