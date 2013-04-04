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
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.AnnotationScopeMetadataResolver;
import org.springframework.context.annotation.ScopeMetadata;

import javax.inject.Singleton;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ScopeMetadataResolverImpl extends AnnotationScopeMetadataResolver {

  /** . */
  private final Set<Scope> scopes;

  public ScopeMetadataResolverImpl(Set<Scope> scopes) {
    this.scopes = scopes;
  }

  @Override
  public ScopeMetadata resolveScopeMetadata(BeanDefinition definition) {
    ScopeMetadata metadata = new ScopeMetadata();
    if (definition instanceof AnnotatedBeanDefinition) {
      AnnotatedBeanDefinition annDef = (AnnotatedBeanDefinition)definition;
      Set<String> annotationTypes = annDef.getMetadata().getAnnotationTypes();

      //
      String scopeName;
      if (annotationTypes.contains(Singleton.class.getName())) {
        scopeName = "singleton";
      }
      else {
        scopeName = "prototype";
        for (Scope scope : scopes) {
          if (annotationTypes.contains(scope.getAnnotationType().getName())) {
            scopeName = scope.name().toLowerCase();
            break;
          }
        }
      }
      metadata.setScopeName(scopeName);
      return metadata;
    }
    else {
      return super.resolveScopeMetadata(definition);
    }
  }
}
