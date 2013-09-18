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

import juzu.impl.common.Tools;

import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class AssetManager {

  /** . */
  protected final LinkedHashMap<String, AssetNode> assets = new LinkedHashMap<String, AssetNode>();

  /** . */
  protected final HashMap<String, URL> resources = new HashMap<String, URL>();

  /**
   * <p>Attempt to add an asset to the manager, the manager will return the asset id
   * if the asset was registered or null if it was not.</p>
   *
   * <p>When no asset id is specified, an asset id will be generated from the asset value by taking
   * the longest trailing substring that contains no <code>/</code> char.</p>
   *
   * @param data the asset description
   * @param url the asset url
   * @return the asset id
   * @throws NullPointerException     if the metaData argument is nul
   * @throws IllegalArgumentException if the metaData does not have an id set
   */
  public String addAsset(AssetMetaData data, URL url) throws NullPointerException, IllegalArgumentException {
    String id = data.id;

    // Use asset value if no specific id specifed
    if (id == null) {
      String value = data.getValue();
      int slash = value.lastIndexOf('/');
      id = slash >= 0 ? value.substring(slash + 1) : value;
    }

    //
    if (!assets.keySet().contains(id)) {
      AssetNode asset = new AssetNode(id, data.location, data.value, data.dependencies);
      for (AssetNode deployed : assets.values()) {
        if (deployed.iDependOn.contains(id)) {
          asset.dependsOnMe = Tools.addToHashSet(asset.dependsOnMe, deployed.id);
        }
        if (asset.iDependOn.contains(deployed.id)) {
          deployed.dependsOnMe = Tools.addToHashSet(deployed.dependsOnMe, id);
        }
      }
      assets.put(id, asset);

      //
      switch (data.location) {
        case APPLICATION:
        case SERVER:
          resources.put(data.getValue(), url);
          break;
        default:
          // Nothing to do
          break;
      }
    } else {
      // log it ?
    }

    //
    return id;
  }

  /**
   * Resolve an asset as a resource URL or return null if it cannot be found.
   *
   * @param path the path
   * @return the resource
   */
  public URL resolveAsset(String path) {
    return resources.get(path);
  }

  /**
   * Perform a topological sort of the provided asset script values.
   *
   * @param scripts the asset id to resolve
   * @return the resolved asset or null
   * @throws NullPointerException     if the asset id argument is null
   * @throws IllegalArgumentException when script dependencies cannot be resolved
   */
  public Iterable<Asset> resolveAssets(Iterable<String> scripts) throws
    NullPointerException,
    IllegalArgumentException {
    LinkedHashMap<String, HashSet<String>> sub = new LinkedHashMap<String, HashSet<String>>();
    for (String script : scripts) {
      AssetNode asset = assets.get(script);
      if (asset != null) {
        sub.put(asset.id, new HashSet<String>(asset.iDependOn));
      }
      else {
        throw new IllegalArgumentException("Cannot resolve asset " + script);
      }
    }

    //
    LinkedList<Asset> resolved = new LinkedList<Asset>();
    while (sub.size() > 0) {
      boolean found = false;
      for (Iterator<Map.Entry<String, HashSet<String>>> i = sub.entrySet().iterator();i.hasNext();) {
        Map.Entry<String, HashSet<String>> entry = i.next();
        if (entry.getValue().isEmpty()) {
          i.remove();
          AssetNode asset = assets.get(entry.getKey());
          resolved.addLast(Asset.of(asset.getLocation(), asset.getValue()));
          for (String dependency : asset.dependsOnMe) {
            HashSet<String> foo = sub.get(dependency);
            if (foo != null) {
              foo.remove(entry.getKey());
            }
          }
          found = true;
          break;
        }
      }
      if (!found) {
        StringBuilder sb = new StringBuilder("Cannot satisfy asset dependencies:\n");
        for (Map.Entry<String, HashSet<String>> entry : sub.entrySet()) {
          sb.append(entry.getKey()).append(" -> ").append(entry.getValue());
        }
        throw new IllegalArgumentException(sb.toString());
      }
    }

    //
    return resolved;
  }
}
