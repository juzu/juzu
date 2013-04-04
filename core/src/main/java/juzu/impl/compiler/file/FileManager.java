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

package juzu.impl.compiler.file;

import juzu.impl.common.FileKey;

import javax.tools.JavaFileObject;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class FileManager {

  public abstract void populateRoots(Set<File> roots) throws IOException;

  public abstract JavaFileObject getReadable(FileKey key) throws IOException;

  public abstract JavaFileObject getWritable(FileKey key) throws IOException;

  public abstract <C extends Collection<JavaFileObject>> C list(
    String packageName,
    Set<JavaFileObject.Kind> kinds,
    boolean recurse,
    C to) throws IOException;

  public abstract void clearCache();
}
