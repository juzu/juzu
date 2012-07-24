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

  abstract void configure(String name, SpringBuilder builder, DefaultListableBeanFactory factory);

}
