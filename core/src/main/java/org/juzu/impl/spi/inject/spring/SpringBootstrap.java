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
import org.juzu.impl.spi.inject.InjectBuilder;
import org.juzu.impl.spi.inject.InjectManager;
import org.springframework.beans.BeanMetadataAttribute;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.annotation.CustomAutowireConfigurer;
import org.springframework.beans.factory.annotation.QualifierAnnotationAutowireCandidateResolver;
import org.springframework.beans.factory.support.AutowireCandidateQualifier;
import org.springframework.beans.factory.support.CglibSubclassingInstantiationStrategy;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.context.annotation.CommonAnnotationBeanPostProcessor;
import org.springframework.context.annotation.ScopeMetadata;
import org.springframework.core.io.UrlResource;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class SpringBootstrap extends InjectBuilder
{

   /** . */
   private ClassLoader classLoader;

   /** . */
   private Map<String, Class<?>> beans = new LinkedHashMap<String, Class<?>>();

   /** . */
   private Map<String, SingletonBean> singletons = new LinkedHashMap<String, SingletonBean>();

   /** . */
   private Set<Scope> scopes = new LinkedHashSet<Scope>();

   /** . */
   private URL configurationURL;

   @Override
   public <T> InjectBuilder declareBean(Class<T> type, Class<? extends T> implementationType)
   {
      if (implementationType == null)
      {
         implementationType = type;
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

      //
      beans.put(name, implementationType);
      return this;
   }

   public URL getConfigurationURL()
   {
      return configurationURL;
   }

   public void setConfigurationURL(URL configurationURL)
   {
      this.configurationURL = configurationURL;
   }

   @Override
   public <T> InjectBuilder declareProvider(Class<T> type, Class<? extends Provider<T>> provider)
   {
      // todo
      return this;
   }

   @Override
   public <T> InjectBuilder bindBean(Class<T> type, Iterable<Annotation> qualifiers, T instance)
   {
      String name = "" + Math.random();
      List<AutowireCandidateQualifier> list = null;
      if (qualifiers != null)
      {
         list = new ArrayList<AutowireCandidateQualifier>();
         for (Annotation annotation : qualifiers)
         {
            Class<? extends Annotation> annotationType = annotation.annotationType();
            AutowireCandidateQualifier md = new AutowireCandidateQualifier(annotationType.getName());
            for (Method method : annotationType.getMethods())
            {
               if (method.getParameterTypes().length == 0 && method.getDeclaringClass() != Object.class)
               {
                  try
                  {
                     String attrName = method.getName();
                     Object attrValue = method.invoke(annotation);
                     md.addMetadataAttribute(new BeanMetadataAttribute(attrName, attrValue));
                  }
                  catch (Exception e)
                  {
                     throw new UnsupportedOperationException("handle me gracefully", e);
                  }
               }
            }
            list.add(md);
         }
      }
      singletons.put(name, new SingletonBean(instance, list));
      return this;
   }

   @Override
   public <T> InjectBuilder bindProvider(Class<T> type, Provider<T> provider)
   {
      return bindBean(ProviderFactory.class, null, new ProviderFactory<T>(type, provider));
   }

   @Override
   public <P> InjectBuilder addFileSystem(ReadFileSystem<P> fs)
   {
      return this;
   }

   @Override
   public InjectBuilder addScope(Scope scope)
   {
      scopes.add(scope);
      return this;
   }

   @Override
   public InjectBuilder setClassLoader(ClassLoader classLoader)
   {
      this.classLoader = classLoader;
      return this;
   }

   @Override
   public <B, I> InjectManager<B, I> create() throws Exception
   {
      DefaultListableBeanFactory factory;
      if (configurationURL != null)
      {
         factory = new XmlBeanFactory(new UrlResource(configurationURL));
      }
      else
      {
         factory = new DefaultListableBeanFactory();
      }

      //
      factory.setBeanClassLoader(classLoader);
      factory.setInstantiationStrategy(new SingletonInstantiationStrategy(new CglibSubclassingInstantiationStrategy(), singletons));

      // Register scopes
      for (Scope scope : scopes)
      {
         factory.registerScope(scope.name().toLowerCase(), new SpringScope(factory, scope, ScopeController.INSTANCE));
      }

      // Bind singletons with associated factories
      for (Map.Entry<String, SingletonBean> entry : singletons.entrySet())
      {
         String name = entry.getKey();
         SingletonBean bean = entry.getValue();
         AnnotatedGenericBeanDefinition definition = new AnnotatedGenericBeanDefinition(bean.instance.getClass());
         definition.setScope("singleton");
         if (bean.qualifiers != null)
         {
            for (AutowireCandidateQualifier qualifier : bean.qualifiers)
            {
               definition.addQualifier(qualifier);
            }
         }
         factory.registerBeanDefinition(name, definition);
      }

      //
      ScopeMetadataResolverImpl resolver = new ScopeMetadataResolverImpl(scopes);
      for (Map.Entry<String, Class<?>> entry : beans.entrySet())
      {
         AnnotatedGenericBeanDefinition definition = new AnnotatedGenericBeanDefinition(entry.getValue());
         ScopeMetadata scopeMD = resolver.resolveScopeMetadata(definition);
         if (scopeMD != null)
         {
            definition.setScope(scopeMD.getScopeName());
         }
         factory.registerBeanDefinition(entry.getKey(), definition);
      }

      //
      AutowiredAnnotationBeanPostProcessor beanPostProcessor = new AutowiredAnnotationBeanPostProcessor();
      beanPostProcessor.setAutowiredAnnotationType(Inject.class);
      beanPostProcessor.setBeanFactory(factory);
      factory.addBeanPostProcessor(beanPostProcessor);

      //
      CommonAnnotationBeanPostProcessor commonAnnotationBeanProcessor = new CommonAnnotationBeanPostProcessor();
      factory.addBeanPostProcessor(commonAnnotationBeanProcessor);

      //
      Set cqt = new HashSet();
      cqt.add(Named.class);
      CustomAutowireConfigurer configurer = new CustomAutowireConfigurer();
      configurer.setCustomQualifierTypes(cqt);
      QualifierAnnotationAutowireCandidateResolver customResolver = new QualifierAnnotationAutowireCandidateResolver();
      factory.setAutowireCandidateResolver(customResolver);
      configurer.postProcessBeanFactory(factory);

      //
      return (InjectManager<B, I>)new SpringManager(factory, classLoader);
   }
}
