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
