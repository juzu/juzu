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

import java.util.List;

import juzu.Response;
import juzu.impl.request.ContextualParameter;
import juzu.impl.request.ControlParameter;
import juzu.impl.request.Request;
import juzu.impl.request.Stage;
import juzu.plugin.shiro.Login;
import juzu.plugin.shiro.impl.common.RememberMeUtil;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 * 
 */
public class ShiroAuthenticator {

  private final boolean rememberMeSupported;

  public ShiroAuthenticator(boolean rememberMeSupported) {
    this.rememberMeSupported = rememberMeSupported;
  }

  public Response doLogout(Stage.Handler request) {
    SecurityUtils.getSubject().logout();
    Response resp = request.invoke();
    if (rememberMeSupported) {
      RememberMeUtil.forgetIdentity();
    }
    return resp;
  }

  public Response doLogin(Stage.Handler stage) {
    Request request = stage.getRequest();
    Login loginAnnotation = request.getHandler().getMethod().getAnnotation(Login.class);
    Subject subject = SecurityUtils.getSubject();

    boolean remember = request.getParameterArguments().get(loginAnnotation.rememberMe()) != null ? true : false;
    String username = null;
    String password = null;
    try {
      username = request.getParameterArguments().get(loginAnnotation.username()).getValue();
      password = request.getParameterArguments().get(loginAnnotation.password()).getValue();
    } catch (NullPointerException e) {
      List<ControlParameter> parameters = request.getHandler().getParameters();
      for (ControlParameter parameter : parameters) {
        if (parameter instanceof ContextualParameter) {
          if (AuthenticationException.class.isAssignableFrom(parameter.getType())) {
            request.getContextualArguments().put((ContextualParameter)parameter, new AuthenticationException(e.getCause()));
            return stage.invoke();
          }
        }
      }

      //
      return new Response.Error(e);
    }

    try {
      subject.login(new UsernamePasswordToken(username, password.toCharArray(), remember));

      //
      Response resp = stage.invoke();
      if (remember && rememberMeSupported) {
        RememberMeUtil.forgetIdentity();
        RememberMeUtil.rememberSerialized();
      }
      return resp;
    } catch (AuthenticationException e) {
      List<ControlParameter> parameters = request.getHandler().getParameters();
      for (ControlParameter parameter : parameters) {
        if (parameter instanceof ContextualParameter) {
          if (AuthenticationException.class.isAssignableFrom(parameter.getType())) {
            request.getContextualArguments().put((ContextualParameter)parameter, e);
            Response resp = stage.invoke();
            if (remember) {
              RememberMeUtil.forgetIdentity();
            }
            return resp;
          }
        }
      }

      return new Response.Error(e);
    }
  }
}
