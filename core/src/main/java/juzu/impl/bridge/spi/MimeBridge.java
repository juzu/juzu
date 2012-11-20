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

package juzu.impl.bridge.spi;

import juzu.Dispatch;
import juzu.impl.common.MethodHandle;
import juzu.impl.plugin.controller.descriptor.MethodDescriptor;
import juzu.request.Phase;

import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public interface MimeBridge extends RequestBridge {

  /**
   * Create a dispatch for the specified phase, target and parameters.
   *
   * @param phase the dispatch phase
   * @param target the dispatch target
   * @param parameters the dispatch parameters
   * @return the dispatch object
   * @throws IllegalArgumentException if any parameter is not valid
   * @throws NullPointerException if any argument is null
   */
  Dispatch createDispatch(Phase phase, MethodHandle target, Map<String, String[]> parameters) throws NullPointerException, IllegalArgumentException;

}
