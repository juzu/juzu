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

import juzu.AmbiguousResolutionException;
import juzu.impl.inject.spi.InjectImplementation;
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

  public SpringContext(DefaultListableBeanFactory factory, ClassLoader classLoader) {
    factory.registerSingleton("" + Math.random(), this);

    //
    this.factory = factory;
    this.classLoader = classLoader;
  }

  public InjectImplementation getImplementation() {
    return InjectImplementation.INJECT_SPRING;
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
      factory.getBeanDefinition(name);
      return name;
    }
    catch (NoSuchBeanDefinitionException e) {
      return null;
    }
  }

  public Iterable<String> resolveBeans(Class<?> type) {
    return factory.getBeansOfType(type).keySet();
  }

  public Object create(String bean) throws InvocationTargetException {
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

  public Object get(String bean, Object instance) {
    return instance;
  }

  public void release(String bean, Object instance) {
    if (factory.isPrototype(bean)) {
      factory.destroyBean(bean, instance);
    }
  }

  public void shutdown() {
    factory.destroySingletons();
  }
}
