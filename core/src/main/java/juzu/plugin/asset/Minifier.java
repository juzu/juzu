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
package juzu.plugin.asset;

import java.io.IOException;
import java.io.InputStream;

/**
 * <p>The interface implemented by an asset minifier, an asset minifier should implement this interface and provide
 * a public no-arg constructor. Implementations can be used by application in asset declarations.</p>
 *
 * @author Julien Viet
 */
public interface Minifier {

  /**
   * Minify an asset.
   *
   * @param name the asset value, for instance <code>jquery.js</code>
   * @param type the asset type: <code>script</code> or <code>stylesheet</code>
   * @param stream the asset stream
   * @return a stream that will produce the asset
   * @throws IOException any io exception that would prevent minification to happen
   */
  InputStream minify(String name, String type, InputStream stream) throws IOException;

}
