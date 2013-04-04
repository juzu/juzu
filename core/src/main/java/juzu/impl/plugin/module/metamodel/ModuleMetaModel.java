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
    ProcessingContext env = this.processingContext;

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

    // Merge plugins
    JSON descriptor = null;
    for (ModuleMetaModelPlugin plugin : context.getPlugins()) {
      JSON pluginDesc = plugin.getDescriptor(this);
      if (pluginDesc != null) {
        if (descriptor == null) {
          descriptor = new JSON();
        }
        descriptor.set(plugin.getName(), pluginDesc);
      }
    }

    // Emit descriptor
    if (descriptor != null) {
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
}