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

import java.io.IOException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public interface Visitor<P> {

  void enterDir(P dir, String name) throws IOException;

  void file(P file, String name) throws IOException;

  void leaveDir(P dir, String name) throws IOException;

  /** A default implementation for the visitor. */
  public static class Default<P> implements Visitor<P> {
    public void enterDir(P dir, String name) throws IOException {
    }

    public void file(P file, String name) throws IOException {
    }

    public void leaveDir(P dir, String name) throws IOException {
    }
  }
}
