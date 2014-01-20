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

import juzu.asset.AssetLocation;

import java.io.Serializable;

/**
 * @author Julien Viet
 */
public final class AssetKey implements Serializable {

  /** . */
  public final String value;

  /** . */
  public final AssetLocation location;

  AssetKey(String value, AssetLocation location) {
    if (value == null) {
      throw new NullPointerException();
    }
    if (location == null) {
      throw new NullPointerException();
    }
    this.value = value;
    this.location = location;
  }

  @Override
  public int hashCode() {
    return value.hashCode() ^ location.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    } else if (obj instanceof AssetKey) {
      AssetKey that = (AssetKey)obj;
      return value.equals(that.value) && location.equals(that.location);
    } else {
      return false;
    }
  }

  @Override
  public String toString() {
    return "AssetKey[value=" + value + ",location=" + location.name() + "]";
  }
}
