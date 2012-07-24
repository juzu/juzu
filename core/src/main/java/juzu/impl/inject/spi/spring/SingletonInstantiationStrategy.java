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

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.InstantiationStrategy;
import org.springframework.beans.factory.support.RootBeanDefinition;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class SingletonInstantiationStrategy implements InstantiationStrategy {

  /** . */
  private final Map<String, Object> singletons;

  /** . */
  private final InstantiationStrategy delegate;

  SingletonInstantiationStrategy(InstantiationStrategy delegate, Map<String, Object> singletons) {
    this.delegate = delegate;
    this.singletons = singletons;
  }

  public Object instantiate(RootBeanDefinition beanDefinition, String beanName, BeanFactory owner) throws BeansException {
    Object instance = singletons.get(beanName);
    if (instance != null) {
      return instance;
    }
    return delegate.instantiate(beanDefinition, beanName, owner);
  }

  public Object instantiate(RootBeanDefinition beanDefinition, String beanName, BeanFactory owner, Constructor<?> ctor, Object[] args) throws BeansException {
    Object instance = singletons.get(beanName);
    if (instance != null) {
      return instance;
    }
    return delegate.instantiate(beanDefinition, beanName, owner, ctor, args);
  }

  public Object instantiate(RootBeanDefinition beanDefinition, String beanName, BeanFactory owner, Object factoryBean, Method factoryMethod, Object[] args) throws BeansException {
    Object instance = singletons.get(beanName);
    if (instance != null) {
      return instance;
    }
    return delegate.instantiate(beanDefinition, beanName, owner, factoryBean, factoryMethod, args);
  }
}
