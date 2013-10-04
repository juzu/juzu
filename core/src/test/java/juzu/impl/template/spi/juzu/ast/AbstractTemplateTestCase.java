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

import juzu.impl.common.Name;
import juzu.impl.common.Tools;
import juzu.impl.compiler.ProcessingException;
import juzu.impl.tags.TitleTag;
import juzu.impl.template.spi.EmitContext;
import juzu.impl.template.spi.SimpleProcessContext;
import juzu.impl.template.spi.Template;
import juzu.impl.template.spi.TemplateException;
import juzu.impl.template.spi.juzu.compiler.ProcessPhase;
import juzu.impl.template.spi.juzu.dialect.gtmpl.GroovyTemplateEmitter;
import juzu.impl.template.spi.juzu.dialect.gtmpl.GroovyTemplateStub;
import juzu.impl.template.spi.juzu.compiler.EmitPhase;
import juzu.impl.common.MethodInvocation;
import juzu.impl.common.Path;
import juzu.io.OutputStream;
import juzu.template.TagHandler;
import juzu.template.TemplateExecutionException;
import juzu.template.TemplateRenderContext;
import juzu.test.AbstractTestCase;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class AbstractTemplateTestCase extends AbstractTestCase {

  public GroovyTemplateStub template(final String text) throws IOException, TemplateException {
    Name pkg = Name.parse("foo");
    Name name = Name.parse("index");
    Name fqn = pkg.append(name);
    Path.Absolute absolute = Path.absolute(fqn, ".gtmpl");
    Path.Relative relative = Path.relative(name, ".gtmpl");
    GroovyTemplateEmitter generator = new GroovyTemplateEmitter(fqn);
    try {
      ProcessPhase processPhase = new ProcessPhase(new SimpleProcessContext(Collections.<Path.Absolute, Template<?>>emptyMap()) {
        @Override
        public TagHandler resolveTagHandler(String name) {
          if ("title".equals(name)) {
            return new TitleTag();
          } else {
            return null;
          }
        }
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
      Template<ASTNode.Template> template = new Template<ASTNode.Template>(
          ASTNode.Template.parse(text),
          absolute,
          0);
      processPhase.process(template);

      // Emit
      EmitPhase emitPhase = new EmitPhase(new EmitContext(){
        @Override
        public TagHandler resolveTagHandler(String name) {
          if ("title".equals(name)) {
            return new TitleTag();
          } else {
            return null;
          }
        }

        public void createResource(Path.Absolute path, CharSequence content) throws IOException {
          throw new UnsupportedOperationException();
        }
      });
      emitPhase.emit(generator, template.getModel());
    }
    catch (juzu.impl.template.spi.juzu.ast.ParseException e) {
      throw failure(e);
    }
    GroovyTemplateStub stub = generator.build(fqn.toString());
    stub.init();
    return stub;
  }

  public String render(String template) throws IOException, TemplateExecutionException, TemplateException {
    return render(template, null, null);
  }

  public String render(String template, Locale locale) throws IOException, TemplateExecutionException, TemplateException {
    return render(template, null, locale);
  }

  public String render(String template, Map<String, Object> attributes) throws IOException, TemplateExecutionException, TemplateException {
    return render(template, attributes, null);
  }

  public String render(String text, Map<String, Object> attributes, Locale locale) throws IOException, TemplateExecutionException, TemplateException {
    StringWriter out = new StringWriter();
    render(text, attributes, locale, out);
    return out.toString();
  }

  public void render(String text, Map<String, Object> attributes, Locale locale, Appendable appendable) throws IOException, TemplateExecutionException, TemplateException {
    GroovyTemplateStub template = template(text);
    TemplateRenderContext renderContext = new TemplateRenderContext(template, null, attributes, locale);
    OutputStream adapter = OutputStream.create(Tools.UTF_8, appendable);
    renderContext.render(adapter);
    final AtomicReference<IOException> ios = new AtomicReference<IOException>();
    adapter.close(new Thread.UncaughtExceptionHandler() {
      public void uncaughtException(Thread t, Throwable e) {
        if (e instanceof IOException) {
          ios.set((IOException)e);
        }
      }
    });
    if (ios.get() != null) {
      throw ios.get();
    }
  }
}
