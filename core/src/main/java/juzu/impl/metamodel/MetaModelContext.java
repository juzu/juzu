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

package juzu.impl.metamodel;

import juzu.impl.compiler.ProcessingContext;
import juzu.impl.compiler.ProcessingException;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public final class MetaModelContext<P extends MetaModelPlugin<M, P>, M extends MetaModel<P, M>>
    implements Serializable, Iterable<M> {

  /** . */
  private ProcessingContext env;

  /** The meta model. */
  private ArrayList<M> metaModels;

  /** The meta model plugins. */
  private final LinkedHashMap<String, P> plugins = new LinkedHashMap<String, P>();

  /** . */
  private Set<Class<? extends java.lang.annotation.Annotation>> supportedAnnotations;

  /** . */
  private final Class<P> pluginType;

  public MetaModelContext(Class<P> pluginType) {
    this.pluginType = pluginType;
    this.metaModels = new ArrayList<M>();
  }

  public void init(ProcessingContext env) throws NullPointerException {
//    if (env == null) {
//      throw new NullPointerException("No null env accepted");
//    }

    //
    this.env = env;

    //
    LinkedHashMap<String, P> plugins = new LinkedHashMap<String, P>();
    StringBuilder msg = new StringBuilder("Using plugins:");
    for (P plugin : env.loadServices(pluginType)) {
      msg.append(" ").append(plugin.getName());
      plugins.put(plugin.getName(), plugin);
    }
    env.log(msg);

    // Collect processed annotations
    HashSet<Class<? extends java.lang.annotation.Annotation>> supportedAnnotations = new HashSet<Class<? extends Annotation>>();
    for (P plugin : plugins.values()) {
      Set<Class<? extends java.lang.annotation.Annotation>> processed = plugin.init(env);
      env.log("Plugin " + plugin.getName() + " wants to process " + processed);
      supportedAnnotations.addAll(processed);
    }

    //
    this.plugins.putAll(plugins);
    this.supportedAnnotations = supportedAnnotations;
  }

  public Iterator<M> iterator() {
    return metaModels.iterator();
  }

  public Set<Class<? extends java.lang.annotation.Annotation>> getSupportedAnnotations() {
    return supportedAnnotations;
  }

  public Collection<P> getPlugins() {
    return plugins.values();
  }

  public void add(M metaModel) {
    metaModel.env = env;
    for (P plugin : plugins.values()) {
      plugin.init(metaModel);
    }
    metaModels.add(metaModel);
  }

  public final void postActivate(ProcessingContext env) throws NullPointerException {
//    if (env == null) {
//      throw new NullPointerException("No null env accepted");
//    }

    //
    this.env = env;
    for (M metaModel : metaModels) {
      metaModel.env = env;
      for (P plugin : plugins.values()) {
        plugin.postActivate(metaModel);
      }
    }
  }

  public void postProcessAnnotations() throws ProcessingException {
    for (M metaModel : metaModels) {
      for (P plugin : plugins.values()) {
        plugin.postProcessAnnotations(metaModel);
      }
    }
  }

  public void processEvents() {
    for (M metaModel : metaModels) {
      for (P plugin : plugins.values()) {
        plugin.processEvents(metaModel, new EventQueue(metaModel.dispatch));
      }
      metaModel.dispatch.clear();
    }
  }

  public void postProcessEvents() {
    for (M metaModel : metaModels) {
      for (P plugin : plugins.values()) {
        plugin.postProcessEvents(metaModel);
      }
    }
  }

  public void prePassivate() {
    for (M metaModel : metaModels) {
      for (P plugin : plugins.values()) {
        plugin.prePassivate(metaModel);
      }
      metaModel.env = null;
    }
    this.env = null;
  }

  public void remove(M metaModel) {
    try {
      metaModel.env = env;

      //
      metaModels.remove(metaModel);

      // Initialize plugins
      for (P plugin : plugins.values()) {
        plugin.destroy(metaModel);
      }
    }
    finally {
      metaModel.env = null;
    }
  }
}
