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

package juzu.templating.mustache;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.DefaultMustacheVisitor;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheVisitor;
import com.github.mustachejava.TemplateContext;
import juzu.impl.template.spi.TemplateStub;
import juzu.io.Chunk;
import juzu.io.Stream;
import juzu.template.TemplateExecutionException;
import juzu.template.TemplateRenderContext;

import java.io.IOException;
import java.io.StringWriter;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class TemplateStubImpl extends TemplateStub {

  /** . */
  private Mustache mustache;

  /** . */
  private final String resourceRoot;

  /** . */
  private final String mustacheName;

  public TemplateStubImpl(String id) {
    super(id);

    //
    int index = id.lastIndexOf('.');
    String resourceRoot = id.substring(0, index).replace('.', '/') + "/";
    String name = id.substring(index + 1) + ".mustache";

    //
    this.resourceRoot = resourceRoot;
    this.mustacheName = name;
  }

  @Override
  protected void doInit(ClassLoader loader) {
    ClassLoader previous = Thread.currentThread().getContextClassLoader();
    try {
      Thread.currentThread().setContextClassLoader(loader);
      DefaultMustacheFactory factory = new DefaultMustacheFactory(resourceRoot) {
        @Override
        public MustacheVisitor createMustacheVisitor() {
          return new DefaultMustacheVisitor(this) {
            @Override
            public void pragma(TemplateContext templateContext, String pragma, String args) {
              if ("param".equals(pragma)) {
                // Do nothing
              } else {
                super.pragma(templateContext, pragma, args);
              }
            }
          };
        }
      };
      mustache = factory.compile(mustacheName);
    }
    finally {
      Thread.currentThread().setContextClassLoader(previous);
    }
  }

  @Override
  protected void doRender(TemplateRenderContext renderContext) throws TemplateExecutionException, IOException {
    StringWriter buffer = new StringWriter();
    mustache.execute(buffer, new Object[]{new ContextMap(renderContext)});
    Stream stream = renderContext.getPrinter();
    stream.provide(Chunk.create(buffer.getBuffer()));
  }
}
