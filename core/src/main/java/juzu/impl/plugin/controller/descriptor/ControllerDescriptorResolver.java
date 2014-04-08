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

package juzu.impl.plugin.controller.descriptor;

import juzu.impl.plugin.controller.ControllerResolver;
import juzu.impl.request.ControllerHandler;
import juzu.request.Phase;

import java.util.Collection;

/**
 * Resolves controller method algorithm.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
class ControllerDescriptorResolver extends ControllerResolver<ControllerHandler> {

  /** . */
  private final ControllersDescriptor desc;

  /** . */
  private final ControllerHandler[] handlers;

  ControllerDescriptorResolver(ControllersDescriptor desc) throws NullPointerException {
    this.handlers = desc.getHandlers().toArray(new ControllerHandler[desc.getHandlers().size()]);
    this.desc = desc;
  }

  @Override
  public ControllerHandler[] getHandlers() {
    return handlers;
  }

  @Override
  public String getId(ControllerHandler handler) {
    return handler.getId();
  }

  @Override
  public Phase getPhase(ControllerHandler handler) {
    return handler.getPhase();
  }

  @Override
  public String getName(ControllerHandler handler) {
    return handler.getName();
  }

  @Override
  public boolean isDefault(ControllerHandler handler) {
    return handler.getType() == desc.getDefaultController() || desc.getControllers().size() < 2;
  }

  @Override
  public Collection<String> getParameterNames(ControllerHandler handler) {
    return handler.getParameterNames();
  }
}
