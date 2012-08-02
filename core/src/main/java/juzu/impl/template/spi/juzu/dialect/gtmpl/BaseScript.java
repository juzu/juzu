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
import groovy.lang.Script;
import juzu.impl.plugin.application.ApplicationException;
import juzu.template.TemplateRenderContext;
import org.codehaus.groovy.runtime.InvokerInvocationException;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class BaseScript extends Script {

  /** . */
  private GroovyPrinter printer;

  /** . */
  private TemplateRenderContext renderContext;

  protected BaseScript() {
  }

  protected BaseScript(Binding binding) {
    super(binding);
  }

  public void init(TemplateRenderContext renderContext) {
    this.printer = new GroovyPrinter(renderContext);
    this.renderContext = renderContext;
  }

  @Override
  public Object getProperty(String property) {
    Object value;
    if ("out".equals(property)) {
      value = printer;
    }
    else if ("renderContext".equals(property)) {
      value = renderContext;
    }
    else {
      try {
        value = renderContext.resolveBean(property);
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
        value = super.getProperty(property);
      }
    }
    return value;
  }

  @Override
  public void println(Object o) {
    try {
      printer.println(o);
    }
    catch (IOException e) {
      throw new InvokerInvocationException(e);
    }
  }

  @Override
  public void println() {
    try {
      printer.println();
    }
    catch (IOException e) {
      throw new InvokerInvocationException(e);
    }
  }

  @Override
  public void print(Object o) {
    try {
      printer.print(o);
    }
    catch (IOException e) {
      throw new InvokerInvocationException(e);
    }
  }
}
