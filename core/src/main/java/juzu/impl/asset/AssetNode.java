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

package juzu.impl.asset;

import juzu.asset.AssetLocation;

import java.net.URL;
import java.util.Set;

/**
 * An immutable asset descriptor.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class AssetNode {

  /** . */
  final String id;

  /** . */
  final AssetLocation location;

  /** . */
  final String value;

  /** . */
  final Asset asset;

  /** . */
  final URL resource;

  /** . */
  Set<String> iDependOn;

  public AssetNode(String id, String type, AssetLocation location, String value, Integer maxAge, URL resource, Set<String> iDependOn) {

    //
    this.id = id;
    this.location = location;
    this.value = value;
    this.asset = new Asset(id, type, location, value, maxAge);
    this.resource = resource;
    this.iDependOn = iDependOn;
  }

  public String getId() {
    return id;
  }

  public AssetLocation getLocation() {
    return location;
  }

  public String getValue() {
    return value;
  }

  public Asset getAsset() {
    return asset;
  }
}
