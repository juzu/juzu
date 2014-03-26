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
package juzu.plugin.shiro.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import javax.inject.Provider;

import juzu.impl.common.JSON;
import juzu.impl.inject.spi.InjectionContext;
import juzu.impl.request.Request;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.UnavailableSecurityManagerException;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.realm.Realm;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 *
 */
public class SecurityManagerProvider implements Provider<SecurityManager> {

  /** .*/
  private final JSON config;

  public SecurityManagerProvider(JSON config) {
    this.config = config;
  }

  public SecurityManager get() {
    SecurityManager manager = null;
    try {
      manager = SecurityUtils.getSecurityManager();
    } catch (UnavailableSecurityManagerException e1) {
      manager = new DefaultSecurityManager();
    }
    
    boolean rememberMeSupported = config.get("rememberMe") != null ? true : false;
    if (rememberMeSupported && manager instanceof DefaultSecurityManager) {
      ((DefaultSecurityManager)manager).setRememberMeManager(new JuzuRememberMe());
    }
    if (config.get("realms") != null) {
      try {
        injectRealms(config, manager, Request.getCurrent().getApplication().getInjectionContext());
      } catch (InvocationTargetException e) {
        throw new RuntimeException(e);
      }
    }

    return manager;
  }

  private void injectRealms(JSON config, SecurityManager currentManager, InjectionContext manager) throws InvocationTargetException {

    JSON realmsJSON = config.getJSON("realms");

    Iterable beans = manager.resolveBeans(AuthorizingRealm.class);
    for (Object bean : beans) {
      Object instance = manager.createContext(bean);
      AuthorizingRealm realm = AuthorizingRealm.class.cast(manager.getInstance(bean, instance));
      JSON realmJSON = realmsJSON.getJSON(realm.getClass().getName());
      if (realmJSON != null) {
        if (realmJSON.get("name") != null) {
          realm.setName(realmJSON.getString("name"));
        }

        Collection<Realm> realms = ((RealmSecurityManager)currentManager).getRealms();
        if (realms == null) {
          ((RealmSecurityManager)currentManager).setRealm(realm);
        } else {
          ((RealmSecurityManager)currentManager).getRealms().add(realm);
        }
      }
    }
  }
}
