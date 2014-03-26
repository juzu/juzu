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

package plugin.shiro.authc.logout;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;

import plugin.shiro.AbstractShiroTestCase;
import plugin.shiro.authc.LogoutTestCase;

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
   @View
   @Route("/")
   public Response index() 
   {
      AbstractShiroTestCase.manager = (DefaultSecurityManager)ThreadContext.getSecurityManager();
      
      Subject subject = SecurityUtils.getSubject(); 
      subject.login(new UsernamePasswordToken("root", "secret".toCharArray()));
      LogoutTestCase.currentUser = subject;
      
      return Response.ok("<a id='logout' href='" + A_.logout() + "'>logout</a>");
   }
   
   @Action
   @Route("/logout")
   @Logout
   public Response logout()
   {
      LogoutTestCase.currentUser = SecurityUtils.getSubject();
      return A_.afterLogout();
   }
   
   @View @Route("/afterLogout")
   public Response afterLogout() {
     return Response.ok("ok");
   }
}
