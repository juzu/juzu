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

import juzu.impl.inject.ScopeController;
import juzu.impl.plugin.controller.AmbiguousResolutionException;
import juzu.impl.common.Tools;
import juzu.impl.inject.spi.InjectorProvider;
import juzu.impl.inject.spi.InjectionContext;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import java.lang.reflect.InvocationTargetException;

/**
 * <ul> <li>http://matthiaswessendorf.wordpress.com/2010/04/17/spring-3-0-and-jsr-330-part/</li>
 * <li>http://matthiaswessendorf.wordpress.com/2010/04/20/spring-3-0-and-jsr-330-part-2/</li>
 * <li>http://matthiaswessendorf.wordpress.com/2010/05/06/using-cdi-scopes-with-spring-3/</li>
 * <li>http://www.earldouglas.com/jsr-330-compliance-with-spring/</li> <li>http://niklasschlimm.blogspot.com/2011/07/custom-scopes-in-cdi-10-and-spring-31.html</li>
 * </ul>
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class SpringContext extends InjectionContext<String, Object> {

  /** . */
  private final DefaultListableBeanFactory factory;

  /** . */
  private final ClassLoader classLoader;

  /** . */
  private final ScopeController scopeController;

  public SpringContext(ScopeController scopeController, DefaultListableBeanFactory factory, ClassLoader classLoader) {
    factory.registerSingleton(Tools.nextUUID(), this);

    //
    this.factory = factory;
    this.classLoader = classLoader;
    this.scopeController = scopeController;
  }

  @Override
  public ScopeController getScopeController() {
    return scopeController;
  }

  public InjectorProvider getProvider() {
    return InjectorProvider.SPRING;
  }

  public ClassLoader getClassLoader() {
    return classLoader;
  }

  public String resolveBean(Class<?> type) {
    String[] names = factory.getBeanNamesForType(type);
    switch (names.length) {
      case 0:
        return null;
      case 1:
        return names[0];
      default:
        throw new AmbiguousResolutionException();
    }
  }

  public String resolveBean(String name) {
    try {
      factory.getBean(name);
      return name;
    }
    catch (NoSuchBeanDefinitionException e) {
      return null;
    }
  }

/*
  public static String resolveBean(BeanFactory factory, String name) {
    try {
      BeanDefinition i = factory.getBeanDefinition(name);
      return name;
    }
    catch (NoSuchBeanDefinitionException e) {
      return null;
    }
  }
*/

  public Iterable<String> resolveBeans(Class<?> type) {
    return factory.getBeansOfType(type).keySet();
  }

  public Object createContext(String bean) throws InvocationTargetException {
    try {
      return factory.getBean(bean);
    }
    catch (BeanCreationException e) {
      Throwable cause = e.getCause();
      if (cause instanceof BeanInstantiationException) {
        BeanInstantiationException bie = (BeanInstantiationException)cause;
        cause = bie.getCause();
      }
      throw new InvocationTargetException(cause);
    }
  }

  public Object getInstance(String bean, Object context) {
    return context;
  }

  public void releaseContext(String bean, Object context) {
    if (factory.isPrototype(bean)) {
      factory.destroyBean(bean, context);
    }
  }

  public void close() {
    factory.destroySingletons();
  }
}
