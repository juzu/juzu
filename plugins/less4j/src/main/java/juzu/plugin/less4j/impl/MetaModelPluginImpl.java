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

package juzu.plugin.less4j.impl;

import juzu.asset.AssetLocation;
import juzu.impl.plugin.application.metamodel.ApplicationMetaModel;
import juzu.impl.plugin.application.metamodel.ApplicationMetaModelPlugin;
import juzu.impl.plugin.asset.Asset;
import juzu.impl.plugin.asset.AssetsMetaModel;
import juzu.impl.metamodel.AnnotationKey;
import juzu.impl.metamodel.AnnotationState;
import juzu.impl.compiler.BaseProcessor;
import juzu.impl.compiler.MessageCode;
import juzu.impl.compiler.ProcessingContext;
import juzu.impl.common.Logger;
import juzu.plugin.less4j.Less;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class MetaModelPluginImpl extends ApplicationMetaModelPlugin {

  /** . */
  public static final MessageCode GENERAL_PROBLEM = new MessageCode(
      "GENERAL_PROBLEM",
      "%1$s:\n%2$s");

  /** . */
  public static final MessageCode COMPILATION_ERROR = new MessageCode(
    "LESS_COMPILATION_ERROR",
    "%1$s in %2$s on line %3$s:\n%4$s");

  /** . */
  public static final MessageCode MALFORMED_PATH = new MessageCode("LESS_MALFORMED_PATH", "The resource path %1$s is malformed");

  /** . */
  static final Logger log = BaseProcessor.getLogger(MetaModelPluginImpl.class);

  public MetaModelPluginImpl() {
    super("less");
  }

  @Override
  public void init(ApplicationMetaModel metaModel) {
  }

  @Override
  public Set<Class<? extends java.lang.annotation.Annotation>> init(ProcessingContext env) {
    return Collections.<Class<? extends java.lang.annotation.Annotation>>singleton(Less.class);
  }

  @Override
  public void processAnnotationAdded(ApplicationMetaModel metaModel, AnnotationKey key, AnnotationState added) {
    AssetsMetaModel assetsMetaModel = metaModel.getChild(AssetsMetaModel.KEY);
    List<LessAsset> assets = getAssets(assetsMetaModel, added);
    for (LessAsset asset : assets) {
      assetsMetaModel.addAsset(asset);
    }
  }

  @Override
  public void processAnnotationRemoved(ApplicationMetaModel metaModel, AnnotationKey key, AnnotationState removed) {
    AssetsMetaModel assetsMetaModel = metaModel.getChild(AssetsMetaModel.KEY);
    List<LessAsset> assets = getAssets(assetsMetaModel, removed);
    for (LessAsset asset : assets) {
      assetsMetaModel.removeAsset(asset);
    }
  }

  private List<LessAsset> getAssets(
      AssetsMetaModel assetsMetaModel,
      AnnotationState annotation) {

    //
    List<LessAsset> assets = Collections.emptyList();
    List<AnnotationState> value = (List<AnnotationState>)annotation.get("value");
    if (value != null) {
      for (AnnotationState assetAnnotation : value) {

        //
        String assetValue = (String)assetAnnotation.get("value");

        // Clone the annotation for modifying it
        Map<String, Serializable> state = new HashMap<String, Serializable>(assetAnnotation);

        //
        if (state.get("id") == null) {
          state.put("id", assetValue);
        }
        Serializable location = state.get("location");
        if (location == null) {
          state.put("location", AssetLocation.APPLICATION.name());
        } else {
          if (!location.equals(AssetLocation.APPLICATION.name())) {
            throw new UnsupportedOperationException("handle me gracefully");
          }
        }

        //
        Asset parsed = new Asset("stylesheet", state);
        LessAsset asset = new LessAsset(
            parsed.id,
            parsed.key.value,
            parsed.depends,
            parsed.maxAge
        );
        if (assets.isEmpty()) {
          assets = new ArrayList<LessAsset>();
        }
        assets.add(asset);
      }
    }
    return assets;
  }
}
