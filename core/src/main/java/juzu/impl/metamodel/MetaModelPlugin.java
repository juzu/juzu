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

import juzu.impl.compiler.Annotation;
import juzu.impl.common.JSON;

import javax.lang.model.element.Element;
import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class MetaModelPlugin implements Serializable {

  /** The plugin name. */
  private final String name;

  protected MetaModelPlugin(String name) {
    this.name = name;
  }

  public final String getName() {
    return name;
  }

  /**
   * Returns the plugin descriptor or null.
   *
   * @param metaModel the meta model instance
   * @return the descriptor
   */
  public JSON getDescriptor(MetaModel metaModel) {
    return null;
  }

  /**
   * Returns a JSON representation mainly for testing purposes.
   *
   * @param metaModel the meta model instance
   * @return the json representation
   */
  public JSON toJSON(MetaModel metaModel) {
    return null;
  }

  public Set<Class<? extends java.lang.annotation.Annotation>> getAnnotationTypes() {
    return Collections.emptySet();
  }

  public void init(MetaModel metaModel) {
  }

  public void postActivate(MetaModel metaModel) {
  }

  public void processAnnotation(MetaModel metaModel, Element element, Annotation annotation) {
  }

  public void postProcessAnnotations(MetaModel metaModel) {
  }

  public void processEvents(MetaModel metaModel, EventQueue queue) {
  }

  public void postProcessEvents(MetaModel metaModel) {
  }

  public void prePassivate(MetaModel metaModel) {
  }
}
