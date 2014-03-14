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
import juzu.impl.plugin.application.Application;
import juzu.impl.resource.ResourceResolver;

import javax.inject.Inject;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class AssetManager {

  /** . */
  private HashMap<String, AssetNode> assets = new HashMap<String, AssetNode>();

  /** Graph saying which assets depends on which asset. */
  private AssetGraph graph = new AssetGraph();

  /** . */
  protected final String prefix;

  /** . */
  protected final ResourceResolver applicationResolver;

  @Inject
  public AssetManager(Application application) {
    this.prefix = "/" + application.getDescriptor().getPackageName().replace('.', '/') + "/assets/";
    this.applicationResolver = application;
  }

  AssetManager(String prefix, ResourceResolver applicationResolver) {
    this.prefix = prefix;
    this.applicationResolver = applicationResolver;
  }

  public AssetDeployment createDeployment() {
    return new AssetDeployment(this);
  }

  boolean deploy(AssetDeployment deployment) {

    // Clone the state
    AssetGraph graphClone = new AssetGraph(graph);
    HashMap<String, AssetNode> assetsClone = new HashMap<String, AssetNode>(assets);
    for (AssetNode asset : deployment.assets) {
      // Check it was not previously deployed
      if (!assetsClone.keySet().contains(asset.id)) {
        for (AssetNode deployed : assetsClone.values()) {
          if (deployed.iDependOn.contains(asset.id)) {
            if (!graphClone.register(asset.id, deployed.id)) {
              return false;
            }
          }
          if (asset.iDependOn.contains(deployed.id)) {
            if (!graphClone.register(deployed.id, asset.id)) {
              return false;
            }
          }
        }
        assetsClone.put(asset.id, asset);
      } else {
        // log it ?
        return false;
      }
    }

    // Everything went fine we updated the manager
    assets = assetsClone;
    graph = graphClone;
    return true;
  }

  void undeploy(AssetDeployment deployment) {
    for (AssetNode asset : deployment.assets) {
      for (AssetNode deployed : assets.values()) {
        if (deployed.iDependOn.contains(asset.id)) {
          graph.unregister(asset.id, deployed.id);
        }
        if (asset.iDependOn.contains(deployed.id)) {
          graph.unregister(deployed.id, asset.id);
        }
      }
      assets.remove(asset.id);
    }
  }

  /**
   * Resolve an application asset as a resource URL or return null if it cannot be found.
   *
   * @param path the path the path within the application
   * @return the resource
   */
  public AssetResource resolveApplicationAssetResource(String path) {
    for (AssetNode asset : assets.values()) {
      if (asset.value.equals(path) && asset.resource != null) {
        Integer maxAge = asset.asset.getMaxAge();
        return new AssetResource(asset.resource, maxAge);
      }
    }
    if (path.startsWith(prefix)) {
      URL resolved = applicationResolver.resolve(path);
      return new AssetResource(resolved, null);
    } else {
      return null;
    }
  }

  /**
   * Find all assets of the specified type and returns a map of id -> Asset.
   *
   * @param type the asset type
   * @return the asset map
   */
  public Map<String, Asset> getAssets(String type) {
    Map<String, Asset> ret = Collections.emptyMap();
    for (AssetNode node : assets.values()) {
      if (node.asset.getType().equals(type)) {
        if (ret.isEmpty()) {
          ret = new HashMap<String, Asset>();
        }
        ret.put(node.id, node.asset);
      }
    }
    return ret;
  }

  /**
   * Returns the assets forthe specifid id.
   *
   * @param id the asset id
   * @return the corresponding assets
   * @throws NullPointerException
   */
  public Asset getAsset(String id) throws NullPointerException {
    if (id == null) {
      throw new NullPointerException("No null id accepted");
    }
    AssetNode node = assets.get(id);
    return node != null ? node.asset : null;
  }

  /**
   * Perform a topological sort of the provided asset script values.
   *
   * @param ids the asset ids to resolve
   * @return the resolved asset or null
   * @throws NullPointerException if the asset ids argument is null
   * @throws IllegalArgumentException when asset dependencies cannot be resolved
   */
  public Iterable<Asset> resolveAssets(Iterable<String> ids) throws
    NullPointerException,
    IllegalArgumentException {

    //
    if (ids == null) {
      throw new NullPointerException("No null asset ids accepted");
    }

    // Compute the closure of the assets we need
    LinkedHashMap<String, HashSet<String>> sub = new LinkedHashMap<String, HashSet<String>>();
    for (LinkedList<String> queue = Tools.addAll(new LinkedList<String>(), ids);!queue.isEmpty();) {
      String id = queue.removeFirst();
      AssetNode asset = this.assets.get(id);
      if (asset != null) {
        sub.put(asset.id, new HashSet<String>(asset.iDependOn));
        for (String depend : asset.iDependOn) {
          if (!sub.containsKey(depend)) {
            queue.addLast(depend);
          }
        }
      }
      else {
        throw new IllegalArgumentException("Cannot resolve asset " + id);
      }
    }

    // Perform the topological sort
    LinkedList<Asset> resolved = new LinkedList<Asset>();
    while (sub.size() > 0) {
      boolean found = false;
      for (Iterator<Map.Entry<String, HashSet<String>>> i = sub.entrySet().iterator();i.hasNext();) {
        Map.Entry<String, HashSet<String>> entry = i.next();
        if (entry.getValue().isEmpty()) {
          i.remove();
          AssetNode asset = this.assets.get(entry.getKey());
          resolved.add(asset.asset);
          Set<String> dependencies = graph.get(asset.id);
          if (dependencies != null) {
            for (String dependency : dependencies) {
              HashSet<String> foo = sub.get(dependency);
              if (foo != null) {
                foo.remove(entry.getKey());
              }
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
