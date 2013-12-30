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
import juzu.impl.common.Name;
import juzu.impl.common.Tools;
import juzu.impl.plugin.application.metamodel.ApplicationMetaModel;
import juzu.impl.plugin.application.metamodel.ApplicationMetaModelPlugin;
import juzu.impl.metamodel.AnnotationKey;
import juzu.impl.metamodel.AnnotationState;
import juzu.impl.common.JSON;
import juzu.impl.compiler.ProcessingContext;
import juzu.plugin.asset.Assets;

import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class AssetMetaModelPlugin extends ApplicationMetaModelPlugin {

  public AssetMetaModelPlugin() {
    super("asset");
  }

  @Override
  public Set<Class<? extends java.lang.annotation.Annotation>> init(ProcessingContext env) {
    return Collections.<Class<? extends java.lang.annotation.Annotation>>singleton(Assets.class);
  }

  @Override
  public void init(ApplicationMetaModel metaModel) {
    metaModel.addChild(AssetsMetaModel.KEY, new AssetsMetaModel());
  }

  @Override
  public void processAnnotationAdded(ApplicationMetaModel metaModel, AnnotationKey key, AnnotationState added) {
    if (metaModel.getHandle().equals(key.getElement())) {
      AssetsMetaModel assetsMetaModel = metaModel.getChild(AssetsMetaModel.KEY);
      for (Asset asset : getAssets(added)) {
        assetsMetaModel.addAsset(asset);
      }
    }
  }

  @Override
  public void processAnnotationRemoved(ApplicationMetaModel metaModel, AnnotationKey key, AnnotationState removed) {
    if (metaModel.getHandle().equals(key.getElement())) {
      AssetsMetaModel assetsMetaModel = metaModel.getChild(AssetsMetaModel.KEY);
      for (Asset asset : getAssets(removed)) {
        assetsMetaModel.removeAsset(asset);
      }
    }
  }

  private Iterable<Asset> getAssets(AnnotationState annotation) {
    ArrayList<Asset> assets = new ArrayList<Asset>();
    String location = (String)annotation.get("location");
    if (location == null) {
      location = AssetLocation.APPLICATION.name();
    }
    List<AnnotationState> value = (List<AnnotationState>)annotation.get("value");
    for (AnnotationState asset : value) {
      Map<String, Serializable> state = new HashMap<String, Serializable>(asset);
      if (state.get("location") == null) {
        state.put("location", location);
      }
      if (state.get("id") == null) {
        state.put("id", state.get("value"));
      }
      String v = (String)state.get("value");
      String type;
      if (v.endsWith(".js")) {
        type = "script";
      } else if (v.endsWith(".css")) {
        type = "stylesheet";
      } else if (v.endsWith(".less")) {
        type = "stylesheet";
      } else {
        throw new UnsupportedOperationException("Handle me gracefully " + v);
      }
      assets.add(new Asset(type, state));
    }
    return assets;
  }

  @Override
  public void prePassivate(ApplicationMetaModel metaModel) {
    ProcessingContext context = metaModel.getProcessingContext();
    Name qn = metaModel.getHandle().getPackageName().append("assets");
    if(!context.isCopyFromSourcesExternallyManaged()) {
      AssetsMetaModel annotation = metaModel.getChild(AssetsMetaModel.KEY);
      for (Map.Entry<String, URL> entry : annotation.getResources().entrySet()) {
        InputStream in = null;
        OutputStream out = null;
        try {
          URL src = entry.getValue();
          URLConnection conn = src.openConnection();
          FileObject dst = context.getResource(StandardLocation.CLASS_OUTPUT, qn, entry.getKey());
          if (dst == null || dst.getLastModified() < conn.getLastModified()) {
            in = conn.getInputStream();
            dst = context.createResource(StandardLocation.CLASS_OUTPUT, qn, entry.getKey(), context.get(metaModel.getHandle()));
            context.info("Copying asset from source path " + src + " to class output " + dst.toUri());
            for (Asset asset : annotation.getAssets()) {
              if (asset.value.equals(entry.getKey())) {
                in = asset.filter(in);
                break;
              }
            }
            out = dst.openOutputStream();
            Tools.copy(in, out);
          } else {
            context.info("Found up to date related asset in class output for " + src);
          }
        }
        catch (IOException e) {
          context.info("Could not copy asset " + entry.getKey() + " ", e);
        }
        finally {
          Tools.safeClose(in);
          Tools.safeClose(out);
        }
      }
    }
  }

  @Override
  public JSON getDescriptor(ApplicationMetaModel application) {
    AssetsMetaModel assetsMetaModel = application.getChild(AssetsMetaModel.KEY);
    Iterator<Asset> assets = assetsMetaModel.getAssets().iterator();
    if (assets.hasNext()) {
      JSON json = new JSON();
      List<JSON> list = new ArrayList<JSON>();
      while (assets.hasNext()) {
        list.add(assets.next().getJSON());
      }
      json.set("assets", list);
      json.set("package", "assets");
      return json;
    } else {
      return null;
    }
  }
}
