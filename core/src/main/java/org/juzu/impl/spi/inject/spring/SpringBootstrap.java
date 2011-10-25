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

package org.juzu.impl.spi.inject.spring;

import org.juzu.impl.inject.ScopeController;
import org.juzu.impl.request.Scope;
import org.juzu.impl.spi.fs.ReadFileSystem;
import org.juzu.impl.spi.inject.InjectBootstrap;
import org.juzu.impl.spi.inject.InjectManager;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.annotation.CustomAutowireConfigurer;
import org.springframework.beans.factory.annotation.QualifierAnnotationAutowireCandidateResolver;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.ScopeMetadata;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class SpringBootstrap extends InjectBootstrap
{

   /** . */
   private DefaultListableBeanFactory factory = new DefaultListableBeanFactory();

   /** . */
   private ScopeMetadataResolverImpl resolver = new ScopeMetadataResolverImpl();

   /** . */
   private ClassLoader classLoader;

   public SpringBootstrap()
   {
   }

   @Override
   public <T> InjectBootstrap declareBean(Class<T> type, Class<? extends T> implementationType)
   {
      if (implementationType == null)
      {
         implementationType = type;
      }

      //
      AnnotatedGenericBeanDefinition definition = new AnnotatedGenericBeanDefinition(implementationType);
      ScopeMetadata scopeMD = resolver.resolveScopeMetadata(definition);
      if (scopeMD != null)
      {
         definition.setScope(scopeMD.getScopeName());
      }

      //
      String name = "" + Math.random();
      for (Annotation annotation : implementationType.getDeclaredAnnotations())
      {
         if (annotation instanceof Named)
         {
            Named named = (Named)annotation;
            name = named.value();
            break;
         }
      }
      factory.registerBeanDefinition(name, definition);
      return this;
   }

   @Override
   public <T> InjectBootstrap declareProvider(Class<T> type, Class<? extends Provider<T>> provider)
   {
      return this;
   }

   @Override
   public <T> InjectBootstrap bindSingleton(Class<T> type, T instance)
   {
      String name = "" + Math.random();
      factory.registerSingleton(name, instance);
      return this;
   }

   @Override
   public <P> InjectBootstrap addFileSystem(ReadFileSystem<P> fs)
   {
      return this;
   }

   @Override
   public InjectBootstrap addScope(Scope scope)
   {
      return this;
   }

   @Override
   public InjectBootstrap setClassLoader(ClassLoader classLoader)
   {
      this.classLoader = classLoader;
      return this;
   }

   @Override
   public <B, I> InjectManager<B, I> create() throws Exception
   {
      // Register scopes
      for (Scope scope : Scope.values())
      {
         factory.registerScope(scope.name().toLowerCase(), new SpringScope(scope, ScopeController.INSTANCE));
      }

      //
      AutowiredAnnotationBeanPostProcessor beanPostProcessor = new AutowiredAnnotationBeanPostProcessor();
      beanPostProcessor.setAutowiredAnnotationType(Inject.class);
      beanPostProcessor.setBeanFactory(factory);
      factory.addBeanPostProcessor(beanPostProcessor);

      //
      Set cqt = new HashSet();
      cqt.add(Named.class);
      CustomAutowireConfigurer configurer = new CustomAutowireConfigurer();
      configurer.setCustomQualifierTypes(cqt);
      QualifierAnnotationAutowireCandidateResolver customResolver = new QualifierAnnotationAutowireCandidateResolver();
      factory.setAutowireCandidateResolver(customResolver);
      configurer.postProcessBeanFactory(factory);

      //


      //
      return (InjectManager<B, I>)new SpringManager(factory, classLoader);
   }
}
