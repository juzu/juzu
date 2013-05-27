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

package juzu.impl.inject.spi.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Binding;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.ProvisionException;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.binder.ScopedBindingBuilder;
import com.google.inject.internal.BindingImpl;
import com.google.inject.internal.Scoping;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Named;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import juzu.Scope;
import juzu.impl.common.Tools;
import juzu.impl.inject.ScopeController;
import juzu.impl.inject.spi.InjectorProvider;
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

  /** . */
  private final ScopeController scopeController;

  public GuiceContext(final GuiceInjector bootstrap) {

    //
    this.scopeController = new ScopeController();

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
            bindScope(scope.getAnnotationType(), new GuiceScope(scope, scopeController));
          }
        }

        // Bind beans
        for (BeanBinding<?> beanBinding : bootstrap.bindings) {

          // Get a binding key
          Key key;
          if (beanBinding.qualifiers != null && beanBinding.qualifiers.size() > 0) {
            Iterator<Annotation> i = beanBinding.qualifiers.iterator();
            // Construction to make compiler happy
//            b = a.annotatedWith(i.next());
//            while (i.hasNext()) {
//              b = a.annotatedWith(i.next());
//            }
            key = Key.get(beanBinding.type, i.next());
          }
          else {
            key = Key.get(beanBinding.type);
          }
          LinkedBindingBuilder b = bind(key);

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
                  // Guice trick : need to bind alias so the implementation type will be bound with the qualifier
                  bind(Key.get(d.implementationType, key.getAnnotation())).toProvider(new BeanAlias(key));
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

  public InjectorProvider getProvider() {
    return InjectorProvider.INJECT_GUICE;
  }

  @Override
  public ScopeController getScopeController() {
    return scopeController;
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
      throw new InvocationTargetException(Tools.safeCause(e));
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

  public void close() {
    for (Binding<?> binding : injector.getAllBindings().values()) {
      Scoping scoping = ((BindingImpl)binding).getScoping();
      if (scoping == Scoping.SINGLETON_INSTANCE) {
        invokePreDestroy(binding.getProvider().get());
      }
    }
  }
}
