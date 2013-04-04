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

package juzu.impl.template.spi.juzu.dialect.gtmpl;

import groovy.lang.GString;
import groovy.lang.GroovyInterceptable;
import groovy.lang.GroovyObjectSupport;
import juzu.io.CharArray;
import juzu.template.TemplateRenderContext;
import org.codehaus.groovy.runtime.InvokerInvocationException;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class GroovyPrinter extends GroovyObjectSupport implements GroovyInterceptable {

  /** . */
  final TemplateRenderContext renderContext;

  public GroovyPrinter(TemplateRenderContext renderContext) throws NullPointerException {
    if (renderContext == null) {
      throw new NullPointerException("No null render context accepted");
    }

    //
    this.renderContext = renderContext;
  }

  /**
   * Optimize the call to the various print methods.
   *
   * @param name the method name
   * @param args the method arguments
   * @return the return value
   */
  @Override
  public Object invokeMethod(String name, Object args) {
    // Optimize access to print methods
    if (args instanceof Object[]) {
      Object[] array = (Object[])args;
      if (array.length == 1) {
        try {
          if ("print".equals(name)) {
            print(array[0]);
            return null;
          }
          else if ("println".equals(name)) {
            println(array[0]);
            return null;
          }
        }
        catch (IOException e) {
          throw new InvokerInvocationException(e);
        }
      }
    }

    // Back to Groovy method call
    return super.invokeMethod(name, args);
  }

  public final void println(Object o) throws IOException {
    print(o);
    println();
  }

  public final void println() throws IOException {
    renderContext.getPrinter().append('\n');
  }

  /**
   * We handle in this method a conversion of an object to another one for formatting purposes.
   *
   * @param o the object to format
   * @return the formatted object
   */
  private Object format(Object o) {
    if (o instanceof Date) {
      Locale locale = renderContext.getLocale();
      if (locale != null) {
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
        o = dateFormat.format((Date)o);
      }
    } else if (o instanceof MessageKey) {
      MessageKey key = (MessageKey)o;
      o = renderContext.resolveMessage(key);
    }

    //
    return o;
  }

  private String toString(Object o) {
    Object f = format(o);
    if (f == null) {
      return "null";
    }
    else if (f instanceof String) {
      return (String)f;
    }
    else {
      return o.toString();
    }
  }

  public final void print(Object o) throws IOException {
    if (o instanceof CharArray) {
      renderContext.getPrinter().append((CharArray)o);
    }
    else if (o instanceof GString) {
      GString gs = (GString)o;
      Object[] values = gs.getValues();
      for (int i = 0;i < values.length;i++) {
        values[i] = format(values[i]);
      }
      renderContext.getPrinter().append(o.toString());
    }
    else {
      renderContext.getPrinter().append(toString(o));
    }
  }
}
