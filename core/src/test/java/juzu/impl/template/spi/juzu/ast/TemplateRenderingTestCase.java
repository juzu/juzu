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

package juzu.impl.template.spi.juzu.ast;

import juzu.impl.template.spi.juzu.dialect.gtmpl.GroovyTemplateStub;
import juzu.io.AppendableStream;
import juzu.template.TemplateExecutionException;
import juzu.template.TemplateRenderContext;
import juzu.test.protocol.mock.MockPrinter;
import org.junit.Test;

import java.awt.*;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.util.Date;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class TemplateRenderingTestCase extends AbstractTemplateTestCase {

  private DateFormat dateFormatFR;
  private DateFormat dateFormatEN;

  @Override
  public void setUp() throws Exception {
    super.setUp();

    //
    dateFormatFR = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.FRANCE);
    dateFormatEN = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.ENGLISH);
  }

/*
   public void testOutputStreamWriter() throws Exception
   {
      GroovyTemplate template = new GroovyTemplate("a<%='b'%>c<%out.print('d');%>e");
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      OutputStreamPrinter writer = new OutputStreamPrinter(CharsetTextEncoder.getUTF8(), baos);
      template.render(writer);
      writer.close();
      assertEquals("abcde", baos.toString("UTF-8"));
   }
*/

  @Test
  public void testDate1() throws Exception {
    Date dateToTest = new Date(0);
    String template = "<% print(new Date(0)); %>";
    assertEquals(dateFormatFR.format(dateToTest), render(template, Locale.FRENCH));
    assertEquals(dateFormatEN.format(dateToTest), render(template, Locale.ENGLISH));
    assertEquals(dateToTest.toString(), render(template));
  }

  @Test
  public void testDate2() throws Exception {
    Date dateToTest = new Date(0);
    String template = "<% def date = new Date(0) %>${date}";
    assertEquals(dateFormatFR.format(dateToTest), render(template, Locale.FRENCH));
    assertEquals(dateFormatEN.format(dateToTest), render(template, Locale.ENGLISH));
    assertEquals(dateToTest.toString(), render(template));
  }

  @Test
  public void testDate3() throws Exception {
    Date dateToTest = new Date(0);
    String template = "<%= new Date(0) %>";
    assertEquals(dateFormatFR.format(dateToTest), render(template, Locale.FRENCH));
    assertEquals(dateFormatEN.format(dateToTest), render(template, Locale.ENGLISH));
    assertEquals(dateToTest.toString(), render(template));
  }

  @Test
  public void testFoo() throws Exception {
    String template = "a";
    String render = render(template);
    assertEquals("a", render);
  }

  @Test
  public void testBar() throws Exception {
    String template = "<%='a'%>";
    String render = render(template);
    assertEquals("a", render);
  }

  @Test
  public void testFooBar() throws Exception {
    String template = "a<%='b'%>c";
    String render = render(template);
    assertEquals("abc", render);
  }

  @Test
  public void testJuu() throws Exception {
    String template = "<% out.print(\"a\"); %>";
    String render = render(template);
    assertEquals("a", render);
  }

  @Test
  public void testLineBreak() throws Exception {
    String template = "\n";
    String render = render(template);
    assertEquals("\n", render);
  }

  @Test
  public void testMultiLine() throws Exception {
    String template =
      "a\n" +
        "b\n" +
        "<%= 'c' %>\n" +
        "d";
    String render = render(template);
    assertEquals("a\nb\nc\nd", render);
  }

  @Test
  public void testIf() throws Exception {
    String template =
      "a\n" +
        "<% if (true) {\n %>" +
        "b\n" +
        "<% } %>";
    String s = render(template);
    assertEquals("a\nb\n", s);
  }

  @Test
  public void testLineComment() throws Exception {
    String template = "<% // foo %>a\nb";
    String s = render(template);
    assertEquals("a\nb", s);
  }

  @Test
  public void testContextResolution() throws Exception {
    String template = "<%= foo %>";
    Map<String, String> context = new HashMap<String, String>();
    context.put("foo", "bar");
    String s = render(template, context);
    assertEquals("bar", s);
  }

  @Test
  public void testDollarInExpression() throws Exception {
    String template = "<%= \"$foo\" %>";
    Map<String, String> context = new HashMap<String, String>();
    context.put("foo", "bar");
    String s = render(template, context);
    assertEquals("bar", s);
  }

  @Test
  public void testEscapeDollarInExpression() throws Exception {
    String template = "<%= \"\\$foo\" %>";
    Map<String, String> context = new HashMap<String, String>();
    context.put("foo", "bar");
    String s = render(template, context);
    assertEquals("$foo", s);
  }

  @Test
  public void testEscapeDollarInText() throws Exception {
    String template = "\\$foo";
    Map<String, String> context = new HashMap<String, String>();
    context.put("foo", "bar");
    String s = render(template, context);
    assertEquals("$foo", s);
  }

  @Test
  public void testDollarInScriplet() throws Exception {
    String template = "<% out.print(\"$foo\") %>";
    Map<String, String> context = new HashMap<String, String>();
    context.put("foo", "bar");
    String s = render(template, context);
    assertEquals("bar", s);
  }

  @Test
  public void testEscapeDollarInScriplet() throws Exception {
    String template = "<% out.print(\"\\$foo\") %>";
    Map<String, String> context = new HashMap<String, String>();
    context.put("foo", "bar");
    String s = render(template, context);
    assertEquals("$foo", s);
  }

  @Test
  public void testQuote() throws Exception {
    String template = "\"";
    String s = render(template);
    assertEquals("\"", s);
  }

  @Test
  public void testNoArgURL() throws Exception {
    String s = render("@{foo()}");
    assertEquals("foo_value", s);
  }

//   public void testSingleArgURL() throws Exception
//   {
//      String s = render("@{echo('julien')}");
//      assertEquals("julien", s);
//   }

  public static String foo() {
    return "foo_value";
  }

  public static String echo(String s) {
    return s;
  }

/*
   public void testFooFoo() throws Exception
   {
      InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("UIPortalApplication.gtmpl");
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      byte[] buffer = new byte[256];
      for (int l = in.read(buffer);l != -1;l = in.read(buffer))
      {
         baos.write(buffer, 0, l);
      }
      String gtmpl = baos.toString("UTF-8");
      GroovyTemplate template = new GroovyTemplate(gtmpl);
   }
*/

  @Test
  public void testException() throws Exception {
    String template = "<% throw new java.awt.AWTException(); %>";
    try {
      render(template);
      fail();
    }
    catch (TemplateExecutionException e) {
      assertTrue(e.getCause() instanceof AWTException);
    }
  }

  @Test
  public void testRuntimeException() throws Exception {
    String template = "<% throw new java.util.EmptyStackException(); %>";
    try {
      render(template);
      fail();
    }
    catch (TemplateExecutionException e) {
      assertTrue(e.getCause() instanceof EmptyStackException);
    }
  }

  @Test
  public void testSiblingClosures() throws IOException {
    GroovyTemplateStub template = template("#{title value=a/}#{title value=b/}");
    template.getClassName();
  }

  @Test
  public void testIOException() throws Exception {
    String template = "<% throw new java.io.IOException(); %>";
    try {
      render(template);
      fail();
    }
    catch (IOException e) {
    }

    //
    try {
      render("foobar", null, null, new Appendable() {
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
      fail();
    }
    catch (IOException e) {
    }
  }

  @Test
  public void testError() throws Exception {
    String template = "<% throw new java.awt.AWTError(); %>";
    try {
      render(template);
      fail();
    }
    catch (AWTError e) {
    }
  }

  @Test
  public void testThrowable() throws Exception {
    String template = "<% throw new Throwable(); %>";
    try {
      render(template);
      fail();
    }
    catch (Throwable t) {
    }
  }

  @Test
  public void testScriptLineNumber() throws Exception {
    testLineNumber("<%");
    assertLineNumber(2, "throw new Exception('e')", "<%\nthrow new Exception('e')%>");
  }

  @Test
  public void testExpressionLineNumber() throws Exception {
    testLineNumber("<%=");
  }

  private void testLineNumber(String prolog) throws Exception {
    assertLineNumber(1, "throw new Exception('a')", prolog + "throw new Exception('a')%>");
    assertLineNumber(1, "throw new Exception('b')", "foo" + prolog + "throw new Exception('b')%>");
    assertLineNumber(2, "throw new Exception('c')", "foo\n" + prolog + "throw new Exception('c')%>");
    assertLineNumber(1, "throw new Exception('d')", "<%;%>foo" + prolog + "throw new Exception('d')%>");
  }

  public static Object out;

  @Test
  public void testWriterAccess() throws Exception {
    out = null;
    Writer writer = new StringWriter();
    GroovyTemplateStub template = template("<% " + TemplateRenderingTestCase.class.getName() + ".out = out; %>");
    new TemplateRenderContext(template).render(new AppendableStream(writer));
    assertNotNull(out);
  }

  private void assertLineNumber(int expectedLineNumber, String expectedText, String script) throws IOException {
    GroovyTemplateStub template = template(script);
    try {
      new TemplateRenderContext(template).render(new MockPrinter());
      fail();
    }
    catch (TemplateExecutionException t) {
      assertEquals(expectedText, t.getText());
      assertEquals(expectedLineNumber, (Object)t.getLineNumber());
      StackTraceElement scriptElt = null;
      for (StackTraceElement elt : t.getCause().getStackTrace()) {
        if (elt.getClassName().equals(template.getClassName())) {
          scriptElt = elt;
          break;
        }
      }
      assertEquals(expectedLineNumber, scriptElt.getLineNumber());
    }
  }
}
