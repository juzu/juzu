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

package juzu.test;

import juzu.impl.common.Tools;
import org.eclipse.jdt.internal.compiler.tool.EclipseCompiler;

import javax.inject.Provider;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import java.io.Writer;
import java.util.ArrayList;

/**
 * Abstract compiler.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public enum JavaCompilerProvider implements Provider<JavaCompiler> {

  JAVAC() {
    public JavaCompiler get() {
      return ToolProvider.getSystemJavaCompiler();
    }
  },

  ECJ() {
    public JavaCompiler get() {
      return new EclipseCompiler() {

        @Override
        public CompilationTask getTask(Writer out, JavaFileManager fileManager, DiagnosticListener<? super JavaFileObject> someDiagnosticListener, Iterable<String> options, Iterable<String> classes, Iterable<? extends JavaFileObject> compilationUnits) {

          // Trick : we remove the test resources so Eclipse won't find them
          // see the pom.xml
          String prevClassPath = System.getProperty("java.class.path");
          ArrayList<String> nextClassPath = new ArrayList<String>();
          for (String s : Tools.split(prevClassPath, ':')) {
            if (!s.endsWith("/src/test/resources")) {
              nextClassPath.add(s);
            }
          }
          System.setProperty("java.class.path", Tools.join(':', nextClassPath));

          //
          try {
            return super.getTask(out, fileManager, someDiagnosticListener, options, classes, compilationUnits);
          }
          finally {
            System.setProperty("java.class.path", Tools.join(':', prevClassPath));
          }
        }
      };
    }
  };

  public abstract JavaCompiler get();

  /** . */
  public static final JavaCompilerProvider DEFAULT;

  static {
    String compilerProperty = System.getProperty("juzu.test.compiler");
    JavaCompilerProvider compiler = JAVAC;
    try {
      if (compilerProperty != null) {
        compiler = JavaCompilerProvider.valueOf(compilerProperty.toUpperCase());
      }
    }
    catch (Throwable ignore) {
    }
    DEFAULT = compiler;
  }
}
