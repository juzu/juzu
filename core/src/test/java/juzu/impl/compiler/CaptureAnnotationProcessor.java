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

package juzu.impl.compiler;

import juzu.impl.metamodel.AnnotationState;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
@SupportedSourceVersion(SourceVersion.RELEASE_6)
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
