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

package juzu.impl.template.spi.juzu.dialect.gtmpl;

import juzu.impl.template.spi.juzu.DialectTemplateEmitter;
import juzu.impl.template.spi.juzu.ast.SectionType;
import juzu.impl.common.Location;
import juzu.impl.common.Tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class GroovyTemplateEmitter extends DialectTemplateEmitter {

  /** . */
  private final String sep = (String)System.getProperty("line.separator");

  /** . */
  private StringBuilder out = new StringBuilder();

  /** . */
  private List<TextConstant> textMethods = new ArrayList<TextConstant>();

  /** . */
  private int methodCount = 0;

  /** The line number table. */
  private HashMap<Integer, Foo> locationTable = new HashMap<Integer, Foo>();

  /** The current line number. */
  private int lineNumber = 1;

  /** . */
  private Location pos = null;

  /** . */
  private int closureCount = 0;

  /** . */
  private int closureCountIndex = -1;

  /** . */
  private final int[] closureCountStack = new int[200];

  public GroovyTemplateEmitter() {
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();

    // Add main stuff
    builder.append(out.toString());

    //
    builder.append(sep);
    builder.append("public static class Constants").append(sep);
    builder.append("{").append(sep);

    // Add text constant
    for (TextConstant method : textMethods) {
      builder.append(method.getDeclaration()).append(sep);
    }

    // Add line table
    builder.append("public static final Map<Integer, ").append(Foo.class.getName()).append("> TABLE = ");
    if (locationTable.isEmpty()) {
      builder.append("[:]");
    }
    else {
      builder.append("[").append(sep);
      for (Iterator<Map.Entry<Integer, Foo>> i = locationTable.entrySet().iterator();i.hasNext();) {
        Map.Entry<Integer, Foo> entry = i.next();
        Foo text = entry.getValue();
        Location location = text.getPosition();
        builder.append(entry.getKey()).append(':').
          append("new ").append(Foo.class.getName()).append("(").
          append("new ").append(Location.class.getName()).append("(").append(location.getCol()).append(',').append(location.getLine()).append("),").
          append("'");
        Tools.escape(text.getValue(), builder);
        builder.append("')");
        if (i.hasNext()) {
          builder.append(",").append(sep);
        }
        else {
          builder.append(']');
        }
      }
    }
    builder.append(";").append(sep);

    // Close context
    builder.append("}").append(sep);

    //
    return builder.toString();
  }

  public GroovyTemplateStub build(String templateId) {
    final String script = toString();
    return new GroovyTemplateStub(templateId) {
      @Override
      public String getScript() {
        return script;
      }
    };
  }

  public void startScriptlet(Location beginPosition) {
    pos = beginPosition;
  }

  public void appendScriptlet(String scriptlet) {
    out.append(scriptlet);
    locationTable.put(lineNumber, new Foo(pos, scriptlet));
  }

  public void endScriptlet() {
    // We append a line break because we want that any line comment does not affect the template
    out.append(sep);
    lineNumber++;
  }

  public void startExpression(Location beginPosition) {
    pos = beginPosition;
    out.append(";out.print(\"${");
  }

  public void appendExpression(String expr) {
    out.append(expr);
    locationTable.put(lineNumber, new Foo(pos, expr));
  }

  public void endExpression() {
    out.append("}\");").append(sep);
    lineNumber++;
  }

  public void appendText(String text) {
    TextConstant m = new TextConstant("s" + methodCount++, text);
    out.append(";out.print(Constants.").append(m.name).append(");").append(sep);
    textMethods.add(m);
    lineNumber++;
  }

  public void appendLineBreak(SectionType currentType, Location position) {
    this.pos = new Location(1, position.getLine() + 1);
    switch (currentType) {
      case SCRIPTLET:
        out.append(sep);
        lineNumber++;
        break;
      case EXPR:
        out.append(sep);
        lineNumber++;
        break;
      default:
        throw new AssertionError();
    }
  }

  @Override
  public void url(String typeName, String methodName, List<String> args) {
    out.append(";out.print(");
    out.append(typeName);
    out.append(".");
    out.append(methodName);
    out.append("(");
    for (int i = 0;i < args.size();i++) {
      if (i > 0) {
        out.append(",");
      }
      String methodArg = args.get(i);
      out.append(methodArg);
    }
    out.append("));");
  }

  @Override
  public void openTag(String className, Map<String, String> args) {
    int count = closureCountStack[++closureCountIndex] = closureCount++;
    out.append("; def closure").append(count).append(" = { ");
  }

  @Override
  public void closeTag(String className, Map<String, String> args) {
    int count = closureCountStack[closureCountIndex--];
    out.append("; } as juzu.template.Renderable;");
    out.append("; new ").append(className).append("().render(out.renderContext, closure").append(count).append(",");
    if (args == null || args.isEmpty()) {
      out.append("null");
    }
    else {
      out.append("[");
      int index = 0;
      for (Map.Entry<String, String> entry : args.entrySet()) {
        if (index++ > 0) {
          out.append(",");
        }
        out.append("\"").append(entry.getKey()).append("\":\"").append(entry.getValue()).append("\".toString()");
      }
      out.append("]");
    }
    out.append(");");
  }

  @Override
  public void tag(String tagName, Map<String, String> args) {
    // throw new UnsupportedOperationException();
  }
}
