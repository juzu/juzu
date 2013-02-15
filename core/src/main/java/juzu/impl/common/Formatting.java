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
package juzu.impl.common;

import juzu.bridge.portlet.JuzuPortlet;
import juzu.impl.compiler.CompilationError;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URL;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Various utilities for formatting stuff.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class Formatting {

  /**
   * Append a script section that will add a <code>stylesheet</code> element in the head section of
   * the document.
   *
   * @param writer the writer
   * @throws IOException any io exception
   */
  public static void renderStyleSheet(Writer writer) throws IOException {
    // Get CSS
    URL cssURL = JuzuPortlet.class.getResource("juzu.css");
    String css = Tools.read(cssURL);
    css = css.replace("\"", "\\\"");
    css = css.replace("'", "\\'");
    css = css.replace("\n", "\\n");

    //
    writer.append("<script type='text/javascript'>\n");
    writer.append("var styleElement = document.createElement('style');\n");
    writer.append("var css = '");
    writer.append(css);
    writer.append("';\n");
    writer.append("styleElement.type = 'text/css';\n");
    writer.append("if (styleElement.styleSheet) {;\n");
    writer.append("styleElement.styleSheet.cssText = css;\n");
    writer.append("} else {\n");
    writer.append("styleElement.appendChild(document.createTextNode(css));\n");
    writer.append("}\n");
    writer.append("document.getElementsByTagName(\"head\")[0].appendChild(styleElement);\n");
    writer.append("</script>\n");
  }

  /**
   * Renders the throwable in the specified writer.
   *
   * @param writer the writer
   * @param t the throwable
   * @throws IOException any io exception
   */
  public static void renderThrowable(Writer writer, Throwable t) throws IOException {
    // Trim the stack trace to remove stuff we don't want to see
    int size = 0;
    StackTraceElement[] trace = t.getStackTrace();
    for (StackTraceElement element : trace) {
      if (element.getClassName().equals(JuzuPortlet.class.getName())) {
        break;
      }
      else {
        size++;
      }
    }
    StackTraceElement[] ourTrace = new StackTraceElement[size];
    System.arraycopy(trace, 0, ourTrace, 0, ourTrace.length);
    t.setStackTrace(ourTrace);

    // We hack a bit
    final AtomicBoolean open = new AtomicBoolean(false);
    PrintWriter formatter = new PrintWriter(writer) {
      @Override
      public void println(Object x) {
        if (open.get()) {
          super.append("</ul></pre>");
        }
        super.append("<div class=\"juzu-message\">");
        super.append(String.valueOf(x));
        super.append("</div>");
        open.set(false);
      }

      @Override
      public void println(String x) {
        if (!open.get()) {
          super.append("<pre><ul>");
          open.set(true);
        }
        super.append("<li><span>");
        super.append(x);
        super.append("</span></li>");
      }

      @Override
      public void println() {
        // Do nothing
      }
    };

    //
    writer.append("<div class=\"juzu\">");
    writer.append("<div class=\"juzu-box\">");

    // We hack a bit with our formatter
    t.printStackTrace(formatter);

    //
    if (open.get()) {
      writer.append("</ul></pre>");
    }

    //
    writer.append("</div>");
    writer.append("</div>");
  }

  /**
   * Renders an iterable of {@link CompilationError} in the specified appendable.
   *
   * @param writer the writer
   * @param errors the errors
   * @throws IOException any io exception
   */
  public static void renderErrors(Writer writer, Iterable<CompilationError> errors) throws IOException {
    renderStyleSheet(writer);

    //
    writer.append("<div class=\"juzu\">");
    for (CompilationError error : errors) {
      writer.append("<div class=\"juzu-box\">");
      writer.append("<div class=\"juzu-message\">").append(error.getMessage()).append("</div>");

      // Display the source code
      File source = error.getSourceFile();
      if (source != null) {
        int line = error.getLocation().getLine();
        int from = line - 2;
        int to = line + 3;
        BufferedReader reader = new BufferedReader(new FileReader(source));
        int count = 1;
        writer.append("<pre><ol start=\"").append(String.valueOf(from)).append("\">");
        for (String s = reader.readLine();s != null;s = reader.readLine()) {
          if (count >= from && count < to) {
            if (count == line) {
              writer.append("<li><span class=\"error\">").append(s).append("</span></li>");
            }
            else {
              writer.append("<li><span>").append(s).append("</span></li>");
            }
          }
          count++;
        }
        writer.append("</ol></pre>");
      }
      writer.append("</div>");
    }
    writer.append("</div>");
  }
}
