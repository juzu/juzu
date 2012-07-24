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

package plugin.binding.scope;

import juzu.Controller;
import juzu.Response;
import juzu.View;

import javax.inject.Inject;
import java.io.IOException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class A extends Controller {

  /** . */
  private static int serial;

  @Inject
  Bean bean;

  @View
  public Response.Content index() throws IOException {
    if (bean != null) {
      serial = bean.getSerial();
      return Response.ok(A_.doneURL().toString());
    }
    else {
      return Response.ok("");
    }
  }

  @View
  public Response.Content done() throws IOException {
    if (bean != null) {
      if (serial + 1 == bean.getSerial()) {
        return Response.ok("pass");
      }
      else {
        return Response.ok("failure: was expecting to have" + serial + " + 1 == " + bean.getSerial());
      }
    }
    else {
      return Response.ok("failure");
    }
  }
}
