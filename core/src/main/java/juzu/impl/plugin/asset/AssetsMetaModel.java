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

import juzu.impl.asset.AssetServer;
import juzu.impl.common.MethodInvocation;
import juzu.impl.common.MethodInvocationResolver;
import juzu.impl.compiler.ElementHandle;
import juzu.impl.metamodel.AnnotationState;
import juzu.impl.metamodel.Key;
import juzu.impl.metamodel.MetaModelObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/** @author Julien Viet */
public class AssetsMetaModel extends MetaModelObject implements MethodInvocationResolver {

  /** . */
  public final static Key<AssetsMetaModel> KEY = Key.of(AssetsMetaModel.class);

  /** . */
  private final ArrayList<Asset> assets = new ArrayList<Asset>();

  public void addAsset(Asset asset) {
    assets.add(asset);
  }

  public Iterable<Asset> getAssets() {
    return assets;
  }

  public void removeAssets(String type) {
    for (Iterator<Asset> i = assets.iterator();i.hasNext();) {
      Asset asset = i.next();
      if (asset.type.equals(type)) {
        i.remove();
      }
    }
  }

  public MethodInvocation resolveMethodInvocation(String typeName, String methodName, Map<String, String> parameterMap) {
    if ("Assets".equals(typeName) && methodName.equals("url")) {
      String path = parameterMap.get("path");
      if (path != null) {
        return new MethodInvocation(AssetServer.class.getName(), "renderAssetURL", Collections.singletonList(path));
      }
    }
    return null;
  }
}
