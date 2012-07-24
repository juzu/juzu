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

import juzu.impl.compiler.ProcessingException;
import juzu.impl.template.spi.EmitContext;
import juzu.impl.template.spi.ProcessContext;
import juzu.impl.template.spi.Template;
import juzu.impl.template.spi.juzu.compiler.ProcessPhase;
import juzu.impl.template.spi.juzu.dialect.gtmpl.GroovyTemplateEmitter;
import juzu.impl.template.spi.juzu.dialect.gtmpl.GroovyTemplateStub;
import juzu.impl.template.spi.juzu.compiler.EmitPhase;
import juzu.impl.common.MethodInvocation;
import juzu.impl.common.Path;
import juzu.io.AppendableStream;
import juzu.template.TemplateExecutionException;
import juzu.template.TemplateRenderContext;
import juzu.test.AbstractTestCase;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class AbstractTemplateTestCase extends AbstractTestCase {

  public GroovyTemplateStub template(final String text) throws IOException {
    GroovyTemplateEmitter generator = new GroovyTemplateEmitter();
    try {
      ProcessPhase processPhase = new ProcessPhase(new ProcessContext(Collections.<Path, Template<?>>emptyMap()) {
        @Override
        public MethodInvocation resolveMethodInvocation(String typeName, String methodName, Map<String, String> parameterMap) throws ProcessingException {
          if (parameterMap.size() > 0) {
            throw failure("Unexpected non empty parameter map");
          }
          Class clazz = AbstractTemplateTestCase.this.getClass();
          try {
            Method m = clazz.getMethod(methodName);
            return new MethodInvocation(clazz.getName(), m.getName(), Collections.<String>emptyList());
          }
          catch (NoSuchMethodException e) {
            // Should we thrown a CompilationException instead ?
            throw failure(e);
          }
        }
      });
      Template<ASTNode.Template> template = new Template<ASTNode.Template>(Path.parse("index.gtmpl"), ASTNode.Template.parse(text), Path.parse("index.gtmpl"), 0);
      processPhase.process(template);

      // Emit
      EmitPhase emitPhase = new EmitPhase(new EmitContext());
      emitPhase.emit(generator, template.getModel());
    }
    catch (juzu.impl.template.spi.juzu.ast.ParseException e) {
      throw failure(e);
    }
    GroovyTemplateStub stub = generator.build("template_" + Math.abs(new Random().nextLong()));
    stub.init(Thread.currentThread().getContextClassLoader());
    return stub;
  }

  public String render(String template) throws IOException, TemplateExecutionException {
    return render(template, null, null);
  }

  public String render(String template, Locale locale) throws IOException, TemplateExecutionException {
    return render(template, null, locale);
  }

  public String render(String template, Map<String, ?> attributes) throws IOException, TemplateExecutionException {
    return render(template, attributes, null);
  }

  public String render(String text, Map<String, ?> attributes, Locale locale) throws IOException, TemplateExecutionException {
    StringWriter out = new StringWriter();
    render(text, attributes, locale, out);
    return out.toString();
  }

  public void render(String text, Map<String, ?> attributes, Locale locale, Appendable appendable) throws IOException, TemplateExecutionException {
    GroovyTemplateStub template = template(text);
    TemplateRenderContext renderContext = new TemplateRenderContext(template, null, attributes, locale);
    renderContext.render(new AppendableStream(appendable));
  }
}
