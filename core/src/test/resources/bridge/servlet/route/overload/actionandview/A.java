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

package bridge.servlet.route.overload.actionandview;

import juzu.Action;
import juzu.Controller;
import juzu.Response;
import juzu.Route;
import juzu.View;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class A extends Controller {

  /** . */
  static int count = 0;

  @View
  public Response.Content index() {
    count = 0;
    return Response.render("" + A_.fooActionURL());
  }

  @Action
  @Route("/foo")
  public Response.Update fooAction() {
    count = 1;
    return A_.fooView();
  }

  @View
  @Route("/foo")
  public Response.Content fooView() {
    return Response.ok(count == 1 ? "pass" : "fail");
  }
}
