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
package juzu.impl.plugin.asset;

import juzu.io.UndeclaredIOException;
import juzu.plugin.asset.Minifier;

import java.io.InputStream;

/**
* @author Julien Viet
*/
public class MinifierCannotInstantiate implements Minifier {

  public MinifierCannotInstantiate() {
    throw new RuntimeException("Cannot instantiate");
  }

  @Override
  public InputStream minify(String name, String type, InputStream stream) throws UndeclaredIOException {
    return stream;
  }
}
