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

package juzu.impl.metamodel;

import juzu.impl.common.Name;
import juzu.impl.compiler.ProcessingContext;
import juzu.impl.compiler.ProcessingException;

import javax.annotation.processing.Completion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public final class MetaModelContext<P extends MetaModelPlugin<M, P>, M extends MetaModel<P, M>>
    implements Serializable, Iterable<M> {

  /** . */
  private ProcessingContext processingContext;

  /** The meta model. */
  private ArrayList<M> metaModels;

  /** The plugins. */
  private LinkedHashMap<String, P> pluginMap;

  /** The supported annotations per plugin. */
  private HashMap<P, HashSet<Name>> supportedAnnotationsMap;

  /** All supported annotations. */
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
    this.processingContext = env;

    //
    HashMap<P, HashSet<Name>> supportedAnnotationsMap = new HashMap<P, HashSet<Name>>();
    LinkedHashMap<String, P> pluginMap = new LinkedHashMap<String, P>();
    StringBuilder msg = new StringBuilder("Using plugins:");
    for (P plugin : env.loadServices(pluginType)) {
      msg.append(" ").append(plugin.getName());
      pluginMap.put(plugin.getName(), plugin);
    }
    env.info(msg);

    // Collect processed annotations
    HashSet<Class<? extends java.lang.annotation.Annotation>> supportedAnnotations = new HashSet<Class<? extends java.lang.annotation.Annotation>>();
    for (P plugin : pluginMap.values()) {
      HashSet<Name> pluginSupportedAnnotations = new HashSet<Name>();
      for (Class<? extends Annotation> annotationType : plugin.init(env)) {
        pluginSupportedAnnotations.add(Name.create(annotationType));
        supportedAnnotations.add(annotationType);
      }
      env.info("Plugin " + plugin.getName() + " supports " + pluginSupportedAnnotations);
      supportedAnnotationsMap.put(plugin, pluginSupportedAnnotations);
    }

    //
    this.pluginMap = pluginMap;
    this.supportedAnnotationsMap = supportedAnnotationsMap;
    this.supportedAnnotations = supportedAnnotations;
  }

  public Iterator<M> iterator() {
    return metaModels.iterator();
  }

  public Set<Class<? extends java.lang.annotation.Annotation>> getSupportedAnnotations() {
    return supportedAnnotations;
  }

  public Collection<P> getPlugins() {
    return pluginMap.values();
  }

  public void add(M metaModel) {
    metaModel.processingContext = processingContext;
    metaModel.forward = true;
    metaModel.context = this;
    metaModel.init(processingContext);
    for (P plugin : pluginMap.values()) {
      plugin.init(metaModel);
    }
    metaModels.add(metaModel);
  }

  public final void postActivate(ProcessingContext processingContext) throws NullPointerException {
    this.processingContext = processingContext;
    for (M metaModel : metaModels) {
      metaModel.processingContext = processingContext;
      for (P plugin : pluginMap.values()) {
        plugin.postActivate(metaModel);
      }
    }
  }

  public void processAnnotationChange(AnnotationChange change) {

    // Update state
    if (change.getAdded() == null) {
      knownAnnotations.remove(change.getKey());
    } else {
      knownAnnotations.put(change.getKey(), change.getAdded());
    }

    //
    for (M metaModel : metaModels) {
      if (metaModel.forward) {
        metaModel.forward = false;
        for (Map.Entry<AnnotationKey, AnnotationState> annotation : knownAnnotations.entrySet()) {
          change = new AnnotationChange(annotation.getKey(), null, annotation.getValue());
          for (P plugin : pluginMap.values()) {
            HashSet<Name> supportedAnnotations = supportedAnnotationsMap.get(plugin);
            if (supportedAnnotations.contains(change.key.type)) {
              plugin.processAnnotationChange(metaModel, change);
            }
          }
        }
      } else {
        for (P plugin : pluginMap.values()) {
          HashSet<Name> supportedAnnotations = supportedAnnotationsMap.get(plugin);
          if (supportedAnnotations.contains(change.key.type)) {
            plugin.processAnnotationChange(metaModel, change);
          }
        }
      }
    }
  }

  public void processAnnotationChanges(Iterable<AnnotationChange> changes) {
    for (AnnotationChange change : changes) {
      processAnnotationChange(change);
    }
  }

  void processAnnotations(Iterable<Map.Entry<AnnotationKey, AnnotationState>> annotations) {
    ArrayList<AnnotationChange> delta = new ArrayList<AnnotationChange>();
    for (Map.Entry<AnnotationKey, AnnotationState> entry : knownAnnotations.entrySet()) {
      AnnotationKey key = entry.getKey();
      Element element = processingContext.get(key.element);
      if (element == null) {
        delta.add(new AnnotationChange(key, entry.getValue(), null));
      } else {
        AnnotationMirror found = null;
        for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
          Name f = Name.parse(((TypeElement)mirror.getAnnotationType().asElement()).getQualifiedName().toString());
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

  public Iterable<? extends Completion> getCompletions(
      AnnotationKey annotationKey,
      AnnotationState annotationState,
      String member,
      String userText) {
    Iterable<? extends Completion> completions = null;
    for (M metaModel : metaModels) {
      for (P plugin : pluginMap.values()) {
        HashSet<Name> supportedAnnotations = supportedAnnotationsMap.get(plugin);
        if (supportedAnnotations.contains(annotationKey.type)) {
          completions = plugin.getCompletions(metaModel, annotationKey, annotationState, member, userText);
          if (completions != null) {
            break;
          }
        }
      }
    }
    return completions;
  }

  public void postProcessAnnotations() throws ProcessingException {
    for (M metaModel : metaModels) {
      for (P plugin : pluginMap.values()) {
        plugin.postProcessAnnotations(metaModel);
      }
    }
  }

  public void processEvents() {
    for (M metaModel : metaModels) {
      for (P plugin : pluginMap.values()) {
        plugin.processEvents(metaModel, new EventQueue(metaModel.dispatch));
      }
      metaModel.dispatch.clear();
    }
  }

  public void postProcessEvents() {
    for (M metaModel : metaModels) {
      for (P plugin : pluginMap.values()) {
        plugin.postProcessEvents(metaModel);
      }
    }
  }

  public void prePassivate() {
    for (M metaModel : metaModels) {
      for (P plugin : pluginMap.values()) {
        plugin.prePassivate(metaModel);
      }
      metaModel.processingContext = null;
    }
    this.processingContext = null;
  }

  public void remove(M metaModel) {
    try {
      metaModel.processingContext = processingContext;

      //
      metaModels.remove(metaModel);

      // Initialize plugins
      for (P plugin : pluginMap.values()) {
        plugin.destroy(metaModel);
      }
    }
    finally {
      metaModel.processingContext = null;
    }
  }
}
