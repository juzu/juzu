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
    int pos = name.indexOf('.');
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

  public static FileKey newName(Path.Absolute path) {
    return new FileKey(path.getDirs().toString(), path.getRawName(), path.getExt());
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
