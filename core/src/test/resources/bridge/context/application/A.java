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

package bridge.context.application;

import juzu.Response;
import juzu.View;
import juzu.impl.bridge.context.AbstractApplicationContextTestCase;
import juzu.impl.bridge.context.AbstractUserContextTestCase;
import juzu.request.ApplicationContext;
import juzu.request.UserContext;

import java.util.Locale;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class A {
  @View
  public Response.Content<?> index(ApplicationContext applicationContext) {
    AbstractApplicationContextTestCase.bundle = applicationContext.resolveBundle(Locale.FRANCE);
    return Response.ok("");
  }
}
