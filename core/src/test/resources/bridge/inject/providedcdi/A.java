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

package bridge.inject.providedcdi;

import juzu.Response;
import juzu.impl.inject.spi.InjectionContext;
import juzu.impl.inject.spi.cdi.CDIContext;

import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class A {

  @Inject
  InjectionContext<?, ?> context;

  @juzu.View
  public Response.Content index() throws Exception {
    if (context == null) {
      return Response.ok("no context");
    } else if (context instanceof CDIContext) {
      BeanManager manager = ((CDIContext)context).getBeanManager();
      BeanManager expectedManager = null;
      try {
        expectedManager = (BeanManager)new InitialContext().lookup("java:comp/BeanManager");
      }
      catch (NamingException notFound1) {
        try {
          expectedManager = (BeanManager)new InitialContext().lookup("java:comp/env/BeanManager");
        }
        catch (NamingException notFound2) {
          // Empty
        }
      }
      if (manager.equals(expectedManager)) {
        return Response.ok("pass");
      } else {
        return Response.ok("not the same bean manager");
      }
    } else {
      return Response.ok("not a cdi injection context");
    }
  }
}
