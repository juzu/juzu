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

package juzu.test.protocol.mock;

import juzu.request.SecurityContext;

import java.security.Principal;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class MockSecurityContext implements SecurityContext {

  /** . */
  private String remoteUser;

  /** . */
  private Set<String> roles;

  /** . */
  private Principal principal;

  public String getRemoteUser() {
    return remoteUser;
  }

  public void setRemoteUser(String remoteUser) {
    if (remoteUser == null) {
      this.remoteUser = null;
      this.principal = null;
    }
    else {
      this.remoteUser = remoteUser;
      this.principal = new Principal() {
        public String getName() {
          return MockSecurityContext.this.remoteUser;
        }
      };
    }
  }

  public Principal getUserPrincipal() {
    return principal;
  }

  public boolean isUserInRole(String role) {
    return roles.contains(role);
  }

  public void addRole(String role) {
    roles.add(role);
  }

  public void clearRoles() {
    roles.clear();
  }
}
