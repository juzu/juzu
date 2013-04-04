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

package juzu.impl.inject.spi.spring;

import org.springframework.beans.BeanMetadataAttribute;
import org.springframework.beans.factory.support.AutowireCandidateQualifier;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
abstract class AbstractBean {

  /** . */
  final Class<?> type;

  /** . */
  final List<AutowireCandidateQualifier> qualifiers;

  AbstractBean(Class<?> type, Iterable<Annotation> qualifiers) {
    List<AutowireCandidateQualifier> list = null;
    if (qualifiers != null) {
      list = new ArrayList<AutowireCandidateQualifier>();
      for (Annotation annotation : qualifiers) {
        Class<? extends Annotation> annotationType = annotation.annotationType();
        AutowireCandidateQualifier md = new AutowireCandidateQualifier(annotationType.getName());
        for (Method method : annotationType.getMethods()) {
          if (method.getParameterTypes().length == 0 && method.getDeclaringClass() != Object.class) {
            try {
              String attrName = method.getName();
              Object attrValue = method.invoke(annotation);
              md.addMetadataAttribute(new BeanMetadataAttribute(attrName, attrValue));
            }
            catch (Exception e) {
              throw new UnsupportedOperationException("handle me gracefully", e);
            }
          }
        }
        list.add(md);
      }
    }

    //
    this.type = type;
    this.qualifiers = list;
  }

  abstract void configure(String name, SpringInjector builder, DefaultListableBeanFactory factory);

}
