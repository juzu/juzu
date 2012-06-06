/*
 * Copyright (C) 2011 eXo Platform SAS.
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

package juzu.impl.controller.descriptor;

import juzu.impl.controller.ControllerResolver;
import juzu.request.Phase;

import java.util.Collection;

/**
 * Resolves controller method algorithm.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class ControllerMethodResolver extends ControllerResolver<ControllerMethod> {

  /** . */
  private final ControllerDescriptor desc;

  /** . */
  private final ControllerMethod[] methods;

  public ControllerMethodResolver(ControllerDescriptor desc) throws NullPointerException {
    this.methods = desc.getMethods().toArray(new ControllerMethod[desc.getMethods().size()]);
    this.desc = desc;
  }

  @Override
  public ControllerMethod[] getMethods() {
    return methods;
  }

  @Override
  public String getId(ControllerMethod method) {
    return method.getId();
  }

  @Override
  public Phase getPhase(ControllerMethod method) {
    return method.getPhase();
  }

  @Override
  public String getName(ControllerMethod method) {
    return method.getName();
  }

  @Override
  public boolean isDefault(ControllerMethod method) {
    return method.getType() == desc.getDefault();
  }

  @Override
  public Collection<String> getParameterNames(ControllerMethod method) {
    return method.getArgumentNames();
  }
}
