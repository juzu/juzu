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

import juzu.impl.compiler.AnnotationData;
import juzu.impl.metamodel.MetaModelEvent;
import juzu.impl.common.JSON;

import javax.lang.model.element.Element;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Set;

/**
 * A plugin for meta model processing.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public abstract class ApplicationMetaModelPlugin implements Serializable {

  /** . */
  private final String name;

  protected ApplicationMetaModelPlugin(String name) {
    this.name = name;
  }

  public final String getName() {
    return name;
  }

  public Set<Class<? extends Annotation>> getAnnotationTypes() {
    return Collections.emptySet();
  }

  public void init(ApplicationsMetaModel applications) {
  }

  public void postActivateApplicationsMetaModel(ApplicationsMetaModel applications) {
  }

  public void postActivate(ApplicationMetaModel application) {
  }

  public void processAnnotation(ApplicationMetaModel application, Element element, String fqn, AnnotationData data) {
  }

  public void postProcessAnnotations(ApplicationMetaModel application) {
  }

  public void processEvent(ApplicationsMetaModel applications, MetaModelEvent event) {
  }

  public void postProcessEvents(ApplicationMetaModel application) {
  }

  public void prePassivate(ApplicationMetaModel model) {
  }

  public void prePassivate(ApplicationsMetaModel applications) {
  }

  public void postConstruct(ApplicationMetaModel application) {
  }

  public void preDestroy(ApplicationMetaModel application) {
  }

  /**
   * Returns the plugin descriptor for the specified application or null if the plugin should not be involved at
   * runtime.
   *
   * @param application the application
   * @return the descriptor
   */
  public JSON getDescriptor(ApplicationMetaModel application) {
    return null;
  }
}
