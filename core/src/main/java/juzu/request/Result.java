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
package juzu.request;

import juzu.PropertyMap;
import juzu.Response;
import juzu.impl.common.Formatting;
import juzu.io.Streamable;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

/**
 * The result of a request.
 *
 * @author Julien Viet
 */
public class Result {

  public abstract static class Simple extends Result {

    /** . */
    public final PropertyMap properties;

    protected Simple(PropertyMap properties) {
      this.properties = properties;
    }
  }

  public static class Error extends Simple {

    /** . */
    public final List<StackTraceElement> at;

    /** . */
    public final Throwable cause;

    /** . */
    public final String message;

    public Error(PropertyMap properties, List<StackTraceElement> at, Throwable cause, String message) {
      super(properties);

      //
      this.at = at;
      this.cause = cause;
      this.message = message;
    }

    public Status asStatus(boolean verbose) {
      Response.Status response = Response.status(500);
      if (verbose) {
        StringBuilder buffer = new StringBuilder();
        Formatting.renderStyleSheet(buffer);
        buffer.append("<div class=\"juzu\">");
        buffer.append("<h1>Oups something went wrong</h1>");
        if (cause != null) {
          Formatting.renderThrowable(null, buffer, cause);
        } else {
          buffer.append(message);
        }
        buffer.append("</div>");
        response = response.content(buffer).withMimeType("text/html");
      }
      return response.result();
    }
  }

  public static class View extends Simple {

    /** . */
    public final Dispatch dispatch;

    public View(PropertyMap properties, Dispatch dispatch) {
      super(properties);

      //
      this.dispatch = dispatch;
    }
  }

  public static class Redirect extends Simple {

    /** . */
    public final String location;

    public Redirect(PropertyMap properties, String location) {
      super(properties);
      this.location = location;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this) {
        return true;
      }
      if (obj instanceof Redirect) {
        Redirect that = (Redirect)obj;
        return location.equals(that.location);
      }
      return false;
    }

    @Override
    public String toString() {
      return "Result.Redirect[location" + location + "]";
    }
  }

  public static class Status extends Result {

    /** . */
    public final int code;

    /** . */
    public final boolean decorated;

    /** . */
    public final Streamable streamable;

    public Status(int code, boolean decorated, Streamable streamable) {
      this.code = code;
      this.streamable = streamable;
      this.decorated = decorated;
    }
  }
}
