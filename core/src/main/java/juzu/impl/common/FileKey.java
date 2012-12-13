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

package juzu.impl.common;

import javax.tools.JavaFileObject;
import java.io.Serializable;
import java.util.EnumMap;

/**
 * A key for identifying a file.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class FileKey implements Serializable {

  /** . */
  private static final EnumMap<JavaFileObject.Kind, String> KIND_TO_EXT = new EnumMap<JavaFileObject.Kind, String>(JavaFileObject.Kind.class);

  static {
    KIND_TO_EXT.put(JavaFileObject.Kind.CLASS, "class");
    KIND_TO_EXT.put(JavaFileObject.Kind.SOURCE, "java");
  }

  public static FileKey newResourceName(String packageName, String name) {
    FileKey key = newName(packageName, name);
    if (key.getKind() == JavaFileObject.Kind.OTHER) {
      return key;
    } else {
      throw new IllegalArgumentException();
    }
  }

  public static FileKey newResourceName(String packageName, String rawName, String ext) {
    FileKey key = newName(packageName, rawName, ext);
    if (key.getKind() == JavaFileObject.Kind.OTHER) {
      return key;
    } else {
      throw new IllegalArgumentException();
    }
  }

  public static FileKey newJavaName(String className, JavaFileObject.Kind kind) {
    if (kind == JavaFileObject.Kind.SOURCE || kind == JavaFileObject.Kind.CLASS) {
      int pos = className.lastIndexOf('.');
      if (pos == -1) {
        return new FileKey("", className, KIND_TO_EXT.get(kind));
      }
      else {
        return new FileKey(className.substring(0, pos), className.substring(pos + 1), KIND_TO_EXT.get(kind));
      }
    }
    else {
      throw new IllegalArgumentException("Kind " + kind + " not accepted");
    }
  }

  public static FileKey newJavaName(String packageName, String name) {
    String ext;
    if (name.endsWith(".java")) {
      ext = "java";
    }
    else if (name.endsWith(".class")) {
      ext = "class";
    }
    else {
      throw new IllegalArgumentException("Illegal name " + name);
    }
    String rawName = name.substring(0, name.length() - 1 - ext.length());
    return new FileKey(packageName, rawName, ext);
  }

  public static FileKey newName(String packageName, String name) {
    int pos = name.lastIndexOf('.');
    String rawName;
    String ext;
    if (pos == -1) {
      rawName = name;
      ext = "";
    } else {
      rawName = name.substring(0, pos);
      ext = name.substring(pos + 1);
    }
    return newName(packageName, rawName, ext);
  }

  public static FileKey newName(String packageName, String rawName, String ext) {
    return new FileKey(packageName, rawName, ext);
  }

  /** . */
  public final Iterable<String> packageNames;

  /** . */
  public final Iterable<String> names;

  /** . */
  public final String packageFQN;

  /** . */
  public final String rawName;

  /** Not sure this make sense as a name could contain '.' . */
  public final String fqn;

  /** . */
  public final String name;

  /** . */
  public final String ext;

  private FileKey(String packageFQN, String rawName, String ext) {
    String name = rawName + "." + ext;
    String fqn;
    if (packageFQN.length() == 0) {
      fqn = rawName;
    }
    else {
      fqn = packageFQN + "." + rawName;
    }

    //
    String[] abc = Tools.split(packageFQN, '.', 1);
    abc[abc.length - 1] = name;

    //
    this.packageNames = new IterableArray<String>(abc, 0, abc.length - 1);
    this.names = new IterableArray<String>(abc, 0, abc.length);
    this.packageFQN = packageFQN;
    this.rawName = rawName;
    this.fqn = fqn;
    this.ext = ext;
    this.name = name;
  }

  public FileKey as(JavaFileObject.Kind kind) {
    return new FileKey(packageFQN, rawName, kind.extension.substring(1));
  }

  public JavaFileObject.Kind getKind() {
    if (ext.equals("java")) {
      return JavaFileObject.Kind.SOURCE;
    } else if (ext.equals("class")) {
      return JavaFileObject.Kind.CLASS;
    } else {
      return JavaFileObject.Kind.OTHER;
    }
  }

  @Override
  public final int hashCode() {
    return packageFQN.hashCode() ^ rawName.hashCode() ^ ext.hashCode();
  }

  @Override
  public final boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    else if (obj instanceof FileKey) {
      FileKey that = (FileKey)obj;
      return packageFQN.equals(that.packageFQN) && rawName.equals(that.rawName) && ext.equals(that.ext);
    }
    else {
      return false;
    }
  }

  @Override
  public String toString() {
    return "FileKey[packageName=" + packageFQN + ",rawName=" + rawName + ",ext=" + ext + "]";
  }
}
