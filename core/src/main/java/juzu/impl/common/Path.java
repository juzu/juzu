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
import java.util.Iterator;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class Path implements Serializable, Iterable<String> {

  /** . */
  private static final int PARSE_CANONICAL = 0;

  /** . */
  private static final int PARSE_ANY = 1;

  public static Path create(boolean absolute, QN qn, String name, String extension) {
    return absolute ? new Absolute(null, new FQN(qn, name), extension) : new Relative(null, new FQN(qn, name), extension);
  }

  public static Path parse(String path) throws NullPointerException, IllegalArgumentException {
    boolean absolute = path.length() > 0 && path.charAt(0) == '/';
    String[] atoms = parse(PARSE_CANONICAL, 0, path, 0, 0);
    return absolute ? new Absolute(path, atoms) : new Relative(path, atoms);
  }

  /**
   * Path parsing method and returns an array. The last two values of the array are the
   *
   * @param mode {@link #PARSE_CANONICAL} : rejects any '.' or '..' / {@link #PARSE_ANY} : accepts '.' or '..'
   * @param padding the first index that will be written
   * @param path the path to parse
   * @param off the first char to parse
   * @param size the current array size
   * @return the parsed path as a String[]
   */
  private static String[] parse(int mode, int padding, String path, int off, int size) {
    int len = path.length();
    int at = padding + size;
    if (off < len) {
      int pos = path.indexOf('/', off);
      if (pos == -1) {

        // Find the last index of '.'
        int cur = path.indexOf('.', off);

        //
        if (cur == -1) {
          String[] ret = new String[padding + size + 2];
          ret[at] = path.substring(off);
          return ret;
        } else {

          // Validate the dot position
          if (cur - off < 1 || len - cur < 2) {
            throw new IllegalArgumentException("The path " + path + " contains an illegal '.' char at the index " + cur);
          }

          // Validate the extension does not contain any '.'
          int dotPos = path.indexOf('.', cur + 1);
          if (dotPos != -1) {
            throw new IllegalArgumentException("The path " + path + " contains an illegal '.' char at the index " + dotPos);
          } else {
            String[] ret = new String[padding + size + 2];
            ret[at] = path.substring(off, cur);
            ret[padding + size + 1] = path.substring(cur + 1);
            return ret;
          }
        }
      }
      else {
        int diff = pos - off;
        if (diff == 0) {
          return parse(mode, padding, path, off + 1, size);
        } else {
          if (diff == 1 && path.charAt(off) == '.') {
            switch (mode) {
              case PARSE_CANONICAL:
                throw new IllegalArgumentException("No '.' allowed here");
              case PARSE_ANY:
                // Skip '.'
                return parse(mode, padding, path, off + 2, size);
              default:
                throw new AssertionError("Should not be here");
            }
          } else if (diff == 2 && path.charAt(off) == '.' && path.charAt(off + 1) == '.') {
            switch (mode) {
              case PARSE_CANONICAL:
                throw new IllegalArgumentException("No '.' allowed here");
              case PARSE_ANY:
                // Skip '..' ?
                if (size > 0) {
                  return parse(mode, padding, path, off + 3, size - 1);
                } else if (padding > 0) {
                  return parse(mode, padding - 1, path, off + 3, size);
                } else {
                  throw new IllegalArgumentException("Invalid path");
                }
              default:
                throw new AssertionError("Should not be here");
            }
          }
          for (int i = off;i < pos;i++) {
            if (path.charAt(i) == '.') {
              throw new IllegalArgumentException("No '.' allowed here");
            }
          }
          String[] ret = parse(mode, padding, path, pos + 1, size + 1);
          if (ret[at] == null) {
            ret[at] = path.substring(off, pos);
          }
          return ret;
        }
      }
    }
    else {
      String[] ret = new String[padding + size + 2];
      ret[at] = "";
      return ret;
    }
  }

  /** . */
  protected final FQN fqn;

  /** . */
  private String canonical;

  /** . */
  private String value;

  /** . */
  private final String ext;

  /** . */
  private String name;

  private Path(String value, FQN fqn, String ext) {
    this.fqn = fqn;
    this.canonical = null;
    this.value = value;
    this.ext = ext;
    this.name = null;
  }

  private Path(String path, String[] atoms) {

    int len = atoms.length - 2;
    QN qn;
    if (len == 0) {
      qn = QN.EMPTY;
    }
    else if (len == 1) {
      qn = new QN(atoms[0], atoms, 1);
    }
    else {
      StringBuilder sb = new StringBuilder();
      for (int i = 0;i < len;i++) {
        if (i > 0) {
          sb.append('.');
        }
        sb.append(atoms[i]);
      }
      qn = new QN(sb.toString(), atoms, len);
    }

    //
    this.fqn = new FQN(qn, atoms[len]);
    this.canonical = null;
    this.value = path;
    this.ext = atoms[atoms.length - 1];
    this.name = null;
  }

  public Path append(String path) throws NullPointerException, IllegalArgumentException {
    if (path == null) {
      throw new NullPointerException("No null path accepted");
    }
    if (path.length() > 0 && path.charAt(0) == '/') {
      throw new IllegalArgumentException("Cannot append absolute path " + path);
    }
    QN pkg = fqn.getPackageName();
    int len = pkg.size();
    String[] atoms = parse(PARSE_ANY, len, path, 0, 0);
    pkg.mergeTo(atoms);
    StringBuilder sb = new StringBuilder();
    if (isAbsolute()) {
      sb.append('/');
    }
    for (int i = 0;i < atoms.length - 1;i++) {
      sb.append(atoms[i]);
    }
    String ext = atoms[atoms.length - 1];
    if (ext != null && ext.length() > 0) {
      sb.append('.').append(ext);
    }
    return isAbsolute() ? new Absolute(sb.toString(), atoms) : new Relative(sb.toString(), atoms);
  }

  public Iterator<String> iterator() {
    return fqn.iterator();
  }

  public String getValue() {
    if (value == null) {
      return getCanonical();
    }
    else {
      return value;
    }
  }

  public abstract boolean isAbsolute();

  public QN getQN() {
    return fqn.getPackageName();
  }

  public FQN getFQN() {
    return fqn;
  }

  public String getRawName() {
    return fqn.getSimpleName();
  }

  public String getExt() {
    return ext;
  }

  public String getName() {
    if (name == null) {
      if (ext != null) {
        name = fqn.getSimpleName() + "." + ext;
      }
      else {
        name = fqn.getSimpleName();
      }
    }
    return name;
  }

  public abstract Path as(String ext);

  public String getCanonical() {
    if (canonical == null) {
      StringBuilder sb = new StringBuilder();
      if (isAbsolute()) {
        sb.append('/');
      }
      for (int i = 0;i < fqn.size();i++) {
        if (i > 0) {
          sb.append('/');
        }
        sb.append(fqn.get(i));
      }
      if (ext != null) {
        sb.append('.').append(ext);
      }
      canonical = sb.toString();
    }
    return canonical;
  }

  @Override
  public int hashCode() {
    return fqn.hashCode() ^ (ext != null ? ext.hashCode() : 0);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj.getClass() == getClass()) {
      Path that = (Path)obj;
      return fqn.equals(that.fqn) && Tools.safeEquals(ext, that.ext);
    }
    return false;
  }

  @Override
  public String toString() {
    return "Path[absolute=" + isAbsolute() + ",fqn=" + fqn + ",extension=" + ext + "]";
  }

  public static class Absolute extends Path {

    public static Absolute create(QN qn, String rawName, String ext) {
      return new Absolute(null, new FQN(qn, rawName), ext);
    }

    private Absolute(String value, FQN fqn, String extension) {
      super(value, fqn, extension);
    }

    private Absolute(String path, String[] atoms) {
      super(path, atoms);
    }

    @Override
    public Absolute as(String ext) {
      return new Absolute(null, fqn, ext);
    }

    @Override
    public Absolute append(String path) throws NullPointerException, IllegalArgumentException {
      return (Absolute)super.append(path);
    }

    @Override
    public boolean isAbsolute() {
      return true;
    }
  }

  public static class Relative extends Path {

    public static Relative create(QN qn, String name, String extension) {
      return new Relative(null, new FQN(qn, name), extension);
    }

    private Relative(String value, FQN fqn, String extension) {
      super(value, fqn, extension);
    }

    private Relative(String path, String[] atoms) {
      super(path, atoms);
    }

    @Override
    public Relative as(String ext) {
      return new Relative(null, fqn, ext);
    }

    @Override
    public Relative append(String path) throws NullPointerException, IllegalArgumentException {
      return (Relative)super.append(path);
    }

    @Override
    public boolean isAbsolute() {
      return false;
    }
  }
}
