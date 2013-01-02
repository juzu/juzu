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

package juzu.impl.plugin.asset;

import juzu.impl.common.Name;
import juzu.impl.common.Path;
import juzu.impl.common.Tools;
import juzu.impl.compiler.ElementHandle;
import juzu.impl.common.FileKey;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class AssetMetaModelPlugin extends ApplicationMetaModelPlugin {

  /** . */
  private HashMap<ElementHandle.Package, AnnotationState> annotations = new HashMap<ElementHandle.Package, AnnotationState>();

  /** . */
  private static final Name ASSETS = Name.create(Assets.class);

  public AssetMetaModelPlugin() {
    super("asset");
  }

  @Override
  public Set<Class<? extends java.lang.annotation.Annotation>> init(ProcessingContext env) {
    return Collections.<Class<? extends java.lang.annotation.Annotation>>singleton(Assets.class);
  }

  @Override
  public void processAnnotationAdded(ApplicationMetaModel metaModel, AnnotationKey key, AnnotationState added) {
    if (key.getType().equals(ASSETS) && metaModel.getHandle().equals(key.getElement())) {
      annotations.put(metaModel.getHandle(), added);
    }
  }

  @Override
  public void processAnnotationRemoved(ApplicationMetaModel metaModel, AnnotationKey key, AnnotationState removed) {
    if (key.getType().equals(ASSETS) && metaModel.getHandle().equals(key.getElement())) {
      annotations.remove(metaModel.getHandle());
    }
  }

  private List<JSON> build(List<Map<String, Object>> scripts) {
    List<JSON> foo = Collections.emptyList();
    if (scripts != null && scripts.size() > 0) {
      foo = new ArrayList<JSON>(scripts.size());
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

  private static final String[] KINDS = {"scripts","stylesheets"};

  @Override
  public void prePassivate(ApplicationMetaModel metaModel) {
    AnnotationState annotation = annotations.get(metaModel.getHandle());
    if (annotation != null) {
      String location = (String)annotation.get("location");
      boolean classpath = location == null || "CLASSPATH".equals(location);
      for (String kind : KINDS) {
        List<AnnotationState> scripts = (List<AnnotationState>)annotation.get(kind);
        ProcessingContext context = metaModel.getProcessingContext();
        if (scripts != null) {
          for (AnnotationState script : scripts) {
            location = (String)script.get("location");
            if ((location == null && classpath) || "CLASSPATH".equals(location)) {
              String value = (String)script.get("src");
              Path path = Path.parse(value);
              FileKey absolute;
              if (path.isRelative()) {
                context.log("Found classpath asset to copy " + value);
                Name qn = metaModel.getHandle().getPackage().append("assets");
                absolute = qn.resolve(path);
                FileObject src = context.resolveResource(metaModel.getHandle(), absolute);
                if (src != null) {
                  URI srcURI = src.toUri();
                  context.log("Found asset " + absolute + " on source path " + srcURI);
                  InputStream in = null;
                  OutputStream out = null;
                  try {
                    FileObject dst = context.getResource(StandardLocation.CLASS_OUTPUT, absolute);
                    if (dst == null || dst.getLastModified() < src.getLastModified()) {
                      in = src.openInputStream();
                      dst = context.createResource(StandardLocation.CLASS_OUTPUT, absolute, context.get(metaModel.getHandle()));
                      context.log("Copying asset from source path " + srcURI + " to class output " + dst.toUri());
                      out = dst.openOutputStream();
                      Tools.copy(in, out);
                    } else {
                      context.log("Found up to date related asset in class output for " + srcURI);
                    }
                  }
                  catch (IOException e) {
                    context.log("Could not copy asset " + path + " ", e);
                  }
                  finally {
                    Tools.safeClose(in);
                    Tools.safeClose(out);
                  }
                } else {
                  context.log("Could not find asset " + absolute + " on source path");
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
    AnnotationState annotation = annotations.get(application.getHandle());
    if (annotation != null) {
      JSON json = new JSON();
      json.set("scripts", build((List<Map<String, Object>>)annotation.get("scripts")));
      json.set("stylesheets", build((List<Map<String, Object>>)annotation.get("stylesheets")));
      json.set("package", application.getName().append("assets").toString());
      json.set("location", annotation.get("location"));
      return json;
    } else {
      return null;
    }
  }
}
