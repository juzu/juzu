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

import juzu.Scope;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.support.AutowireCandidateQualifier;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.ScopeMetadata;

import java.lang.annotation.Annotation;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class DeclaredBean extends AbstractBean {

  /** . */
  private final Scope scope;

  DeclaredBean(Class<?> type, Scope scope, Iterable<Annotation> qualifiers) {
    super(type, qualifiers);

    //
    this.scope = scope;
  }

  @Override
  void configure(String name, SpringInjector builder, DefaultListableBeanFactory factory) {
    AnnotatedGenericBeanDefinition definition = new AnnotatedGenericBeanDefinition(type);

    //
    if (scope != null) {
      definition.setScope(scope.name().toLowerCase());
    }
    else {
      ScopeMetadata scopeMD = builder.scopeResolver.resolveScopeMetadata(definition);
      if (scopeMD != null) {
        definition.setScope(scopeMD.getScopeName());
      }
    }

    //
    if (qualifiers != null) {
      for (AutowireCandidateQualifier qualifier : qualifiers) {
        definition.addQualifier(qualifier);
      }
    }

    //
    factory.registerBeanDefinition(name, definition);
  }
}
