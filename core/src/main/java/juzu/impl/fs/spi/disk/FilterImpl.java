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

package juzu.impl.fs.spi.disk;

import juzu.impl.common.Name;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class FilterImpl implements FilenameFilter {

  /** . */
  private final Map<File, String> valids;

  FilterImpl(File root, Name path) {
    Map<File, String> valids = new HashMap<File, String>();

    {
      File current = root;
      for (String name : path) {
        valids.put(current, name);
        current = new File(current, name);
      }
    }


    this.valids = valids;
  }

  public boolean accept(File dir, String name) {
    String found = valids.get(dir);
    return found == null || found.equals(name);
  }
}
