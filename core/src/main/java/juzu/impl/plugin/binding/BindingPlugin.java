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

import juzu.impl.inject.BeanDescriptor;
import juzu.impl.metadata.Descriptor;
import juzu.impl.common.JSON;
import juzu.impl.plugin.application.ApplicationPlugin;

import java.util.ArrayList;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class BindingPlugin extends ApplicationPlugin {

  public BindingPlugin() {
    super("binding");
  }

  @Override
  public Descriptor init(ClassLoader loader, JSON config) throws Exception {
    ArrayList<BeanDescriptor> beans = new ArrayList<BeanDescriptor>();
    List<? extends JSON> bindings = config.getList("bindings", JSON.class);
    for (JSON binding : bindings) {
      String value = binding.getString("value");
      String scope = binding.getString("scope");
      String implementation = binding.getString("implementation");
      Class<?> valueType = loader.loadClass(value);
      Class<?> implementationType = implementation != null ? loader.loadClass(implementation) : null;
      juzu.Scope beanScope = scope != null ? juzu.Scope.valueOf(scope.toUpperCase()) : null;
      BeanDescriptor bean = new BeanDescriptor(valueType, beanScope, null, implementationType);
      beans.add(bean);
    }
    return new BindingDescriptor(beans);
  }
}
