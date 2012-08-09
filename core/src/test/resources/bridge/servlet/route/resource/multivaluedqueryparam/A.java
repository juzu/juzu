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

package bridge.servlet.route.resource.multivaluedqueryparam;

import juzu.Controller;
import juzu.Resource;
import juzu.Response;
import juzu.Route;
import juzu.View;

import java.util.Arrays;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class A extends Controller {

  @View
  public Response.Content index() {
    return Response.render("<a id='trigger' href='" + A_.fooURL(new String[]{"bar1","bar2"}) + "'>click</div>");
  }

  @Resource
  @Route("/foo")
  public Response.Content foo(String[] juu) {
    return Response.ok("" + Arrays.asList(juu));
  }
}
