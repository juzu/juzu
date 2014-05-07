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
package juzu.plugin.closurecompiler;

import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.JSError;
import com.google.javascript.jscomp.Result;
import com.google.javascript.jscomp.SourceFile;
import juzu.plugin.asset.Minifier;

import com.google.javascript.jscomp.Compiler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;

/**
 * @author Julien Viet
 */
public class ClosureMinifier implements Minifier {

  @Override
  public InputStream minify(String name, String type, InputStream stream) throws IOException {
    if (type.equals("script")) {
      Compiler compiler = new Compiler();
      CompilerOptions options = new CompilerOptions();
      SourceFile source = SourceFile.fromInputStream(name, stream);
      Result result = compiler.compile(Collections.<SourceFile>emptyList(), Collections.singletonList(source), options);
      if (result.errors.length > 0) {
        StringWriter buffer = new StringWriter();
        PrintWriter writer = new PrintWriter(buffer);
        writer.println("Malformed asset:");
        for (JSError error : result.errors) {
          writer.println(error);
        }
        throw new IOException(buffer.toString());
      }
      String s = compiler.toSource();
      return new ByteArrayInputStream(s.getBytes());
    } else {
      throw new IOException("Can only process scripts and not " + type + " asset");
    }
  }
}
