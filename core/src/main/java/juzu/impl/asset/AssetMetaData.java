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
import java.util.List;
import java.util.Set;

/**
 * Describes an asset.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class AssetMetaData {

  /** The asset id. */
  final String id;

  /** The asset location. */
  final AssetLocation location;

  /** The asset value. */
  final List<String> values;

  /** The asset dependencies. */
  final Set<String> dependencies;

  public AssetMetaData(String id, AssetLocation location, List<String> values, String... dependencies) {
    this.id = id;
    this.values = values;
    this.location = location;
    this.dependencies = Collections.unmodifiableSet(Tools.set(dependencies));
  }

  public String getId() {
    return id;
  }

  public AssetLocation getLocation() {
    return location;
  }

  public List<String> getValues() {
    return values;
  }

  public Set<String> getDependencies() {
    return dependencies;
  }

  @Override
  public String toString() {
    return "AssetDescriptor[id=" + id + ",location=" + location + ",values=" + values + ",dependencies=" + dependencies + "]";
  }
}
