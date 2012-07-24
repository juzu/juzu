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

package juzu.plugin.less.impl.lesser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class Lesser {

  /** . */
  private static final ThreadLocal<LessContext> current = new ThreadLocal<LessContext>();

  /** . */
  private static final ThreadLocal<Result> currentResult = new ThreadLocal<Result>();

  /** . */
  private final JSContext engine;

  public class Bridge {
    public String load(String name) {
      return current.get().load(name);
    }

    public void failure(String src, int line, int column, int index, String message, String type, String[] extract) {
      Failure failure = (Failure)currentResult.get();
      if (failure == null) {
        currentResult.set(failure = new Failure());
      }
      failure.errors.add(new LessError(src, line, column, index, message, type, extract));
    }

    public void compilation(String result) {
      currentResult.set(new Compilation(result));
    }
  }

  public Lesser(JSContext jsContext) throws Exception {
    InputStream lessIn = getClass().getResourceAsStream("less.js");

    //
    ByteArrayOutputStream baos = append(lessIn, new ByteArrayOutputStream());

    //
    jsContext.put("bridge", new Bridge());
    jsContext.eval("load = function(name) { return '' + bridge.load(name); }");
    jsContext.eval("failure = function(src, line, column, index, message, type, extract) { bridge.failure(src, line, column, index, message, type, extract); }");
    jsContext.eval("compilation = function(stylesheet) { bridge.compilation(stylesheet); }");
    jsContext.put("window", "{}");

    //
    jsContext.eval(baos.toString());

    //
    this.engine = jsContext;
  }

  public Result compile(LessContext context, String name) throws Exception {
    return compile(context, name, false);
  }

  public Result compile(LessContext context, String name, boolean compress) throws Exception {
    current.set(context);
    try {
      engine.invokeFunction("parse", name, compress);
      return currentResult.get();
    }
    finally {
      current.set(null);
      currentResult.set(null);
    }
  }

  static <O extends OutputStream> O append(InputStream in, O out) throws IOException {
    byte[] buffer = new byte[256];
    for (int l = in.read(buffer);l != -1;l = in.read(buffer)) {
      out.write(buffer, 0, l);
    }
    return out;
  }
}
