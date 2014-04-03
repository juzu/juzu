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
class ControllerMetaModelResolver extends ControllerResolver<HandlerMetaModel> {

  /** . */
  private final ControllersMetaModel controllers;

  /** . */
  private final HandlerMetaModel[] methods;

  /** . */
  private final int size;

  ControllerMetaModelResolver(ControllersMetaModel controllers) throws NullPointerException {
    int size = 0;
    List<HandlerMetaModel> methods = new ArrayList<HandlerMetaModel>();
    for (ControllerMetaModel controller : controllers.getChildren(ControllerMetaModel.class)) {
      size++;
      for (HandlerMetaModel method : controller.getHandlers()) {
        methods.add(method);
      }
    }

    //
    this.controllers = controllers;
    this.methods = methods.toArray(new HandlerMetaModel[methods.size()]);
    this.size = size;
  }

  @Override
  public HandlerMetaModel[] getHandlers() {
    return methods;
  }

  @Override
  public String getId(HandlerMetaModel method) {
    return method.getId();
  }

  @Override
  public Phase getPhase(HandlerMetaModel method) {
    return method.getPhase();
  }

  @Override
  public String getName(HandlerMetaModel method) {
    return method.getName();
  }

  @Override
  public boolean isDefault(HandlerMetaModel method) {
    return method.getController().getHandle().getName().equals(controllers.defaultController) || size < 2;
  }

  @Override
  public Collection<String> getParameterNames(HandlerMetaModel method) {
    return method.getParameterNames();
  }
}
