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

import juzu.impl.common.JSON;
import juzu.impl.compiler.ProcessingContext;

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

  public void processAnnotationChanges(M metaModel, Iterable<AnnotationChange> changes) {
    for (AnnotationChange change : changes) {
      processAnnotationChange(metaModel, change.key, change.removed, change.added);
    }
  }

  public void processAnnotationChange(M metaModel, AnnotationKey key, AnnotationState removed, AnnotationState added) {
    if (added == null) {
      processAnnotationRemoved(metaModel, key, removed);
    } else if (removed == null) {
      processAnnotationAdded(metaModel, key, added);
    } else if (!removed.equals(added)) {
      processAnnotationUpdated(metaModel, key, removed, added);
    }
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
