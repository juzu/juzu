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

package bridge.inject.spring;

import juzu.Response;
import juzu.impl.inject.spi.InjectionContext;

import javax.inject.Inject;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class A {


  @Inject
  SpringBean bean;

  @Inject
  InjectionContext ioc;

  @juzu.View
  public Response.Content index() throws Exception {
    Object bean = ioc.resolveBean("foo");
    if (this.bean != null) {
      Object context = ioc.createContext(bean);
      Object instance = ioc.getInstance(bean, context);
      if (instance == this.bean) {
        return Response.ok("<span id='spring'>" + this.bean.value + "<span>");
      } else {
        return Response.ok("fail not same bean");
      }
    } else {
      return Response.ok("fail no bean");
    }
  }
}
