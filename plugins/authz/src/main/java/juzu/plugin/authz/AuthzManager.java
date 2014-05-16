/*
 * Copyright 2013 eXo Platform SAS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package juzu.plugin.authz;

import juzu.Response;
import juzu.impl.request.Request;
import juzu.impl.request.RequestFilter;
import juzu.impl.request.Stage;
import juzu.request.SecurityContext;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import java.lang.reflect.Method;

/**
 * @author Julien Viet
 */
public class AuthzManager implements RequestFilter<Stage.Handler> {

  @Override
  public Class<Stage.Handler> getStageType() {
    return Stage.Handler.class;
  }

  @Override
  public Response handle(Stage.Handler argument) {
    Request request = argument.getRequest();

    // Search for annotation on the method
    Method method = request.getHandler().getMethod();
    RolesAllowed rolesAllowed = method.getAnnotation(RolesAllowed.class);
    PermitAll permitAll = method.getAnnotation(PermitAll.class);
    DenyAll denyAll = method.getAnnotation(DenyAll.class);

    // Look at parent if nothing found at method level
    if (rolesAllowed == null && permitAll == null && denyAll == null) {
      Class<?> controllerClass = method.getDeclaringClass();
      rolesAllowed = controllerClass.getAnnotation(RolesAllowed.class);
      denyAll = controllerClass.getAnnotation(DenyAll.class);
    }

    //
    boolean ok = false;
    if (denyAll != null) {
      ok = false;
    } else if (rolesAllowed != null) {
      SecurityContext securityContext = request.getSecurityContext();
      for (String role : rolesAllowed.value()) {
        if (securityContext.isUserInRole(role)) {
          ok = true;
          break;
        }
      }
    } else {
      ok = true;
    }

    //
    if (!ok) {
      return new Response.Error.Forbidden("Access denied");
    } else {
      return argument.invoke();
    }
  }
}
