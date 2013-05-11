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
import juzu.impl.common.NameLiteral;
import juzu.impl.plugin.PluginDescriptor;
import juzu.impl.common.Tools;
import juzu.impl.inject.BeanDescriptor;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class AssetDescriptor extends PluginDescriptor {

  /** . */
  private final List<AssetMetaData> scripts;

  /** . */
  private final List<AssetMetaData> stylesheets;

  /** . */
  private final List<AssetMetaData> declaredScripts;

  /** . */
  private final List<AssetMetaData> declaredStylesheets;

  AssetDescriptor(
      List<AssetMetaData> scripts,
      List<AssetMetaData> declaredScripts,
      List<AssetMetaData> stylesheets,
      List<AssetMetaData> declaredStylesheets) {
    this.scripts = scripts;
    this.stylesheets = stylesheets;
    this.declaredScripts = declaredScripts;
    this.declaredStylesheets = declaredStylesheets;
  }

  public List<AssetMetaData> getScripts() {
    return scripts;
  }

  public List<AssetMetaData> getStylesheets() {
    return stylesheets;
  }

  public List<AssetMetaData> getDeclaredScripts() {
    return declaredScripts;
  }

  public List<AssetMetaData> getDeclaredStylesheets() {
    return declaredStylesheets;
  }

  @Override
  public Iterable<BeanDescriptor> getBeans() {
    return Tools.list(
        BeanDescriptor.createFromBean(
            AssetManager.class,
            Scope.SINGLETON,
            Collections.<Annotation>singletonList(new NameLiteral("juzu.asset_manager.script"))),
        BeanDescriptor.createFromBean(
            AssetManager.class,
            Scope.SINGLETON,
            Collections.<Annotation>singletonList(new NameLiteral("juzu.asset_manager.stylesheet"))));
  }
}
