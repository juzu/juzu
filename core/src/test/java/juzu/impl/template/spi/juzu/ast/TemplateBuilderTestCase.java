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
import juzu.template.TemplateRenderContext;
import org.junit.Test;

import java.io.StringWriter;
import java.util.Collections;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class TemplateBuilderTestCase extends AbstractTemplateTestCase {

  @Test
  public void testFoo() throws Exception {
    GroovyTemplateStub s = template("a<%=foo%>c");
    s.init(Thread.currentThread().getContextClassLoader());
    StringWriter out = new StringWriter();
    new TemplateRenderContext(s, Collections.singletonMap("foo", "b")).render(new AppendableStream(out));
    assertEquals("abc", out.toString());
  }

  @Test
  public void testCarriageReturn() throws Exception {
    GroovyTemplateStub s = template("a\r\nb");
    s.init(Thread.currentThread().getContextClassLoader());
    StringWriter out = new StringWriter();
    new TemplateRenderContext(s, Collections.<String, Object>emptyMap()).render(new AppendableStream(out));
    assertEquals("a\nb", out.toString());
  }
}
