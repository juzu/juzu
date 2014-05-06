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
import juzu.impl.common.Tools;

import java.util.Collections;
import java.util.Set;

/**
 * Describes an asset.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class AssetMetaData {

  /** The asset id. */
  final String id;

  /** The asset type. */
  final String type;

  /** The asset location. */
  final AssetLocation location;

  /** The asset value. */
  final String value;

  /** The header value. */
  final Boolean header;

  /** The asset minified value. */
  final String minified;

  /** The asset dependencies. */
  final Set<String> dependencies;

  /** . */
  final Integer maxAge;

  public AssetMetaData(String id, String type, AssetLocation location, String value, Boolean header, String minified, Integer maxAge, String... dependencies) {
    this.id = id;
    this.type = type;
    this.value = value;
    this.header = header;
    this.minified = minified;
    this.location = location;
    this.maxAge = maxAge;
    this.dependencies = Collections.unmodifiableSet(Tools.set(dependencies));
  }

  public String getId() {
    return id;
  }

  public String getType() {
    return type;
  }

  public AssetLocation getLocation() {
    return location;
  }

  public Integer getMaxAge() {
    return maxAge;
  }

  public Boolean getHeader() {
    return header;
  }

  public String getValue() {
    return value;
  }

  public String getMinified() {
    return minified;
  }

  public Set<String> getDependencies() {
    return dependencies;
  }

  @Override
  public String toString() {
    return "AssetDescriptor[id=" + id + ",location=" + location + ",values=" + value + ",dependencies=" + dependencies + "]";
  }
}
