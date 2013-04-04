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

  void merge(String base, String path) throws IOException {
    if (path.length() == 0 || path.endsWith("/")) {
      // Ignore
    } else {
      merge(base, path, 0);
    }
  }

  void merge(String base, String path, int from) throws IOException {
    int pos = path.indexOf('/', from);
    if (pos == -1) {
      String name = path.substring(from);
      Node childNode = add(name);
      childNode.url = new URL(base + path);
    } else {
      String name = path.substring(from, pos);
      Node childNode = add(name);
      childNode.merge(base, path, pos + 1);
    }
  }
}
