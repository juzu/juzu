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

package plugin.shiro.authc.login;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.util.ThreadContext;

import plugin.shiro.AbstractShiroTestCase;
import plugin.shiro.authc.LoginTestCase;

import juzu.Action;
import juzu.Path;
import juzu.Response;
import juzu.Route;
import juzu.View;
import juzu.request.RequestContext;
import juzu.plugin.shiro.*;
import juzu.template.Template;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 * 
 */
public class A
{
   @Action
   @Route("/login")
   @Login(username = "uname", password = "passwd")
   public Response login(AuthenticationException e)
   {
      LoginTestCase.exception = e;
      return e == null ? A_.success() : A_.failed();
   }

   @View @Route("/index")
   public Response.Content index()
   {
      AbstractShiroTestCase.manager = (DefaultSecurityManager)ThreadContext.getSecurityManager();
      return Response.ok(
         "<form action='" +A_.login()+ "' method='post'>" +
               "<input type='text' id='uname' name='uname'/>" +
               "<input type='password' id='passwd' name='passwd'/>" +
               "<input type='submit' id='submit' name='submit' value='Login'/>" +
         "</form>");
   }
   
   @View
   @Route("/success")
   public Response success()
   {
      return Response.ok(SecurityUtils.getSubject().getPrincipal() + " logged");
   }
   
   @View
   @Route("/failed")
   public Response failed()
   {
      return Response.ok("failed");
   }
}
