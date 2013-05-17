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

package plugin.template.ioe;

import juzu.Path;
import juzu.Response;
import juzu.impl.common.Tools;
import juzu.io.UndeclaredIOException;
import juzu.View;
import juzu.io.Stream;
import juzu.io.Streams;
import juzu.template.Template;

import javax.inject.Inject;
import java.io.IOException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class A {

  @Inject
  @Path("index.gtmpl")
  Template index;

  @View
  public Response.Content index() {
    String ret = "";
    Stream printer = Streams.appendable(Tools.UTF_8, new Appendable() {
      public Appendable append(CharSequence csq) throws IOException {
        throw new IOException();
      }

      public Appendable append(CharSequence csq, int start, int end) throws IOException {
        throw new IOException();
      }

      public Appendable append(char c) throws IOException {
        throw new IOException();
      }
    });
    try {
      index.renderTo(printer);
    }
    catch (UndeclaredIOException expected) {
      ret = "pass";
    }
    return Response.ok(ret);
  }
}
