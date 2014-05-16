/*
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
package juzu.authenticated;

import juzu.Response;
import juzu.View;
import juzu.Route;
import juzu.request.RequestContext;

import javax.annotation.security.DenyAll;
import javax.annotation.security.RolesAllowed;

public class A {

  @DenyAll
  @View
  @Route("/denyall")
  public Response denyAll() {
    return Response.ok("ok");
  }

  @RolesAllowed("manager")
  @View
  @Route("/manager")
  public Response manager() {
    return Response.ok("ok");
  }

  @RolesAllowed("myrole")
  @View
  @Route("/myrole")
  public Response myrole() {
    return Response.ok("ok");
  }
}
