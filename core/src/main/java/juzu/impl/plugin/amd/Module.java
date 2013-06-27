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

package juzu.impl.plugin.amd;

import juzu.PropertyType;
import juzu.asset.AssetLocation;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class Module {

  /** AMD. */
  public static PropertyType<Module> TYPE = new PropertyType<Module>(){};

  /** . */
  final String name;

  /** . */
  final AssetLocation location;

  /** . */
  final String uri;

  public Module(String name, AssetLocation location, String uri) {
    this.name = name;
    this.location = location;
    this.uri = uri;
  }

  public String getName() {
    return name;
  }

  public AssetLocation getLocation() {
    return location;
  }

  public String getUri() {
    return uri;
  }
}
