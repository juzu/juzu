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
import juzu.impl.common.JSON;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Julien Viet
 */
public class Asset implements Serializable {

  /** Asset identifiers for dependencies. */
  public final String id;

  /** Asset type. */
  public final String type;

  /** Asset dependencies. */
  public final List<String> depends;

  /** Coordinate of the asset. */
  public final AssetKey key;

  /** The asset max age. */
  public final Integer maxAge;

  public Asset(String type, Map<String, Serializable> asset) {
    String id = (String)asset.get("id");
    String value = (String)asset.get("value");
    List<String> depends = (List<String>)asset.get("depends");
    AssetLocation location = AssetLocation.safeValueOf((String)asset.get("location"));
    Integer maxAge = (Integer)asset.get("maxAge");

    //
    if (type == null) {
      throw new NullPointerException("No null type accepted");
    }
    if (id == null) {
      throw new IllegalArgumentException("No null id accepted");
    }
    if (value == null) {
      throw new IllegalArgumentException("No null value accepted");
    }
    if (location == null) {
      throw new IllegalArgumentException("No null location accepted");
    }

    //
    this.id = id;
    this.type = type;
    this.depends = depends != null ? depends : new ArrayList<String>();
    this.key = new AssetKey(value, location);
    this.maxAge = maxAge;
  }

  public Asset(String id, String type, String value, List<String> depends, AssetLocation location, Integer maxAge) {
    if (type == null) {
      throw new NullPointerException("No null type accepted");
    }
    if (id == null) {
      throw new NullPointerException("No null id accepted");
    }
    if (value == null) {
      throw new NullPointerException("No null value accepted");
    }
    if (location == null) {
      throw new NullPointerException("No null location accepted");
    }

    //
    this.id = id;
    this.type = type;
    this.depends = depends;
    this.key = new AssetKey(value, location);
    this.maxAge = maxAge;
  }

  public boolean isApplication() {
    return key.location == AssetLocation.APPLICATION;
  }

  /**
   * Returns the asset source for application assets, by default it returns the {@link AssetKey#value} field of
   * {@link #key}. It can be subclassed to provide a custom source.
   *
   * @return the asset source for application assets
   */
  public String getSource() {
    if (isApplication()) {
      return key.value;
    } else {
      return null;
    }
  }

  public JSON getJSON() {
    JSON json = new JSON().
        set("id", id).
        set("value", key.value).
        set("location", key.location.toString()).
        set("type", type);
    if (maxAge != null) {
      json.set("max-age", maxAge);
    }
    if (depends != null) {
      json.set("depends", depends);
    }
    return json;
  }

  /**
   * Provide an opportunity to process the asset resource.
   *
   * @param resource the resource to open
   * @return the effective resource stream
   */
  public InputStream open(URLConnection resource) throws IOException {
    return resource.getInputStream();
  }
}
