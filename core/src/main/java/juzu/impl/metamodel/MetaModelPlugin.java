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

import juzu.impl.common.JSON;
import juzu.impl.compiler.ProcessingContext;

import javax.annotation.processing.Completion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

/**
 * A plugin for a meta model object.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class MetaModelPlugin<M extends MetaModel<P, M>, P extends MetaModelPlugin<M, P>> implements Serializable {

  /** The plugin name. */
  private final String name;

  protected MetaModelPlugin(String name) {
    this.name = name;
  }

  public final String getName() {
    return name;
  }

  public Set<Class<? extends java.lang.annotation.Annotation>> init(ProcessingContext env) {
    return Collections.emptySet();
  }

  public void init(M metaModel) {
  }

  public void postActivate(M metaModel) {
  }

  public void processAnnotationChange(M metaModel, AnnotationChange change) {
    if (change.added == null) {
      processAnnotationRemoved(metaModel, change.key, change.removed);
    } else if (change.removed == null) {
      processAnnotationAdded(metaModel, change.key, change.added);
    } else if (!change.removed.equals(change.added)) {
      processAnnotationUpdated(metaModel, change.key, change.removed, change.added);
    }
  }

  public Iterable<? extends Completion> getCompletions(
      M metaModel,
      AnnotationKey annotationKey,
      AnnotationState annotationState,
      String member,
      String userText) {
    return null;
  }

  public void processAnnotationAdded(M metaModel, AnnotationKey key, AnnotationState added) {
  }

  public void processAnnotationUpdated(M metaModel, AnnotationKey key, AnnotationState removed, AnnotationState added) {
    processAnnotationRemoved(metaModel, key, removed);
    processAnnotationAdded(metaModel, key, added);
  }

  public void processAnnotationRemoved(M metaModel, AnnotationKey key, AnnotationState removed) {
  }

  public void postProcessAnnotations(M metaModel) {
  }

  public void processEvents(M metaModel, EventQueue queue) {
  }

  public void postProcessEvents(M metaModel) {
  }

  public void prePassivate(M metaModel) {
  }

  public void destroy(M metaModel) {
  }

  /**
   * Returns the plugin descriptor or null.
   *
   * @param metaModel the meta model instance
   * @return the descriptor
   */
  public JSON getDescriptor(M metaModel) {
    return null;
  }
}
