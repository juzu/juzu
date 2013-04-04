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
package juzu.impl.fs;

import juzu.impl.common.Tools;
import juzu.impl.fs.spi.ReadFileSystem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class Snapshot<P> extends HashMap<String, Long> {

  /** . */
  private FileSystemScanner<P> scanner;

  /** Changes since previous snapshot. */
  Map<String, Change> changes;

  public Snapshot(FileSystemScanner<P> scanner) {
    this.scanner = scanner;
    this.changes = new LinkedHashMap<String, Change>();
  }

  public boolean hasChanges() {
    return changes.size() > 0;
  }

  public Map<String, Change> getChanges() {
    return changes;
  }

  public Snapshot<P> scan() throws IOException {
    return scan(this);
  }

  private static <P> Snapshot<P> scan(final Snapshot<P> current) throws IOException {

    //
    final FileSystemScanner<P> scanner = current.scanner;
    final ReadFileSystem<P> fs = scanner.fs;
    final ArrayList<String> stack = scanner.stack;

    // Create a new snapshot
    final Snapshot<P> next = new Snapshot<P>(scanner);

    // Traverse map
    fs.traverse(scanner, new Visitor<P>() {

      public void enterDir(P dir, String name) throws IOException {
        stack.add(name);
      }

      public void file(P file, String name) throws IOException {
        long stamp = scanner.stampOf(file);
        stack.add(name);
        String id = Tools.join('/', stack);
        stack.remove(stack.size() - 1);
        next.put(id, stamp);

        //
        Long data = current.get(id);
        if (data == null) {
          next.changes.put(id, Change.ADD);
        } else {
          if (scanner.isModified(data, stamp)) {
            next.changes.put(id, Change.UPDATE);
          }
        }
      }

      public void leaveDir(P dir, String name) throws IOException {
        stack.remove(scanner.stack.size() - 1);
      }

    });

    // Now find all removed entries
    for (String id : current.keySet()) {
      if (!next.containsKey(id)) {
        next.changes.put(id, Change.REMOVE);
      }
    }

    //
    return next;
  }
}
