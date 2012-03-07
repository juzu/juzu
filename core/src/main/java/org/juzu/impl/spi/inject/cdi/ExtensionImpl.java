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
import org.juzu.impl.utils.Tools;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessBean;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Juzu CDI extension.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class ExtensionImpl implements Extension
{

   /** . */
   private final static AnnotationLiteral<Produces> PRODUCES_ANNOTATION_LITERAL = new AnnotationLiteral<Produces>()
   {
   };

   /** . */
   private final CDIManager manager;

   /** The singletons to shut down. */
   private List<Bean<?>> singletons;

   public ExtensionImpl()
   {
      this.manager = CDIManager.boot.get();
      this.singletons = new ArrayList<Bean<?>>();
   }

   <T> void processAnnotatedType(@Observes ProcessAnnotatedType<T> pat)
   {

      AnnotatedType<T> annotatedType = pat.getAnnotatedType();
      Class<T> type = annotatedType.getJavaClass();

      // Determine if bean is a singleton bound
      boolean bound = false;
      for (AbstractBean boundBean : manager.boundBeans)
      {
         if (boundBean.getBeanClass().isAssignableFrom(type))
         {
            bound = true;
         }
      }

      //
      boolean veto;
      if (bound)
      {
         veto = true;
      }
      else
      {
         if (manager.declaredBeans.contains(type))
         {
            if (Provider.class.isAssignableFrom(type))
            {
               final AnnotatedType<T> at = pat.getAnnotatedType();

               AnnotatedType<T> providerType = new AnnotatedType<T>() {

                  public Class<T> getJavaClass()
                  {
                     return at.getJavaClass();
                  }

                  public Set<AnnotatedConstructor<T>> getConstructors()
                  {
                     return at.getConstructors();
                  }

                  public Set<AnnotatedMethod<? super T>> getMethods()
                  {
                     Set<AnnotatedMethod<? super T>> ams = at.getMethods();
                     for (final AnnotatedMethod<? super T> am : ams)
                     {
                        final AnnotatedMethod<T> a = (AnnotatedMethod<T>)am;
                        Method method = am.getJavaMember();
                        if (method.getName().equals("get") && method.getParameterTypes().length == 0)
                        {
                           Produces produces = am.getAnnotation(Produces.class);
                           if (produces == null)
                           {
                              // We need to annotate this method with @Produces for CDI
                              AnnotatedMethod<T> am2 = new AnnotatedMethod<T>()
                              {
                                 public Method getJavaMember()
                                 {
                                    return am.getJavaMember();
                                 }

                                 public List<AnnotatedParameter<T>> getParameters()
                                 {
                                    return a.getParameters();
                                 }

                                 public boolean isStatic()
                                 {
                                    return am.isStatic();
                                 }

                                 public AnnotatedType<T> getDeclaringType()
                                 {
                                    return a.getDeclaringType();
                                 }

                                 public Type getBaseType()
                                 {
                                    return am.getBaseType();
                                 }

                                 public Set<Type> getTypeClosure()
                                 {
                                    return am.getTypeClosure();
                                 }

                                 public <T extends Annotation> T getAnnotation(Class<T> annotationType)
                                 {
                                    if (annotationType == Produces.class)
                                    {
                                       return (T)PRODUCES_ANNOTATION_LITERAL;
                                    }
                                    else
                                    {
                                       return am.getAnnotation(annotationType);
                                    }
                                 }

                                 public Set<Annotation> getAnnotations()
                                 {
                                    Set<Annotation> annotations = am.getAnnotations();
                                    for (Annotation annotation : annotations)
                                    {
                                       if (annotation instanceof Produces)
                                       {
                                          return annotations;
                                       }
                                    }
                                    annotations = new HashSet<Annotation>(annotations);
                                    annotations.add(PRODUCES_ANNOTATION_LITERAL);
                                    return annotations;
                                 }

                                 public boolean isAnnotationPresent(Class<? extends Annotation> annotationType)
                                 {
                                    return Produces.class.isInstance(annotationType) || am.isAnnotationPresent(annotationType);
                                 }
                              };
                              
                              //
                              ams = new HashSet<AnnotatedMethod<? super T>>(ams);
                              ams.remove(am);
                              ams.add(am2);
                           }
                           
                           // We are done here, no need to inspect other methods
                           break;
                        }
                     }
                     return ams;
                  }

                  public Set<AnnotatedField<? super T>> getFields()
                  {
                     return at.getFields();
                  }

                  public Type getBaseType()
                  {
                     return at.getBaseType();
                  }

                  public Set<Type> getTypeClosure()
                  {
                     return at.getTypeClosure();
                  }

                  public <T extends Annotation> T getAnnotation(Class<T> annotationType)
                  {
                     return at.getAnnotation(annotationType);
                  }

                  public Set<Annotation> getAnnotations()
                  {
                     return at.getAnnotations() ;
                  }

                  public boolean isAnnotationPresent(Class<? extends Annotation> annotationType)
                  {
                     return at.isAnnotationPresent(annotationType);
                  }
               };
               
               // Use our patched type
               pat.setAnnotatedType(providerType);
            }
            
            //
            veto = false;
         }
         else
         {
            veto = manager.filter != null && !manager.filter.acceptBean(type);
         }
      }

      //
      if (veto)
      {
         pat.veto();
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
      event.addBean(new InstanceBean(InjectManager.class, Tools.set(AbstractBean.DEFAULT_QUALIFIER, AbstractBean.ANY_QUALIFIER), manager));

      // Add singletons
      for (AbstractBean bean : manager.boundBeans)
      {
         event.addBean(bean);
      }
   }

   void processBean(@Observes ProcessBean event, BeanManager beanManager)
   {
      Bean bean = event.getBean();
      manager.beans.add(bean);
      
      //
      if (bean.getScope() == Singleton.class)
      {
         singletons.add(bean);
      }
   }

   public void beforeShutdown(@Observes BeforeShutdown event, BeanManager beanManager) 
   {
      // Take care of destroying singletons
      for (Bean singleton : singletons)
      {
         CreationalContext cc = beanManager.createCreationalContext(singleton);
         Object o = beanManager.getReference(singleton, singleton.getBeanClass(), cc);
         singleton.destroy(o, cc);
      }
   }
}