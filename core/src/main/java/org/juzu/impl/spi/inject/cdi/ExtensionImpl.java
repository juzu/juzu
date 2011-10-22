/*
 * Copyright (C) 2011 eXo Platform SAS.
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

package org.juzu.impl.spi.inject.cdi;

import org.juzu.impl.inject.Export;
import org.juzu.impl.request.Scope;
import org.juzu.impl.spi.inject.InjectManager;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Juzu CDI extension.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class ExtensionImpl implements Extension
{

   /** . */
   private final CDIManager manager;

   public ExtensionImpl()
   {
      this.manager = CDIManager.boot.get();
   }

   <T> void processAnnotatedType(@Observes ProcessAnnotatedType<T> pat)
   {
      AnnotatedType<T> annotatedType = pat.getAnnotatedType();
      Class<?> type = annotatedType.getJavaClass();
      if (type.getName().startsWith("org.juzu."))
      {
         boolean present = annotatedType.isAnnotationPresent(Export.class);
         if (!present)
         {
            pat.veto();
         }
      }
   }

   void afterBeanDiscovery(@Observes AfterBeanDiscovery event, BeanManager beanManager)
   {
      Container container = Container.boot.get();

      //
      for (Scope scope : container.scopes)
      {
         event.addContext(new ContextImpl(container.scopeController, scope, scope.getAnnotationType()));
      }

      // Add the manager
      event.addBean(bean(InjectManager.class, manager));

      // Add singletons
      for (Map.Entry<Class<?>, Object> entry : manager.singletons.entrySet())
      {
         event.addBean(bean(entry.getKey(), entry.getValue()));
      }
   }

   private Bean bean(final Class type, final Object instance)
   {
      return new Bean()
      {
         public Set<Type> getTypes()
         {
            Set<Type> types = new HashSet<Type>();
            types.add(type);
            types.add(Object.class);
            return types;
         }

         public Set<Annotation> getQualifiers()
         {
            Set<Annotation> qualifiers = new HashSet<Annotation>();
            qualifiers.add( new AnnotationLiteral<Default>() {} );
            qualifiers.add( new AnnotationLiteral<Any>() {} );
            return qualifiers;
         }

         public Class<? extends Annotation> getScope()
         {
            return Singleton.class;
         }

         public String getName()
         {
            return null;
         }

         public Set<Class<? extends Annotation>> getStereotypes()
         {
            return Collections.emptySet();
         }

         public Class<?> getBeanClass()
         {
            return type;
         }

         public boolean isAlternative()
         {
            return false;
         }

         public boolean isNullable()
         {
            return false;
         }

         public Set<InjectionPoint> getInjectionPoints()
         {
            return Collections.emptySet();
         }

         public Object create(CreationalContext tCreationalContext)
         {
            return instance;
         }

         public void destroy(Object instance, CreationalContext ctx)
         {
            ctx.release();
         }
      };
   }
}
