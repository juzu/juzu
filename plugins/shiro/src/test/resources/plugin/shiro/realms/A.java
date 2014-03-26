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
package plugin.shiro.realms;

import java.lang.reflect.InvocationTargetException;

import javax.enterprise.inject.Any;
import javax.inject.Inject;

import juzu.Action;
import juzu.Response;
import juzu.Route;
import juzu.View;
import juzu.impl.request.Request;
import juzu.plugin.shiro.*;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.annotation.RequiresGuest;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.util.ThreadContext;

import plugin.shiro.realms.MultipleRealmsTestCase;

import com.google.inject.name.Named;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 *
 */
public class A
{
   @View @Route("/")
   public Response index() throws InvocationTargetException
   {
      MultipleRealmsTestCase.manager = (DefaultSecurityManager)ThreadContext.getSecurityManager();
      return Response.ok("<a href='" + A_.login("john", "foo") + "' id='john'>john</a><a href='" + A_.login("marry", "foo") + "' id='marry'>marry</a>");
   }
   
   @Action @Route("/login") @Login
   public Response login(String username, String password, AuthenticationException e)
   {
      return A_.access();
   }
   
   @View @Route("/view") @RequiresRoles("role3")
   public Response access(AuthorizationException e)
   {
      return e == null ? Response.ok("ok") : Response.ok("can not access");
   }
}
