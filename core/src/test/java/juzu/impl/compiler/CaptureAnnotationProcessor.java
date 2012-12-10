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

package juzu.impl.compiler;

import juzu.impl.metamodel.AnnotationState;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class CaptureAnnotationProcessor extends AbstractProcessor {

  /** . */
  final HashMap<ElementHandle<?>, HashMap<String, AnnotationState>> statesMap = new HashMap<ElementHandle<?>,HashMap<String, AnnotationState>>();

  /** . */
  final HashSet<String> annotationTypes = new HashSet<String>();

  public CaptureAnnotationProcessor with(Class<? extends Annotation> annotationType) {
    annotationTypes.add(annotationType.getName());
    return this;
  }

  public AnnotationState get(ElementHandle<?> element, Class<?> annotationType) {
    HashMap<String, AnnotationState> annotations = statesMap.get(element);
    return annotations != null ? annotations.get(annotationType.getName()) : null;
  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return annotationTypes;
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    for (TypeElement annotationTypeElement : annotations) {
      for (Element element : roundEnv.getElementsAnnotatedWith(annotationTypeElement)) {
        ElementHandle<?> key = ElementHandle.create(element);
        HashMap<String, AnnotationState> states = statesMap.get(key);
        if (states == null) {
          statesMap.put(key, states = new HashMap<String, AnnotationState>());
        }
        states.put(annotationTypeElement.asType().toString(), AnnotationState.get(element, annotationTypeElement.asType()));
      }
    }
    return false;
  }
}
