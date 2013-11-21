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
import juzu.impl.common.Path;
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
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class AssetMetaModelPlugin extends ApplicationMetaModelPlugin {

  /** . */
  private static final String[] KINDS = {"value"};


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
    metaModel.getChild(AssetsMetaModel.KEY).annotations.put(metaModel.getHandle(), added);
  }

  @Override
  public void processAnnotationRemoved(ApplicationMetaModel metaModel, AnnotationKey key, AnnotationState removed) {
    metaModel.getChild(AssetsMetaModel.KEY).annotations.remove(metaModel.getHandle());
  }

  private List<JSON> build(List<Map<String, Object>> scripts) {
    List<JSON> foo = new ArrayList<JSON>();
    if (scripts != null) {
      for (Map<String, Object> script : scripts) {
        JSON bar = new JSON();
        for (Map.Entry<String, Object> entry : script.entrySet()) {
          bar.set(entry.getKey(), entry.getValue());
        }
        foo.add(bar);
      }
    }
    return foo;
  }

  @Override
  public void prePassivate(ApplicationMetaModel metaModel) {
    ProcessingContext context = metaModel.getProcessingContext();
    if(!context.isCopyFromSourcesExternallyManaged()) {
      AnnotationState annotation = metaModel.getChild(AssetsMetaModel.KEY).annotations.get(metaModel.getHandle());
      if (annotation != null) {
        String location = (String)annotation.get("location");
        boolean classpath = location == null || AssetLocation.APPLICATION.equals(AssetLocation.safeValueOf(location));
        for (String kind : KINDS) {
          List<AnnotationState> scripts = (List<AnnotationState>)annotation.get(kind);        
          if (scripts != null) {
            for (AnnotationState script : scripts) {
              location = (String)script.get("location");
              if ((location == null && classpath) || AssetLocation.APPLICATION.equals(AssetLocation.safeValueOf(location))) {
                List<String> values = (List<String>)script.get("value");
                for (String value : values) {
                  Path path = Path.parse(value);
                  if (path.isRelative()) {
                    context.info("Found classpath asset to copy " + value);
                    Name qn = metaModel.getHandle().getPackageName().append("assets");
                    Path.Absolute absolute = qn.resolve(path);
                    FileObject src = context.resolveResourceFromSourcePath(metaModel.getHandle(), absolute);
                    if (src != null) {
                      URI srcURI = src.toUri();
                      context.info("Found asset " + absolute + " on source path " + srcURI);
                      InputStream in = null;
                      OutputStream out = null;
                      try {
                        FileObject dst = context.getResource(StandardLocation.CLASS_OUTPUT, absolute);
                        if (dst == null || dst.getLastModified() < src.getLastModified()) {
                          in = src.openInputStream();
                          dst = context.createResource(StandardLocation.CLASS_OUTPUT, absolute, context.get(metaModel.getHandle()));
                          context.info("Copying asset from source path " + srcURI + " to class output " + dst.toUri());
                          out = dst.openOutputStream();
                          Tools.copy(in, out);
                        } else {
                          context.info("Found up to date related asset in class output for " + srcURI);
                        }
                      }
                      catch (IOException e) {
                        context.info("Could not copy asset " + path + " ", e);
                      }
                      finally {
                        Tools.safeClose(in);
                        Tools.safeClose(out);
                      }
                    } else {
                      context.info("Could not find asset " + absolute + " on source path");
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  @Override
  public JSON getDescriptor(ApplicationMetaModel application) {
    AnnotationState annotation = application.getChild(AssetsMetaModel.KEY).annotations.get(application.getHandle());
    if (annotation != null) {
      JSON json = new JSON();
      List<JSON> assets = build((List<Map<String, Object>>)annotation.get("value"));
      json.set("assets", assets);
      json.set("package", "assets");
      json.set("location", annotation.get("location"));
      return json;
    } else {
      return null;
    }
  }
}
