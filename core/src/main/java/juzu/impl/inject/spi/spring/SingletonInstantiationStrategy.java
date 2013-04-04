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
