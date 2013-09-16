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
import juzu.impl.resource.ResourceResolver;

import javax.inject.Inject;
import java.net.URL;

/** @author Julien Viet */
public class AssetResolver implements ResourceResolver {

  @Inject AssetPlugin plugin;

  public URL resolve(String uri) {
    URL url = plugin.assetManager.resolveAsset(uri);
    if (url == null) {
      String assetsPath = plugin.getAssetsPath();
      if (assetsPath != null && uri.startsWith(assetsPath)) {
        url = plugin.resolve(AssetLocation.APPLICATION, uri);
      }
    }
    return url;
  }
}
