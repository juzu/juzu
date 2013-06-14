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
import juzu.io.OutputStream;
import juzu.View;
import juzu.io.Stream;
import juzu.template.Template;

import javax.inject.Inject;
import java.io.IOException;
import java.util.LinkedList;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class A {

  @Inject
  @Path("index.gtmpl")
  Template index;

  @View
  public Response.Content index() {
    Stream printer = OutputStream.create(Tools.UTF_8, new Appendable() {
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
    index.renderTo(printer);
    final LinkedList<Throwable> errors = new LinkedList<Throwable>();
    printer.close(new Thread.UncaughtExceptionHandler() {
      public void uncaughtException(Thread t, Throwable e) {
        errors.add(e);
      }
    });
    String ret;
    if (errors.size() == 1 && errors.get(0) instanceof IOException) {
      ret = "pass";
    } else {
      ret = "fail";
    }
    return Response.ok(ret);
  }
}
