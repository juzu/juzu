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
import juzu.impl.compiler.CompilationException;
import juzu.impl.compiler.ElementHandle;
import juzu.impl.compiler.MessageCode;
import juzu.impl.plugin.application.metamodel.ApplicationMetaModel;
import juzu.impl.plugin.application.metamodel.ApplicationMetaModelPlugin;
import juzu.impl.metamodel.AnnotationKey;
import juzu.impl.metamodel.AnnotationState;
import juzu.impl.common.JSON;
import juzu.impl.compiler.ProcessingContext;
import juzu.plugin.asset.Scripts;
import juzu.plugin.asset.Stylesheets;

import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class AssetMetaModelPlugin extends ApplicationMetaModelPlugin {

  /** . */
  public static final MessageCode ASSET_NOT_FOUND = new MessageCode("ASSET_NOT_FOUND", "The asset %1$s cannot be resolved");

  /** . */
  public static final MessageCode DUPLICATE_ASSET_ID = new MessageCode("DUPLICATE_ASSET_ID", "The asset id %1$s must be used once");

  /** . */
  public static final MessageCode CANNOT_PROCESS_ASSET = new MessageCode("CANNOT_PROCESS_ASSET", "The asset id %1$s cannot be processed: %2d");

  /** . */
  private static final Set<Class<? extends java.lang.annotation.Annotation>> ANNOTATIONS;

  static {
    HashSet<Class<? extends Annotation>> tmp = new HashSet<Class<? extends Annotation>>();
    tmp.add(Scripts.class);
    tmp.add(Stylesheets.class);
    ANNOTATIONS = Collections.unmodifiableSet(tmp);
  }

  public AssetMetaModelPlugin() {
    super("asset");
  }

  @Override
  public Set<Class<? extends java.lang.annotation.Annotation>> init(ProcessingContext env) {
    return ANNOTATIONS;
  }

  @Override
  public void init(ApplicationMetaModel metaModel) {
    metaModel.addChild(AssetsMetaModel.KEY, new AssetsMetaModel(metaModel.getHandle()));
  }

  @Override
  public void processAnnotationAdded(ApplicationMetaModel metaModel, AnnotationKey key, AnnotationState added) {
    if (metaModel.getHandle().equals(key.getElement())) {
      AssetsMetaModel assetsMetaModel = metaModel.getChild(AssetsMetaModel.KEY);
      Integer maxAge = (Integer)added.get("maxAge");
      String type;
      String identifier = key.getType().getIdentifier();
      if (identifier.equals(Scripts.class.getSimpleName())) {
        type = "script";
      } else {
        type = "stylesheet";
      }
      List<ElementHandle.Type> minifiers = (List<ElementHandle.Type>)added.get("minifier");
      for (Asset asset : getAssets(type, added, maxAge, minifiers)) {
        assetsMetaModel.addAsset(asset);
      }
    }
  }

  @Override
  public void processAnnotationRemoved(ApplicationMetaModel metaModel, AnnotationKey key, AnnotationState removed) {
    if (metaModel.getHandle().equals(key.getElement())) {
      AssetsMetaModel assetsMetaModel = metaModel.getChild(AssetsMetaModel.KEY);
      for (Asset asset : getAssets(null, removed, null, null)) {
        assetsMetaModel.removeAsset(asset);
      }
    }
  }

  private Iterable<Asset> getAssets(
      String type,
      AnnotationState annotation,
      Integer maxAge,
      List<ElementHandle.Type> minifier) {
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
      if (maxAge != null && state.get("maxAge") == null) {
        state.put("maxAge", maxAge);
      }
      if (minifier != null && state.get("minifier") == null) {
        state.put("minifier", (Serializable)minifier);
      }
      if (state.get("id") == null) {
        state.put("id", state.get("value"));
      }
      assets.add(new Asset(type, state));
    }
    return assets;
  }

  @Override
  public void prePassivate(ApplicationMetaModel metaModel) {
    ProcessingContext context = metaModel.getProcessingContext();
    AssetsMetaModel assetMetaMode = metaModel.getChild(AssetsMetaModel.KEY);

    // Check duplicate ids
    HashSet<String> ids = new HashSet<String>();
    for (Asset asset : assetMetaMode.getAssets()) {
      if (!ids.add(asset.id)) {
        throw DUPLICATE_ASSET_ID.failure(asset.id);
      }
    }

    //
    Name qn = metaModel.getHandle().getPackageName().append("assets");
    if(!context.isCopyFromSourcesExternallyManaged()) {

      //
      HashMap<String, URL> bilta = new HashMap<String, URL>();
      HashMap<URL, Asset> bilto = new HashMap<URL, Asset>();
      for (Asset asset : assetMetaMode.getAssets()) {
        if (asset.isApplication()) {
          for (Map.Entry<String, String> entry : asset.getSources().entrySet()) {
            String source = entry.getValue();
            if (!source.startsWith("/")) {
              URL resource = assetMetaMode.getResources().get(source);
              if (resource == null) {
                resource = assetMetaMode.resolveResource(source);
              }
              if (resource != null) {
                bilto.put(resource, asset);
                bilta.put(entry.getKey(), resource);
              } else {
                throw ASSET_NOT_FOUND.failure(source);
              }
            }
          }
        }
      }
      bilta.putAll(assetMetaMode.getResources());

      // Process all resources
      for (Map.Entry<String, URL> entry : bilta.entrySet()) {
        InputStream in = null;
        OutputStream out = null;
        try {
          URL src = entry.getValue();
          URLConnection conn = src.openConnection();
          FileObject dst = context.getResource(StandardLocation.CLASS_OUTPUT, qn, entry.getKey());
          if (dst == null || dst.getLastModified() < conn.getLastModified()) {
            dst = context.createResource(StandardLocation.CLASS_OUTPUT, qn, entry.getKey(), context.get(metaModel.getHandle()));
            context.info("Copying asset from source path " + src + " to class output " + dst.toUri());
            Asset r = bilto.get(entry.getValue());
            if (r != null) {
              in = r.open(entry.getKey(), conn);
            } else {
              in = conn.getInputStream();
            }
            out = dst.openOutputStream();
            Tools.copy(in, out);
          } else {
            context.info("Found up to date related asset in class output for " + src);
          }
        }
        catch (IOException e) {
          throw CANNOT_PROCESS_ASSET.failure(entry.getKey(), e.getMessage());
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
    Iterator<Asset> assetsIterator = assetsMetaModel.getAssets().iterator();
    if (assetsIterator.hasNext()) {
      JSON descriptor = new JSON();
      JSON assets = new JSON();
      while (assetsIterator.hasNext()) {
        Asset asset = assetsIterator.next();
        assets.set(asset.id, asset.getJSON());
      }
      descriptor.set("assets", assets);
      descriptor.set("package", "assets");
      return descriptor;
    } else {
      return null;
    }
  }
}
