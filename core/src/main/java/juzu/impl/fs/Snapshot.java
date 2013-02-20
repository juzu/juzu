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
