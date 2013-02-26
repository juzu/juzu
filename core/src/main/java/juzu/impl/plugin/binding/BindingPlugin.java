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

package juzu.impl.plugin.binding;

import juzu.impl.common.Tools;
import juzu.impl.inject.BeanDescriptor;
import juzu.impl.metadata.Descriptor;
import juzu.impl.common.JSON;
import juzu.impl.plugin.PluginContext;
import juzu.impl.plugin.application.ApplicationPlugin;
import juzu.inject.ProviderFactory;

import javax.inject.Provider;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class BindingPlugin extends ApplicationPlugin {

  public BindingPlugin() {
    super("binding");
  }

  @Override
  public Descriptor init(PluginContext context) throws Exception {
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
      return new BindingDescriptor(beans);
    } else {
      return null;
    }
  }
}
