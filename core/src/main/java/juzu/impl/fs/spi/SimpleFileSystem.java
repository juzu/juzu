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

package juzu.impl.fs.spi;

import juzu.impl.common.Content;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * A degenerated file system which allows to: <ul> <li>Retrieve a path according to its names</li> <li>Determine whether
 * a path is a directory or a fiile</li> <li>List the set of children of a directory</li> <li>Determine the names of a
 * path</li> <li>Retrieve the content of a file</li> </ul>
 * <p/>
 * The file system is said to be degenerated because a valid path may not have valid ancestors.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public abstract class SimpleFileSystem<P> {

  public final <A extends Appendable> A packageOf(P path, char separator, A appendable) throws NullPointerException, IOException {
    if (path == null) {
      throw new NullPointerException("No null path accepted");
    }
    if (appendable == null) {
      throw new NullPointerException("No null appendable accepted");
    }
    List<String> l = new ArrayList<String>();
    packageOf(path, l);
    bilto(l, separator, appendable);
    return appendable;
  }

  public final void pathOf(P path, Collection<String> to) throws IOException {
    packageOf(path, to);
    if (isFile(path)) {
      String name = getName(path);
      to.add(name);
    }
  }

  public final void pathOf(P path, char separator, Appendable appendable) throws NullPointerException, IOException {
    if (path == null) {
      throw new NullPointerException("No null path accepted");
    }
    if (appendable == null) {
      throw new NullPointerException("No null appendable accepted");
    }
    List<String> l = new ArrayList<String>();
    pathOf(path, l);
    bilto(l, separator, appendable);
  }

  private void bilto(Collection<String> l, char separator, Appendable appendable) throws IOException {
    boolean dot = false;
    for (String name : l) {
      if (dot) {
        appendable.append(separator);
      }
      else {
        dot = true;
      }
      appendable.append(name);
    }
  }

  public final P getPath(String... names) throws IOException {
    return getPath(Arrays.asList(names));
  }

  /**
   * Returns an description for the file system (for debugging purposes).
   *
   * @return the id
   */
  public abstract String getDescription();

  public abstract String getName(P path) throws IOException;

  public abstract P getPath(Iterable<String> names) throws IOException;

  public abstract void packageOf(P path, Collection<String> to) throws IOException;

  public abstract Iterator<P> getChildren(P dir) throws IOException;

  public abstract boolean isDir(P path) throws IOException;

  public abstract boolean isFile(P path) throws IOException;

  public abstract Content getContent(P file) throws IOException;

  /**
   * Attempt to return a {@link java.io.File} associated with this file or null if no physical file exists.
   *
   * @param path the path
   * @return the file system object
   * @throws IOException any IO exception
   */
  public abstract File getFile(P path) throws IOException;

  /**
   * Get an URL for the provided path or return null if no such URL can be found.
   *
   * @param path the path
   * @return the URL for this path
   * @throws NullPointerException if the path is null
   * @throws IOException          any io exception
   */
  public abstract URL getURL(P path) throws NullPointerException, IOException;
}
