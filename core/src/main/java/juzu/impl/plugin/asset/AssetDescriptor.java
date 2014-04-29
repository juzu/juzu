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

import juzu.Scope;
import juzu.impl.asset.AssetManager;
import juzu.impl.asset.AssetMetaData;
import juzu.impl.plugin.ServiceDescriptor;
import juzu.impl.common.Tools;
import juzu.impl.inject.BeanDescriptor;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class AssetDescriptor extends ServiceDescriptor {

  /** . */
  private final List<AssetMetaData> assets;

  AssetDescriptor(List<AssetMetaData> assets) {
    this.assets = assets;
  }

  public List<AssetMetaData> getAssets() {
    return assets;
  }

  @Override
  public Iterable<BeanDescriptor> getBeans() {
    return Tools.list(
        BeanDescriptor.createFromBean(
            AssetManager.class,
            Scope.SINGLETON,
            Collections.<Annotation>emptyList()));
  }
}
