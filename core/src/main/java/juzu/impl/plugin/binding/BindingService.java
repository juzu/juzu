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

package juzu.impl.plugin.binding;

import juzu.impl.plugin.ServiceContext;
import juzu.impl.plugin.ServiceDescriptor;
import juzu.impl.common.Tools;
import juzu.impl.inject.BeanDescriptor;
import juzu.impl.common.JSON;
import juzu.impl.plugin.application.ApplicationService;
import juzu.inject.ProviderFactory;

import javax.inject.Provider;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class BindingService extends ApplicationService {

  public BindingService() {
    super("binding");
  }

  @Override
  public ServiceDescriptor init(ServiceContext context) throws Exception {
    JSON config = context.getConfig();
    ClassLoader loader = context.getClassLoader();
    if (config != null) {

      // Load factories via servicer loader mechanism
      ArrayList<ProviderFactory> factories = Tools.list(ServiceLoader.load(ProviderFactory.class, loader));

      //
      ArrayList<BeanDescriptor> beans = new ArrayList<BeanDescriptor>();
      List<? extends JSON> bindings = config.getList("bindings", JSON.class);
      for (JSON binding : bindings) {
        String value = binding.getString("value");
        String scope = binding.getString("scope");
        String implementation = binding.getString("implementation");

        //
        Class<?> beanClass = loader.loadClass(value);
        juzu.Scope beanScope = scope != null ? juzu.Scope.valueOf(scope.toUpperCase()) : null;
        Class<?> beanImplClass = implementation != null ? loader.loadClass(implementation) : null;

        //
        BeanDescriptor descriptor;
        if (beanImplClass != null) {
          if (Provider.class.isAssignableFrom(beanImplClass)) {
            // Should check that the provider resolved <T> variable
            // is assignable from the implementation Type
            descriptor = BeanDescriptor.createFromProviderType((Class)beanClass, beanScope, null, (Class)beanImplClass);
          } else {
            if (beanClass.isAssignableFrom(beanImplClass)) {
              descriptor = BeanDescriptor.createFromImpl((Class)beanClass, beanScope, null, (Class)beanImplClass);
            } else {
              throw new UnsupportedClassVersionError("Handle me gracefully / not tested");
            }
          }
        } else {
          descriptor = null;
          for (ProviderFactory factory : factories) {
            Provider provider = factory.getProvider(beanClass);
            if (provider != null) {
              // Should check that the provider resolved <T> variable
              // is assignable from the implementation Type
              descriptor = BeanDescriptor.createFromProvider(beanClass, beanScope, null, provider);
            }
          }
          if (descriptor == null) {
            if (beanClass.isInterface()) {
              throw new UnsupportedClassVersionError("Handle me gracefully / not tested");
            }
            if (beanClass.isEnum()) {
              throw new UnsupportedClassVersionError("Handle me gracefully / not tested");
            }
            if (beanClass.isPrimitive()) {
              throw new UnsupportedClassVersionError("Handle me gracefully / not tested");
            }
            if (Modifier.isAbstract(beanClass.getModifiers())) {
              throw new UnsupportedClassVersionError("Handle me gracefully / not tested");
            }
            descriptor = BeanDescriptor.createFromBean(beanClass, beanScope, null);
          }
        }

        //
        beans.add(descriptor);
      }
      return new ServiceDescriptor(beans);
    } else {
      return null;
    }
  }
}
