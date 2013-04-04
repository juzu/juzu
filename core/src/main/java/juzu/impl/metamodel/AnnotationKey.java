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
import juzu.impl.compiler.ElementHandle;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.io.Serializable;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class AnnotationKey implements Serializable {

  /** . */
  final ElementHandle<?> element;

  /** . */
  final Name type;

  public AnnotationKey(Element element, AnnotationMirror mirror) {
    this.element = ElementHandle.create(element);
    this.type = Name.parse(((TypeElement)mirror.getAnnotationType().asElement()).getQualifiedName().toString());
  }

  public AnnotationKey(Element element, Name type) {
    this.element = ElementHandle.create(element);
    this.type = type;
  }

  public AnnotationKey(ElementHandle<?> element, Name type) {
    this.element = element;
    this.type = type;
  }

  public ElementHandle<?> getElement() {
    return element;
  }

  public Name getType() {
    return type;
  }

  @Override
  public int hashCode() {
    return element.hashCode() ^ type.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof AnnotationKey) {
      AnnotationKey that = (AnnotationKey)obj;
      return element.equals(that.element) && type.equals(that.type);
    }
    return false;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[annotated=" + element + ",type=" + type + "]";
  }
}
