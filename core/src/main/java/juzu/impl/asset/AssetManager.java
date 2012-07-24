/*
 * Copyright (C) 2012 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package juzu.impl.asset;

import juzu.asset.Asset;
import juzu.impl.common.Tools;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class AssetManager {

  /** . */
  private final LinkedHashMap<String, AssetNode> assets = new LinkedHashMap<String, AssetNode>();

  /** . */
  private final HashSet<String> classPathAssets = new HashSet<String>();

  /**
   * Attempt to add an asset to the manager.
   *
   * @param metaData the metaData to add
   * @throws NullPointerException     if the metaData argument is nul
   * @throws IllegalArgumentException if the metaData does not have an id set
   */
  public void addAsset(AssetMetaData metaData) throws NullPointerException, IllegalArgumentException {
    String id = metaData.id;
    if (id != null) {
      if (!assets.keySet().contains(id)) {
        AssetNode asset = new AssetNode(id, metaData.location, metaData.value, metaData.dependencies);
        for (AssetNode deployed : assets.values()) {
          if (deployed.iDependOn.contains(id)) {
            asset.dependsOnMe = Tools.addToHashSet(asset.dependsOnMe, deployed.id);
          }
          if (asset.iDependOn.contains(deployed.id)) {
            deployed.dependsOnMe = Tools.addToHashSet(deployed.dependsOnMe, id);
          }
        }
        assets.put(id, asset);
      }
      else {
        // log it ?
        return;
      }
    }

    //
    switch (metaData.location) {
      case CLASSPATH:
        classPathAssets.add(metaData.getValue());
        break;
      default:
        // Nothing to do
        break;
    }
  }

  public boolean isClassPath(String path) {
    return classPathAssets.contains(path);
  }

  /**
   * Perform a topological sort of the provided asset script values.
   *
   * @param scripts the asset id to resolve
   * @return the resolved asset or null
   * @throws NullPointerException     if the asset id argument is null
   * @throws IllegalArgumentException when script dependencies cannot be resolved
   */
  public Iterable<Asset.Value> resolveAssets(Iterable<juzu.asset.Asset> scripts) throws
    NullPointerException,
    IllegalArgumentException {
    LinkedHashMap<String, HashSet<String>> sub = new LinkedHashMap<String, HashSet<String>>();
    for (juzu.asset.Asset script : scripts) {
      if (script instanceof Asset.Value) {
        // resolved.addLast(script);
      }
      else {
        AssetNode asset = assets.get(((juzu.asset.Asset.Ref)script).getId());
        if (asset != null) {
          sub.put(asset.id, new HashSet<String>(asset.iDependOn));
        }
        else {
          throw new IllegalArgumentException("Cannot resolve asset " + script);
        }
      }
    }

    //
    LinkedList<Asset.Value> resolved = new LinkedList<Asset.Value>();
    while (sub.size() > 0) {
      boolean found = false;
      for (Iterator<Map.Entry<String, HashSet<String>>> i = sub.entrySet().iterator();i.hasNext();) {
        Map.Entry<String, HashSet<String>> entry = i.next();
        if (entry.getValue().isEmpty()) {
          i.remove();
          AssetNode asset = assets.get(entry.getKey());
          resolved.addLast(Asset.uri(asset.getLocation(), asset.getValue()));
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
    for (juzu.asset.Asset script : scripts) {
      if (script instanceof Asset.Value) {
        Asset.Value script1 = (Asset.Value)script;
        String uri = script1.getURI();
        resolved.addLast(Asset.uri(script1.getLocation(), uri));
      }
    }

    //
    return resolved;
  }
}
