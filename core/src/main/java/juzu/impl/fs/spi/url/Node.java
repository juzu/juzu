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

package juzu.impl.fs.spi.url;

import juzu.impl.common.AbstractTrie;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.jar.JarEntry;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class Node extends AbstractTrie<String, Node> {

  /** . */
  URL url;

  public Node() {
  }

  private Node(Node parent, String key) {
    super(parent, key);
  }

  @Override
  protected Node create(Node parent, String key) {
    return new Node(parent, key);
  }

  /**
   * Merge this file to the current node recursively.
   *
   * @param file the file to merge
   * @throws IOException any io exception
   */
  void merge(File file) throws IOException {
    File[] children = file.listFiles();
    if (children != null) {
      for (File child : children) {
        Node childNode = add(child.getName());
        if (child.isDirectory()) {
          childNode.merge(child);
        }
        else {
          childNode.url =  child.toURI().toURL();
        }
      }
    }
  }

  void merge(URL base, JarEntry entry) throws IOException {
    if (entry.isDirectory()) {
      // Ignore
    } else {
      merge(base, entry.getName(), 0);
    }
  }

  void merge(URL base, String path, int from) throws IOException {
    int pos = path.indexOf('/', from);
    if (pos == -1) {
      String name = path.substring(from);
      Node childNode = add(name);
      childNode.url = new URL("jar:" + base + "!/" + path);
    } else {
      String name = path.substring(from, pos);
      Node childNode = add(name);
      childNode.merge(base, path, pos + 1);
    }
  }
}
