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
package plugin.shiro.authz;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresGuest;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.authz.annotation.RequiresUser;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.util.ThreadContext;

import plugin.shiro.AbstractShiroTestCase;
import plugin.shiro.authz.AuthorizationTestCase;

import juzu.Action;
import juzu.Path;
import juzu.Resource;
import juzu.Response;
import juzu.Route;
import juzu.View;
import juzu.impl.common.Tools;
import juzu.plugin.shiro.*;
import juzu.template.Template;


/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 *
 */
public class A
{
   @View @Route("/")
   public Response index() throws Exception 
   {
      AbstractShiroTestCase.manager = (DefaultSecurityManager)ThreadContext.getSecurityManager();
      
      String resp =
      "<a id='root' href='" + A_.login("root", "secret") + "'>root</a>" +
      "<a id='john' href='" + A_.login("john", "foo") + "'>john</a>" +
      "<a id='logout' href='" + A_.logout() + "'>logout</a>" +
      "<a id='role1' href='" + A_.role1() + "'>role1</a>" +
      "<a id='role2' href='" + A_.role2() + "'>role2</a>" +
      "<a id='role1or2' href='" + A_.role1or2() + "'>role1or2</a>" +
      "<a id='role1and2' href='" + A_.role1and2() + "'>role1and2</a>" +
      "<a id='permission1' href='" + A_.permission1() + "'>permission1</a>" +
      "<a id='permission2' href='" + A_.permission2() + "'>permission2</a>" +
      "<a id='role2andPerm1' href='" + A_.role2andPerm1() + "'>role2andPerm1</a>";
      return Response.ok(resp);
   }
   
   @Action @Route("/role1") @RequiresRoles("role1")
   public Response role1(AuthorizationException e)
   {
      return e == null ? A_.ok() : A_.error("role1", null);
   }
   
   @Action @Route("/role2") @RequiresRoles("role2")
   public Response role2(AuthorizationException e)
   {
      return e == null ? A_.ok() : A_.error("role2", null);
   }
   
   @Action @Route("/role1or2") @RequiresRoles(value={"role1","role2"}, logical=Logical.OR)
   public Response role1or2(AuthorizationException e)
   {
      return e == null ? A_.ok() : A_.error("role1 OR role2", null);
   }
   
   
   @Action @Route("/role1and2") @RequiresRoles(value={"role1","role2"})
   public Response role1and2(AuthorizationException e)
   {
      return e == null ? A_.ok() : A_.error("role1 AND role2", null);
   }
   
   @Action @Route("/permission1") @RequiresPermissions("permission1")
   public Response permission1(AuthorizationException e)
   {
      return e == null ? A_.ok() : A_.error(null, "permission1");
   }
   
   @Action @Route("/permission2") @RequiresPermissions("permission2")
   public Response permission2(AuthorizationException e)
   {
      return e == null ? A_.ok() : A_.error(null, "permission2");
   }
   
   @Action @Route("/role2andPerm1") @RequiresRoles("role2") @RequiresPermissions("permission1")
   public Response role2andPerm1(AuthorizationException e)
   {
      return e == null ? A_.ok() : A_.error("role2", "permission1");
   }
   
   @View @Route("/ok")
   public Response ok()
   {
      return Response.ok("ok");
   }
   
   @View @Route("/error")
   public Response error(String roles, String permisions)
   {
      if(roles != null && permisions != null)
      {
         AuthorizationTestCase.missingRole = roles;
         AuthorizationTestCase.missingPermission = permisions;
      }
      else if(roles != null)
      {
         AuthorizationTestCase.missingRole = roles;
      }
      else if(permisions != null)
      {
         AuthorizationTestCase.missingPermission = permisions;
      }
      
      return Response.ok("Cannot access");
   }
   
   @Action @Route("/login") @Login
   public Response login(String username, String password, AuthenticationException e)
   {
      if(e != null) return Response.error(e);
      return A_.index();
   }
   
   @Action @Route("/logout") @Logout
   public Response logout() {
     return A_.index();
   }
}
