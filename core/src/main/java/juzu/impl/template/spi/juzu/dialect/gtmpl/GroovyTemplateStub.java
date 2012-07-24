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
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import juzu.impl.template.spi.TemplateStub;
import juzu.template.TemplateExecutionException;
import juzu.template.TemplateRenderContext;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class GroovyTemplateStub extends TemplateStub {

  /** . */
  protected final String templateId;

  /** . */
  private Class<?> scriptClass;

  /** . */
  private HashMap<Integer, Foo> locationTable;

  protected GroovyTemplateStub() {
    String name = getClass().getName();
    this.templateId = name.substring(0, name.length() - 1); // Remove trailing _
  }

  public GroovyTemplateStub(String templateId) {
    this.templateId = templateId;
    this.scriptClass = null;
    this.locationTable = null;
  }

  @Override
  public void doInit(ClassLoader loader) {
    CompilerConfiguration config = new CompilerConfiguration();
    config.setScriptBaseClass(BaseScript.class.getName());
    String script = getScript();
    GroovyCodeSource gcs = new GroovyCodeSource(new ByteArrayInputStream(script.getBytes()), "myscript", "/groovy/shell");
    GroovyClassLoader gcl = new GroovyClassLoader(loader, config);
    try {
      scriptClass = gcl.parseClass(gcs, false);
      Class<?> constants = scriptClass.getClassLoader().loadClass("Constants");
      locationTable = (HashMap<Integer, Foo>)constants.getField("TABLE").get(null);
    }
    catch (Exception e) {
      throw new UnsupportedOperationException("handle me gracefully", e);
    }
  }

  public abstract String getScript();

  public String getClassName() {
    return scriptClass != null ? scriptClass.getName() : null;
  }

  @Override
  public void doRender(TemplateRenderContext renderContext) throws TemplateExecutionException, IOException {
    BaseScript script = (BaseScript)InvokerHelper.createScript(scriptClass, renderContext.getAttributes() != null ? new Binding(renderContext.getAttributes()) : new Binding());
    script.init(renderContext);

    //
    try {
      script.run();
    }
    catch (Exception e) {
      if (e instanceof IOException) {
        throw (IOException)e;
      }
      else {
        throw buildRuntimeException(e);
      }
    }
    catch (Throwable e) {
      if (e instanceof Error) {
        throw ((Error)e);
      }
      throw buildRuntimeException(e);
    }
  }

  private TemplateExecutionException buildRuntimeException(Throwable t) {
    StackTraceElement[] trace = t.getStackTrace();

    //
    Foo firstItem = null;

    // Try to find the groovy script lines
    for (int i = 0;i < trace.length;i++) {
      StackTraceElement element = trace[i];
      if (element.getClassName().equals(scriptClass.getName())) {
        int lineNumber = element.getLineNumber();
        Foo item = locationTable.get(lineNumber);
        int templateLineNumber;
        if (item != null) {
          templateLineNumber = item.getPosition().getLine();
          if (firstItem == null) {
            firstItem = item;
          }
        }
        else {
          templateLineNumber = -1;
        }
        element = new StackTraceElement(
          element.getClassName(),
          element.getMethodName(),
          element.getFileName(),
          templateLineNumber);
        trace[i] = element;
      }
    }

    //
    t.setStackTrace(trace);

    //
    if (firstItem != null) {
      return new TemplateExecutionException(templateId, firstItem.getPosition(), firstItem.getValue(), t);
    }
    else {
      return new TemplateExecutionException(templateId, null, null, t);
    }
  }
}
