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

import java.io.Serializable;
import java.util.Arrays;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class Path implements Serializable {

  /** . */
  private static final String[] EMPTY_STRING_ARRAY = new String[0];

  public static Path absolute(Name qn, String name, String extension) {
    return create(true, qn, name, extension);
  }

  public static Path create(boolean absolute, Name qn, String rawName, String ext) {
    return new Path(absolute, qn, rawName, ext);
  }

  public static Path parse(String path) throws NullPointerException, IllegalArgumentException {
    boolean absolute = path.length() > 0 && path.charAt(0) == '/';
    String[] atoms = Lexers.parsePath(Lexers.PARSE_CANONICAL, EMPTY_STRING_ARRAY, 0, path, 0);
    return new Path(absolute, atoms);
  }

  /** . */
  protected final String[] atoms;

  /** . */
  private final boolean absolute;

  /** . */
  private String canonical;

  private Path(boolean absolute, Name pkg, String rawName, String ext) {

    String[] atoms = new String[pkg.size() + 2];
    for (int i = 0;i < pkg.size();i++) {
      atoms[i] = pkg.get(i);
    }
    atoms[atoms.length - 2] = rawName;
    atoms[atoms.length - 1] = ext;

    //
    this.absolute = absolute;
    this.canonical = null;
    this.atoms = atoms;
  }

  private Path(boolean absolute, String[] atoms) {
    this.absolute = absolute;
    this.canonical = null;
    this.atoms = atoms;
  }

  public Path append(String path) throws NullPointerException, IllegalArgumentException {
    if (path == null) {
      throw new NullPointerException("No null path accepted");
    }
    if (path.length() > 0 && path.charAt(0) == '/') {
      throw new IllegalArgumentException("Cannot append absolute path " + path);
    }
    String[] atoms = Lexers.parsePath(Lexers.PARSE_ANY, this.atoms, this.atoms.length - 2, path, 0);
    return new Path(absolute, atoms);
  }

  public String getValue() {
    return getCanonical();
  }

  public boolean isAbsolute() {
    return absolute;
  }

  public final boolean isRelative() {
    return !isAbsolute();
  }

  public Iterable<String> getDirs() {
    return Tools.iterable(0, atoms.length - 2, atoms);
  }

  public String getRawName() {
    return atoms[atoms.length - 2];
  }

  public String getExt() {
    return atoms[atoms.length - 1];
  }

  public String getName() {
    String ext = getExt();
    String rawName = getRawName();
    if (ext != null) {
      return rawName + "." + ext;
    }
    else {
      return rawName;
    }
  }

  public Path as(String ext) {
    String[] tmp = atoms.clone();
    tmp[tmp.length - 1] = ext;
    return new Path(absolute, tmp);
  }

  public String getCanonical() {
    if (canonical == null) {
      StringBuilder sb = new StringBuilder();
      if (isAbsolute()) {
        sb.append('/');
      }
      for (int i = 0;i < atoms.length - 1;i++) {
        if (i > 0) {
          sb.append('/');
        }
        sb.append(atoms[i]);
      }
      String ext = getExt();
      if (ext != null) {
        sb.append('.').append(ext);
      }
      canonical = sb.toString();
    }
    return canonical;
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(atoms);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj.getClass() == getClass()) {
      Path that = (Path)obj;
      return absolute == that.absolute && Arrays.equals(atoms, that.atoms);
    }
    return false;
  }

  @Override
  public String toString() {
    return "Path[" + getCanonical() +  "]";
  }
}
