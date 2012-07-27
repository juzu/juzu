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

import juzu.impl.common.FQN;
import juzu.impl.compiler.ProcessingContext;
import juzu.impl.compiler.ProcessingException;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
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

  /** All known annotations. */
  final LinkedHashMap<AnnotationKey, AnnotationState> knownAnnotations = new LinkedHashMap<AnnotationKey, AnnotationState>();

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
    HashSet<Class<? extends java.lang.annotation.Annotation>> supportedAnnotations = new HashSet<Class<? extends java.lang.annotation.Annotation>>();
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
    metaModel.forward = true;
    metaModel.context = this;
    metaModel.init(env);
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

  public void processAnnotationChanges(Iterable<AnnotationChange> delta) {
    //
    for (AnnotationChange change : delta) {
      if (change.getAdded() == null) {
        knownAnnotations.remove(change.getKey());
      } else {
        knownAnnotations.put(change.getKey(), change.getAdded());
      }
    }

    //
    ArrayList<AnnotationChange> all = new ArrayList<AnnotationChange>();
    for (Map.Entry<AnnotationKey, AnnotationState> annotation : knownAnnotations.entrySet()) {
      all.add(new AnnotationChange(annotation.getKey(), null, annotation.getValue()));
    }

    //
    for (M metaModel : metaModels) {
      Iterable<AnnotationChange> changes;
      if (metaModel.forward) {
        metaModel.forward = false;
        changes = all;
      } else {
        changes = delta;
      }
      for (P plugin : plugins.values()) {
        plugin.processAnnotationChanges(metaModel, changes);
      }
    }
  }

  void processAnnotations(Iterable<Map.Entry<AnnotationKey, AnnotationState>> annotations) {
    ArrayList<AnnotationChange> delta = new ArrayList<AnnotationChange>();
    for (Map.Entry<AnnotationKey, AnnotationState> entry : knownAnnotations.entrySet()) {
      AnnotationKey key = entry.getKey();
      Element element = env.get(key.element);
      if (element == null) {
        delta.add(new AnnotationChange(key, entry.getValue(), null));
      } else {
        AnnotationMirror found = null;
        for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
          FQN f = new FQN(((TypeElement)mirror.getAnnotationType().asElement()).getQualifiedName().toString());
          if (key.getType().equals(f)) {
            found = mirror;
            break;
          }
        }
        if (found == null) {
          delta.add(new AnnotationChange(key, entry.getValue(), null));
        }
      }
    }
    for (Map.Entry<AnnotationKey, AnnotationState> annotation : annotations) {
      delta.add(new AnnotationChange(annotation.getKey(), knownAnnotations.get(annotation.getKey()), annotation.getValue()));
    }
    processAnnotationChanges(delta);
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
