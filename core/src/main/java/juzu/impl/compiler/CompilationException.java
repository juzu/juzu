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

package juzu.impl.compiler;

import juzu.Response;
import juzu.impl.common.Formatting;
import juzu.io.UndeclaredIOException;
import juzu.request.Result;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class CompilationException extends Exception {

  /** . */
  private List<CompilationError> errors;

  public CompilationException(List<CompilationError> errors) {
    this.errors = errors;
  }

  public List<CompilationError> getErrors() {
    return errors;
  }

  @Override
  public String toString() {
    return "CompilationException[" + errors + "]";
  }

  public Result.Error result() {
    try {
      StringWriter writer = new StringWriter();
      PrintWriter printer = new PrintWriter(writer);
      Formatting.renderErrors(printer, errors);
      return Response.error(writer.getBuffer().toString()).result();
    }
    catch (IOException e) {
      // Should not happen
      throw new AssertionError(e);
    }
  }
}
