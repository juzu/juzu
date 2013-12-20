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

import juzu.impl.common.JSON;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Julien Viet
 */
public class Asset implements Serializable {

  /** . */
  public final String id;

  /** The asset type. */
  public final String type;

  /** . */
  public final String value;

  /** . */
  public final List<String> depends;

  /** . */
  public final String location;

  public Asset(String type, Map<String, Serializable> asset) {
    String id = (String)asset.get("id");
    String value = (String)asset.get("value");
    List<String> depends = (List<String>)asset.get("depends");
    String location = (String)asset.get("location");

    //
    if (value == null) {
      throw new NullPointerException();
    }
    if (location == null) {
      location = "APPLICATION";
    }

    //
    this.id = id;
    this.type = type;
    this.value = value;
    this.depends = depends != null ? depends : Collections.<String>emptyList();
    this.location = location;
  }

  public Asset(String id, String type, String value, List<String> depends, String location) {
    if (value == null) {
      throw new NullPointerException();
    }
    if (location == null) {
      location = "APPLICATION";
    }
    this.id = id;
    this.type = type;
    this.value = value;
    this.depends = depends;
    this.location = location;
  }

  public JSON getJSON() {
    JSON json = new JSON().set("value", value).set("type", type);
    if (id != null) {
      json.set("id", id);
    }
    if (depends != null) {
      json.set("depends", depends);
    }
    if (location != null) {
      json.set("location", location);
    }
    return json;
  }

  /**
   * Provide an opportunity to process the asset stream.
   * @param stream the stream to filter
   * @return the filtered stream
   */
  public InputStream filter(InputStream stream) throws IOException {
    return stream;
  }
}
