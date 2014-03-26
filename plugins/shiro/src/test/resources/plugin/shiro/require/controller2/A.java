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
package plugin.shiro.require.controller2;

import juzu.Action;
import juzu.Response;
import juzu.Route;
import juzu.View;
import juzu.plugin.shiro.*;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.annotation.RequiresGuest;
import org.apache.shiro.authz.annotation.RequiresUser;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.subject.Subject;

import plugin.shiro.authc.AuthcWithRequireAtCtrlTestCase;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 *
 */
@RequiresGuest
public class A
{
   @View @Route("/")
   public Response index(AuthorizationException e) {
      AuthcWithRequireAtCtrlTestCase.manager = (DefaultSecurityManager)SecurityUtils.getSecurityManager();
      AuthcWithRequireAtCtrlTestCase.exception = e;
      return Response.ok(
         "<a href='" + A_.login("foo", "foo") + "' id='failed'>failed</a>" +
         "<a href='" + A_.login("root", "secret") + "' id='login'>login</a>" +
         "<a href='" + A_.logout() + "' id='logout'>logout</a>");
   }
   
   @Action @Route("/login") @Login
   public Response login(String username, String password, AuthenticationException e)
   {
      AuthcWithRequireAtCtrlTestCase.exception = e;
      AuthcWithRequireAtCtrlTestCase.currentUser = SecurityUtils.getSubject();
      return A_.foo();
   }
   
   @View @Route("/foo")
   public Response foo() {
     return Response.ok("foo");
   }
   
   @Action @Route("/logout") @Logout
   public Response logout(AuthorizationException e)
   {
      SecurityUtils.getSubject().logout();
      AuthcWithRequireAtCtrlTestCase.exception = e;
      AuthcWithRequireAtCtrlTestCase.currentUser = SecurityUtils.getSubject();
      return A_.foo();
   }
}
