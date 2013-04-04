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
