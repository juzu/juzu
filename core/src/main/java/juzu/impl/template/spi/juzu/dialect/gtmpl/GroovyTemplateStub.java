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
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import groovy.lang.Script;
import juzu.impl.template.spi.TemplateStub;
import juzu.template.TemplateExecutionException;
import juzu.template.TemplateRenderContext;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class GroovyTemplateStub extends TemplateStub {

  /** . */
  private Class<?> scriptClass;

  /** . */
  private HashMap<Integer, Foo> locationTable;

  public GroovyTemplateStub(ClassLoader loader, String templateId) {
    super(loader, templateId);

    //
    this.scriptClass = null;
    this.locationTable = null;
  }

  @Override
  public void doInit(ClassLoader loader) {

    // The class fqn
    String fqn = id + "_";

    // Load from class loader first
    try {
      scriptClass = loader.loadClass(fqn);
    }
    catch (ClassNotFoundException ignore) {
    }
    catch (NoClassDefFoundError ignore) {
    }

    // Compile class
    if (scriptClass == null) {
      CompilerConfiguration config = new CompilerConfiguration();
      String script = getScript(loader, fqn);
      GroovyCodeSource gcs = new GroovyCodeSource(new ByteArrayInputStream(script.getBytes()), "myscript", "/groovy/shell");
      GroovyClassLoader gcl = new GroovyClassLoader(loader, config);
      try {
        scriptClass = gcl.parseClass(gcs, false);
      }
      catch (Exception e) {
        throw new UnsupportedOperationException("handle me gracefully", e);
      }
    }

    // Load constants
    try {
      String simpleName;
      String prefix;
      int pos = id.lastIndexOf('.');
      if (pos != -1) {
        prefix = id.substring(0, pos + 1);
        simpleName = id.substring(pos + 1);
      } else {
        prefix = "";
        simpleName = id;
      }
      String constantsName = prefix + "C" + simpleName;
      Class<?> constants = scriptClass.getClassLoader().loadClass(constantsName);
      locationTable = (HashMap<Integer, Foo>)constants.getField("TABLE").get(null);
    }
    catch (Exception e) {
      throw new UnsupportedOperationException("Handle me gracefully", e);
    }
  }

  public String getScript(ClassLoader loader, String fqn) {
    try {
      String path = fqn.replace('.', '/') + ".groovy";
      URL url = loader.getResource(path);
      if (url != null) {
        byte[] buffer = new byte[256];
        InputStream in = url.openStream();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int l = in.read(buffer);l != -1;l = in.read(buffer)) {
          baos.write(buffer, 0, l);
        }
        return baos.toString();
      }
      else {
        // Should log that
      }
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  public String getClassName() {
    return scriptClass != null ? scriptClass.getName() : null;
  }

  @Override
  public void doRender(TemplateRenderContext renderContext) throws TemplateExecutionException, IOException {
    Binding binding = new BindingImpl(renderContext);

    //
    Script script = InvokerHelper.createScript(scriptClass, binding);

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
      return new TemplateExecutionException(id, firstItem.getPosition(), firstItem.getValue(), t);
    }
    else {
      return new TemplateExecutionException(id, null, null, t);
    }
  }
}
