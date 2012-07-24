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
