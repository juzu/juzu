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

import groovy.lang.Binding;
import juzu.impl.plugin.application.ApplicationException;
import juzu.template.TemplateRenderContext;

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
      catch (ApplicationException e) {
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
        value = super.getVariable(name);
      }
    }
    return value;
  }
}
