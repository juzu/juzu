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

package juzu.impl.inject.spi.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Binding;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.ProvisionException;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.AnnotatedBindingBuilder;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.binder.ScopedBindingBuilder;
import com.google.inject.internal.BindingImpl;
import com.google.inject.internal.Scoping;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Named;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import juzu.Scope;
import juzu.impl.inject.ScopeController;
import juzu.impl.inject.spi.InjectImplementation;
import juzu.impl.inject.spi.InjectionContext;

import javax.annotation.PreDestroy;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class GuiceContext extends InjectionContext<GuiceBean, Object> {

  /** . */
  private Injector injector;

  /** . */
  private final ClassLoader classLoader;

  /** . */
  private final Map<String, Key<?>> nameMap;

  public GuiceContext(final GuiceBuilder bootstrap) {

    AbstractModule module = new AbstractModule() {
      @Override
      protected void configure() {
        //
        bindListener(Matchers.any(), new TypeListener() {
          public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
            encounter.register(new PostConstructInjectionListener());
          }
        });

        // Bind guice scopes
        for (Scope scope : bootstrap.scopes) {
          if (!scope.isBuiltIn()) {
            bindScope(scope.getAnnotationType(), new GuiceScope(scope, ScopeController.INSTANCE));
          }
        }

        // Bind beans
        for (BeanBinding<?> beanBinding : bootstrap.bindings) {
          AnnotatedBindingBuilder a = bind(beanBinding.type);
          LinkedBindingBuilder b;

          if (beanBinding.qualifiers != null && beanBinding.qualifiers.size() > 0) {
            Iterator<Annotation> i = beanBinding.qualifiers.iterator();
            // Construction to make compiler happy
            b = a.annotatedWith(i.next());
            while (i.hasNext()) {
              b = a.annotatedWith(i.next());
            }
          }
          else {
            b = a;
          }

          //
          ScopedBindingBuilder c;
          if (beanBinding instanceof BeanBinding.ToInstance) {
            BeanBinding.ToInstance d = (BeanBinding.ToInstance)beanBinding;
            b.toInstance(d.instance);
            c = b;
          }
          else if (beanBinding instanceof BeanBinding.ToProviderInstance) {
            BeanBinding.ToProviderInstance d = (BeanBinding.ToProviderInstance)beanBinding;
            c = b.toProvider(d);
            if (beanBinding.scopeType != null) {
              c.in(beanBinding.scopeType);
            }
          }
          else {
            if (beanBinding instanceof BeanBinding.ToProviderType) {
              BeanBinding.ToProviderType d = (BeanBinding.ToProviderType)beanBinding;
              c = b.toProvider(d.provider);
            }
            else {
              BeanBinding.ToType d = (BeanBinding.ToType)beanBinding;
              if (d.qualifiers != null) {
                if (d.implementationType != null) {
                  c = b.to(d.implementationType);
                }
                else {
                  c = b.to(beanBinding.type);
                }
              }
              else {
                if (d.implementationType != null) {
                  c = b.to(d.implementationType);
                }
                else {
                  c = b;
                }
              }
            }
            if (beanBinding.scopeType != null) {
              c.in(beanBinding.scopeType);
            }
          }
        }

        // Bind the manager itself
        bind(InjectionContext.class).toInstance(GuiceContext.this);
      }

      private <T> void bind(Class<T> clazz, T instance) {
        bind(clazz).toInstance(instance);
      }
    };

    //
    Map<String, Key<?>> nameMap = new HashMap<String, Key<?>>();
    Injector injector = Guice.createInjector(module);
    for (Key<?> key : injector.getBindings().keySet()) {
      Class<? extends Annotation> annotationType = key.getAnnotationType();
      if (annotationType != null && Named.class.isAssignableFrom(annotationType)) {
        Named named = (Named)key.getAnnotation();
        nameMap.put(named.value(), key);
      }
    }

    //
    this.injector = injector;
    this.nameMap = nameMap;
    this.classLoader = bootstrap.classLoader;
  }

  public InjectImplementation getImplementation() {
    return InjectImplementation.INJECT_GUICE;
  }

  public ClassLoader getClassLoader() {
    return classLoader;
  }

  public GuiceBean resolveBean(Class<?> type) {
    Binding<?> binding = injector.getBinding(type);
    return binding != null ? new GuiceBean(binding) : null;
  }

  public Iterable<GuiceBean> resolveBeans(Class<?> type) {
    List<GuiceBean> beans = new ArrayList<GuiceBean>();
    Map<Key<?>, Binding<?>> allBindings = injector.getAllBindings();
    Collection<Binding<?>> bindings = allBindings.values();
    for (Binding<?> binding : bindings) {
      Class bindingType = binding.getKey().getTypeLiteral().getRawType();
      if (type.isAssignableFrom(bindingType)) {
        beans.add(new GuiceBean(binding));
      }
    }
    return beans;
  }

  public GuiceBean resolveBean(String name) {
    Key<?> key = nameMap.get(name);
    GuiceBean bean = null;
    if (key != null) {
      bean = new GuiceBean(injector.getBinding(key));
    }
    return bean;
  }

  public Object create(GuiceBean bean) throws InvocationTargetException {
    try {
      return bean.binding.getProvider().get();
    }
    catch (ProvisionException e) {
      throw new InvocationTargetException(e.getCause());
    }
  }

  public Object get(GuiceBean bean, Object instance) throws InvocationTargetException {
    return instance;
  }

  public void release(GuiceBean bean, Object instance) {
    Scoping scoping = ((BindingImpl)bean.binding).getScoping();
    if (scoping.isNoScope()) {
      invokePreDestroy(instance);
    }
  }

  static void invokePreDestroy(Object o) {
    for (Method method : o.getClass().getMethods()) {
      if (
        Modifier.isPublic(method.getModifiers()) &&
          !Modifier.isStatic(method.getModifiers()) &&
          method.getAnnotation(PreDestroy.class) != null) {
        try {
          method.invoke(o);
        }
        catch (IllegalAccessException e) {
          throw new UnsupportedOperationException("handle me gracefully", e);
        }
        catch (InvocationTargetException e) {
          throw new UnsupportedOperationException("handle me gracefully", e);
        }
      }
    }
  }

  public void shutdown() {
    for (Binding<?> binding : injector.getAllBindings().values()) {
      Scoping scoping = ((BindingImpl)binding).getScoping();
      if (scoping == Scoping.SINGLETON_INSTANCE) {
        invokePreDestroy(binding.getProvider().get());
      }
    }
  }
}
