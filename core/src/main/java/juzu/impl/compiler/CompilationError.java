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

import juzu.impl.common.Location;

import java.io.File;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class CompilationError {

  /** . */
  private final MessageCode code;

  /** . */
  private final List<String> arguments;

  /** . */
  private final String source;

  /** The optional source file. */
  private final File sourceFile;

  /** . */
  private final Location location;

  /** . */
  private final String message;

  public CompilationError(MessageCode code, List<String> arguments, String source, File sourceFile, Location location, String message) {
    this.code = code;
    this.arguments = arguments;
    this.source = source;
    this.sourceFile = sourceFile;
    this.location = location;
    this.message = message;
  }

  public MessageCode getCode() {
    return code;
  }

  public List<String> getArguments() {
    return arguments;
  }

  public String getSource() {
    return source;
  }

  public Location getLocation() {
    return location;
  }

  public String getMessage() {
    return message;
  }

  public File getSourceFile() {
    return sourceFile;
  }

  @Override
  public String toString() {
    return "CompilationError[source=" + source + ",message=" + message + ",location=" + location + "]";
  }
}
