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

package juzu.impl.tags;

import juzu.impl.template.spi.TemplateStub;
import juzu.io.UndeclaredIOException;
import juzu.template.Renderable;
import juzu.template.TagHandler;
import juzu.template.TemplateExecutionException;
import juzu.template.TemplateRenderContext;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/** @author Julien Viet */
public class SimpleTag extends TagHandler {

  /** . */
  private final String className;

  /** . */
  private final TemplateStub stub;

  // Compile time
  public SimpleTag(String name, String className) {
    super(name);

    //
    this.className = className;
    this.stub = null;
  }

  // Runtime
  public SimpleTag(String name, TemplateStub stub) {
    super(name);

    // Init stub
    stub.init(Thread.currentThread().getContextClassLoader());

    //
    this.className = getClass().getName();
    this.stub = stub;
  }

  @Override
  public String getClassName() {
    return className;
  }

  @Override
  public void render(final TemplateRenderContext context, final Renderable body, Map<String, String> args) throws IOException {
    final Object _parameters = context.setAttribute("parameters", args != null ? args : Collections.emptyMap());
    Renderable wrappedBody = new Renderable() {
      public void render(TemplateRenderContext context) throws TemplateExecutionException, UndeclaredIOException {
        Object prev = context.setAttribute("parameters", _parameters);
        try {
          body.render(context);
        }
        finally {
          context.setAttribute("parameters", prev);
        }
      }
    };
    InsertTag.current.get().addLast(wrappedBody);
    try {
      stub.render(context);
    }
    finally {
      InsertTag.current.get().removeLast();
      context.setAttribute("parameters", _parameters);
    }
  }
}
