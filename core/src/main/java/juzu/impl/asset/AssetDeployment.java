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

import java.net.URL;
import java.util.ArrayList;
import java.util.Set;

/**
 * @author Julien Viet
 */
public class AssetDeployment {

  /** The assets for this deployment. */
  final ArrayList<AssetNode> assets = new ArrayList<AssetNode>();

  /** . */
  private final AssetManager manager;

  /** . */
  private boolean deployed;

  AssetDeployment(AssetManager manager) {
    this.manager = manager;
  }

  public AssetDeployment addAsset(
      String id,
      String type,
      AssetLocation location,
      String value,
      String minifiedValue,
      Integer maxAge,
      URL url,
      String... dependencies) throws NullPointerException, IllegalArgumentException {
    return addAsset(id, type, location, value, minifiedValue, maxAge, url, Tools.set(dependencies));
  }

  /**
   * <p>Attempt to add an asset to the manager, the manager will return the asset id
   * if the asset was registered or null if it was not.</p>
   *
   * <p>When no asset id is specified, an asset id will be generated from the asset value by taking
   * the longest trailing substring that contains no <code>/</code> char.</p>
   *
   * @param id the asset id
   * @param type the asset type
   * @param location the asset location
   * @param value the asset value
   * @param maxAge the asset max age
   * @param resource the asset resource
   * @param dependencies the asset dependencies
   * @throws NullPointerException     if the metaData argument is nul
   * @throws IllegalArgumentException if the metaData does not have an id set
   */
  public AssetDeployment addAsset(
      String id,
      String type,
      AssetLocation location,
      String value,
      String minifiedValue,
      Integer maxAge,
      URL resource,
      Set<String> dependencies) throws NullPointerException, IllegalArgumentException {
    assets.add(new AssetNode(id, type, location, value, minifiedValue, maxAge, resource, dependencies));
    return this;
  }

  /**
   * Attempt to deploy.
   *
   * @return if the deployment was succesfully deployed
   */
  public boolean deploy() {
    return deployed = manager.deploy(this);
  }

  /**
   * Undeploy.
   */
  public void undeploy() {
    if (deployed) {
      deployed = false;
      manager.undeploy(this);
    }
  }


}
