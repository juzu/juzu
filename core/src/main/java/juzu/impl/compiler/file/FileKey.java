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

package juzu.impl.compiler.file;

import juzu.impl.common.IterableArray;
import juzu.impl.common.Tools;

import javax.tools.JavaFileObject;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class FileKey {

  public static FileKey newResourceName(String packageName, String name) throws IOException {
    return new FileKey(packageName, name, JavaFileObject.Kind.OTHER);
  }

  public static FileKey newJavaName(String className, JavaFileObject.Kind kind) throws IOException {
    if (kind == JavaFileObject.Kind.SOURCE || kind == JavaFileObject.Kind.CLASS) {
      int pos = className.lastIndexOf('.');
      if (pos == -1) {
        return new FileKey("", className, kind);
      }
      else {
        return new FileKey(className.substring(0, pos), className.substring(pos + 1), kind);
      }
    }
    else {
      throw new IllegalArgumentException("Kind " + kind + " not accepted");
    }
  }

  public static FileKey newJavaName(String packageName, String name) throws IOException {
    JavaFileObject.Kind kind;
    if (name.endsWith(".java")) {
      kind = JavaFileObject.Kind.SOURCE;
    }
    else if (name.endsWith(".class")) {
      kind = JavaFileObject.Kind.CLASS;
    }
    else {
      throw new IllegalArgumentException("Illegal name " + name);
    }
    String rawName = name.substring(0, name.length() - kind.extension.length());
    return new FileKey(packageName, rawName, kind);
  }

  public static FileKey newName(String packageName, String name) throws IOException {
    JavaFileObject.Kind kind;
    if (name.endsWith(".java")) {
      kind = JavaFileObject.Kind.SOURCE;
    }
    else if (name.endsWith(".class")) {
      kind = JavaFileObject.Kind.CLASS;
    }
    else {
      kind = JavaFileObject.Kind.OTHER;
    }
    String rawName = name.substring(0, name.length() - kind.extension.length());
    return new FileKey(packageName, rawName, kind);
  }

  /** . */
  public final Iterable<String> packageNames;

  /** . */
  public final Iterable<String> names;

  /** . */
  public final String packageFQN;

  /** . */
  public final String rawName;

  /** . */
  public final String fqn;

  /** . */
  public final String name;

  /** . */
  public final URI uri;

  /** . */
  public final JavaFileObject.Kind kind;

  private FileKey(String packageFQN, String rawName, JavaFileObject.Kind kind) throws IOException {
    String name = rawName + kind.extension;
    String path;
    String fqn;
    if (packageFQN.length() == 0) {
      path = "/" + name;
      fqn = rawName;
    }
    else {
      path = "/" + packageFQN.replace('.', '/') + '/' + name;
      fqn = packageFQN + "." + rawName;
    }
    try {
      String[] abc = Tools.split(packageFQN, '.', 1);
      abc[abc.length - 1] = name;

      //
      this.packageNames = new IterableArray<String>(abc, 0, abc.length - 1);
      this.names = new IterableArray<String>(abc, 0, abc.length);
      this.packageFQN = packageFQN;
      this.rawName = rawName;
      this.uri = new URI(path);
      this.fqn = fqn;
      this.kind = kind;
      this.name = name;
    }
    catch (URISyntaxException e) {
      throw new IOException("Could not create path " + path, e);
    }
  }

  public FileKey as(JavaFileObject.Kind kind) throws IOException {
    return new FileKey(packageFQN, rawName, kind);
  }

  @Override
  public final int hashCode() {
    return packageFQN.hashCode() ^ rawName.hashCode() ^ kind.hashCode();
  }

  @Override
  public final boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    else if (obj instanceof FileKey) {
      FileKey that = (FileKey)obj;
      return packageFQN.equals(that.packageFQN) && rawName.equals(that.rawName) && kind.equals(that.kind);
    }
    else {
      return false;
    }
  }

  @Override
  public String toString() {
    return "FileKey[packageName=" + packageFQN + ",rawName=" + rawName + ",kind=" + kind + "]";
  }
}
