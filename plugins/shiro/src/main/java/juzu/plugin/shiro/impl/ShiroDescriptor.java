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

import java.util.Arrays;

import juzu.Response;
import juzu.Scope;
import juzu.impl.common.JSON;
import juzu.impl.inject.BeanDescriptor;
import juzu.impl.plugin.ServiceContext;
import juzu.impl.plugin.ServiceDescriptor;

import juzu.impl.request.Stage;
import org.apache.shiro.mgt.SecurityManager;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 * 
 */
public class ShiroDescriptor extends ServiceDescriptor {
  /** . */
  private final ShiroAuthorizor authorizer;

  /** . */
  private final ShiroAuthenticator authenticater;

  /** . */
  private final BeanDescriptor bean;
  
  /** .*/
  private ServiceContext context;

  ShiroDescriptor(ServiceContext context) {
    JSON config = context.getConfig();
    this.authenticater = new ShiroAuthenticator(config.get("rememberMe") != null ? true : false);
    this.authorizer = new ShiroAuthorizor();
    this.context = context;
    this.bean =
      BeanDescriptor
        .createFromProvider(SecurityManager.class, Scope.SESSION, null, new SecurityManagerProvider(config));
  }

  public JSON getConfig() {
    return context.getConfig();
  }
  
  public ServiceContext getContext() {
    return context;
  }

  @Override
  public Iterable<BeanDescriptor> getBeans() {
    return Arrays.asList(bean);
  }

  public Response invoke(Stage.Handler stage) {
    //
    String methodId = stage.getRequest().getHandler().getHandle().toString();
    String controllerId = methodId.substring(0, methodId.indexOf('#'));
    methodId = methodId.substring(controllerId.length() + 1);
    JSON controllerJSON = getConfig().getJSON(controllerId);
    if (controllerJSON == null) {
      return stage.invoke();
    }

    //
    JSON methodsJSON = controllerJSON.getJSON("methods");
    JSON methodJSON;

    if (controllerJSON.get("require") != null) {
      Response resp = authorizer.isAuthorized(stage, controllerJSON);
      if (resp != null) {
        return resp;
      } else {
        if (methodsJSON == null) {
          return stage.invoke();
        }

        methodJSON = methodsJSON.getJSON(methodId);
        if (methodJSON == null) {
          return stage.invoke();
        }

        return doInvoke(stage, methodJSON);
      }
    }

    if (methodsJSON == null) {
      return stage.invoke();
    }

    methodJSON = methodsJSON.getJSON(methodId);
    if (methodJSON == null) {
      return stage.invoke();
    }

    return doInvoke(stage, methodJSON);
  }

  private Response doInvoke(Stage.Handler request, JSON json) {
    Response resp = authorizer.isAuthorized(request, json);
    if (resp != null) {
      return resp;
    } else {
      if ("login".equals(json.get("operator"))) {
        return authenticater.doLogin(request);
      } else if ("logout".equals(json.get("operator"))) {
        return authenticater.doLogout(request);
      } else {
        return request.invoke();
      }
    }
  }
}
