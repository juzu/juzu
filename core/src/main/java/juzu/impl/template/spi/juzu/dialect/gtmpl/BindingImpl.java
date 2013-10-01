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

package juzu.impl.template.spi.juzu.dialect.gtmpl;

import groovy.lang.Binding;
import juzu.template.TemplateRenderContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class BindingImpl extends Binding {

  /** . */
  private GroovyPrinter printer;

  /** . */
  private TemplateRenderContext renderContext;

  public BindingImpl(TemplateRenderContext renderContext) {
    super(renderContext.getAttributes());

    //
    this.printer = new GroovyPrinter(renderContext);
    this.renderContext = renderContext;
  }

  @Override
  public Object getVariable(String name) {
    Object value;
    if ("out".equals(name)) {
      value = printer;
    }
    else if ("renderContext".equals(name)) {
      value = renderContext;
    }
    else {
      try {
        value = renderContext.resolveBean(name);
      }
      catch (InvocationTargetException e) {
        Throwable cause = e.getCause();
        if (cause instanceof RuntimeException) {
          throw (RuntimeException)cause;
        }
        else if (cause instanceof Error) {
          throw (Error)cause;
        }
        else {
          throw new UndeclaredThrowableException(cause);
        }
      }
      if (value == null) {
        value = renderContext.getAttribute(name);
        if (value == null) {
          value = super.getVariable(name);
        }
      }
    }
    return value;
  }
}
