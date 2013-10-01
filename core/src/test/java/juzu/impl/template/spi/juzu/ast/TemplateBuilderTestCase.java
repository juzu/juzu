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
import juzu.impl.template.spi.juzu.dialect.gtmpl.GroovyTemplateStub;
import juzu.io.OutputStream;
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
    new TemplateRenderContext(s, Collections.<String, Object>singletonMap("foo", "b")).render(OutputStream.create(Tools.UTF_8, out));
    assertEquals("abc", out.toString());
  }

  @Test
  public void testCarriageReturn() throws Exception {
    GroovyTemplateStub s = template("a\r\nb");
    s.init(Thread.currentThread().getContextClassLoader());
    StringWriter out = new StringWriter();
    new TemplateRenderContext(s, Collections.<String, Object>emptyMap()).render(OutputStream.create(Tools.UTF_8, out));
    assertEquals("a\nb", out.toString());
  }
}
