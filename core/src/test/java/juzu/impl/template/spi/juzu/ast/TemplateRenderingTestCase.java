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

package juzu.impl.template.spi.juzu.ast;

import juzu.impl.common.Tools;
import juzu.impl.template.spi.TemplateException;
import juzu.impl.template.spi.juzu.dialect.gtmpl.GroovyTemplateStub;
import juzu.io.OutputStream;
import juzu.template.TemplateExecutionException;
import juzu.template.TemplateRenderContext;
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
  public void testMessage() throws Exception {
    String template = "&{a}";
    String render = render(template);
    assertEquals("MessageKey[a]", render);
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
    Map<String, Object> context = new HashMap<String, Object>();
    context.put("foo", "bar");
    String s = render(template, context);
    assertEquals("bar", s);
  }

  @Test
  public void testDollarInExpression() throws Exception {
    String template = "<%= \"$foo\" %>";
    Map<String, Object> context = new HashMap<String, Object>();
    context.put("foo", "bar");
    String s = render(template, context);
    assertEquals("bar", s);
  }

  @Test
  public void testEscapeDollarInExpression() throws Exception {
    String template = "<%= \"\\$foo\" %>";
    Map<String, Object> context = new HashMap<String, Object>();
    context.put("foo", "bar");
    String s = render(template, context);
    assertEquals("$foo", s);
  }

  @Test
  public void testEscapeDollarInText() throws Exception {
    String template = "\\$foo";
    Map<String, Object> context = new HashMap<String, Object>();
    context.put("foo", "bar");
    String s = render(template, context);
    assertEquals("$foo", s);
  }

  @Test
  public void testDollarInScriplet() throws Exception {
    String template = "<% out.print(\"$foo\") %>";
    Map<String, Object> context = new HashMap<String, Object>();
    context.put("foo", "bar");
    String s = render(template, context);
    assertEquals("bar", s);
  }

  @Test
  public void testEscapeDollarInScriplet() throws Exception {
    String template = "<% out.print(\"\\$foo\") %>";
    Map<String, Object> context = new HashMap<String, Object>();
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
  public void testSiblingClosures() throws IOException, TemplateException {
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
    new TemplateRenderContext(template).render(OutputStream.create(Tools.UTF_8, writer));
    assertNotNull(out);
  }

  private void assertLineNumber(int expectedLineNumber, String expectedText, String script) throws IOException, TemplateException {
    GroovyTemplateStub template = template(script);
    try {
      new TemplateRenderContext(template).render(OutputStream.create());
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
