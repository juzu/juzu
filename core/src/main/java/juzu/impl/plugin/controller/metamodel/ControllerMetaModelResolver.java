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

package juzu.impl.plugin.controller.metamodel;

import juzu.impl.plugin.controller.ControllerResolver;
import juzu.request.Phase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class ControllerMetaModelResolver extends ControllerResolver<MethodMetaModel> {

  /** . */
  private final ControllersMetaModel controllers;

  /** . */
  private final MethodMetaModel[] methods;

  /** . */
  private final int size;

  ControllerMetaModelResolver(ControllersMetaModel controllers) throws NullPointerException {
    int size = 0;
    List<MethodMetaModel> methods = new ArrayList<MethodMetaModel>();
    for (ControllerMetaModel controller : controllers.getChildren(ControllerMetaModel.class)) {
      size++;
      for (MethodMetaModel method : controller.getMethods()) {
        methods.add(method);
      }
    }

    //
    this.controllers = controllers;
    this.methods = methods.toArray(new MethodMetaModel[methods.size()]);
    this.size = size;
  }

  @Override
  public MethodMetaModel[] getMethods() {
    return methods;
  }

  @Override
  public String getId(MethodMetaModel method) {
    return method.getId();
  }

  @Override
  public Phase getPhase(MethodMetaModel method) {
    return method.getPhase();
  }

  @Override
  public String getName(MethodMetaModel method) {
    return method.getName();
  }

  @Override
  public boolean isDefault(MethodMetaModel method) {
    return method.getController().getHandle().getFQN().equals(controllers.defaultController) || size < 2;
  }

  @Override
  public Collection<String> getParameterNames(MethodMetaModel method) {
    return method.getParameterNames();
  }
}
