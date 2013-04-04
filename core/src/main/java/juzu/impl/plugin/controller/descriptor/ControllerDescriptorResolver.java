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
import juzu.impl.request.Method;
import juzu.request.Phase;

import java.util.Collection;

/**
 * Resolves controller method algorithm.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
class ControllerDescriptorResolver extends ControllerResolver<Method> {

  /** . */
  private final ControllersDescriptor desc;

  /** . */
  private final Method[] methods;

  ControllerDescriptorResolver(ControllersDescriptor desc) throws NullPointerException {
    this.methods = desc.getMethods().toArray(new Method[desc.getMethods().size()]);
    this.desc = desc;
  }

  @Override
  public Method[] getMethods() {
    return methods;
  }

  @Override
  public String getId(Method method) {
    return method.getId();
  }

  @Override
  public Phase getPhase(Method method) {
    return method.getPhase();
  }

  @Override
  public String getName(Method method) {
    return method.getName();
  }

  @Override
  public boolean isDefault(Method method) {
    return method.getType() == desc.getDefault() || desc.getControllers().size() < 2;
  }

  @Override
  public Collection<String> getParameterNames(Method method) {
    return method.getParameterNames();
  }
}
