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
import juzu.impl.asset.AssetServer;
import juzu.impl.common.MethodInvocation;
import juzu.impl.common.MethodInvocationResolver;
import juzu.impl.common.Name;
import juzu.impl.compiler.MessageCode;
import juzu.impl.compiler.ProcessingContext;
import juzu.impl.compiler.ProcessingException;
import juzu.impl.metamodel.Key;
import juzu.impl.metamodel.MetaModelObject;
import juzu.impl.plugin.application.metamodel.ApplicationMetaModel;

import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

/** @author Julien Viet */
public class AssetsMetaModel extends MetaModelObject implements MethodInvocationResolver {

  /** . */
  public static final MessageCode UNRESOLVED_ASSET = new MessageCode("UNRESOLVED_ASSET", "The application asset %1s cannot be resolved");

  /** . */
  public final static Key<AssetsMetaModel> KEY = Key.of(AssetsMetaModel.class);

  /** . */
  private final ArrayList<Asset> assets = new ArrayList<Asset>();

  /** . */
  private final HashMap<String, URL> resources = new HashMap<String, URL>();

  public void addAsset(Asset asset) {

    //
    if (AssetLocation.APPLICATION == asset.location) {
      URL url = resolve(asset.value);
      if (url != null) {
        add(asset.value, url);
      }
    }

    //
    assets.add(asset);
  }

  public void removeAsset(Asset asset) {

    //
    if (AssetLocation.APPLICATION == asset.location) {
      URL url = resolve(asset.value);
      if (url != null) {
        remove(asset.value, url);
      }
    }

    //
    for (Iterator<Asset> i = assets.iterator();i.hasNext();) {
      Asset candidate = i.next();
      if (asset.value.equals(candidate.value) && asset.location.equals(candidate.location)) {
        i.remove();
      }
    }
  }

  public void add(String path, URL resource) {
    URL existing = resources.get(path);
    if (existing != null) {
      if (!existing.equals(resource)) {
        throw new UnsupportedOperationException("Resource conflict for path " + path + " : " + resource + " != " + existing);
      }
    } else {
      resources.put(path, resource);
    }
  }

  public void remove(String path, URL resource) {
    URL existing = resources.get(path);
    if (existing != null) {
      if (existing.equals(resource)) {
        resources.remove(path);
      }
    }
  }

  public URL getResource(String path) {
    return resources.get(path);
  }

  public Map<String, URL> getResources() {
    return resources;
  }

  public Iterable<Asset> getAssets() {
    return assets;
  }

  public Iterable<Asset> getAssets(final String type) {
    return new Iterable<Asset>() {
      public Iterator<Asset> iterator() {
        final Iterator<Asset> i = assets.iterator();
        return new Iterator<Asset>() {
          Asset next = null;
          public boolean hasNext() {
            while (next == null && i.hasNext()) {
              Asset asset = i.next();
              if (asset.type.equals(type)) {
                next = asset;
              }
            }
            return next != null;
          }
          public Asset next() {
            if (hasNext()) {
              Asset tmp = next;
              next = null;
              return tmp;
            } else {
              throw new NoSuchElementException();
            }
          }
          public void remove() {
            throw new UnsupportedOperationException();
          }
        };
      }
    };
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

  private URL resolve(String value) throws ProcessingException {
    ApplicationMetaModel application = (ApplicationMetaModel)metaModel;
    ProcessingContext context = application.getProcessingContext();
    boolean relative = value.length() == 0 || value.charAt(0) != '/';
    if (relative) {
      context.info("Found classpath asset " + value);
      Name qn = application.getHandle().getPackageName().append("assets");
      FileObject src;
      try {
        src = context.getResource(StandardLocation.SOURCE_PATH, qn, value);
      }
      catch (Exception e) {
        if (e.getClass().getName().equals("com.sun.tools.javac.util.ClientCodeException") && e.getCause() instanceof NullPointerException) {
          // com.sun.tools.javac.util.ClientCodeException: java.lang.NullPointerException
          // Bug in java compiler for file not found
          // at com.sun.tools.javac.util.ClientCodeException: java.lang.NullPointerException
          // at com.sun.tools.javac.api.ClientCodeWrapper$WrappedJavaFileManager.getFileForInput(ClientCodeWrapper.java:307)
          // at com.sun.tools.javac.processing.JavacFiler.getResource(JavacFiler.java:472)
          src = null;
        } else {
          throw UNRESOLVED_ASSET.failure(value).initCause(e);
        }
      }
      if (src != null) {
        URI uri = src.toUri();
        context.info("Found asset " + value + " on source path " + uri);
        try {
          return uri.toURL();
        }
        catch (MalformedURLException e) {
          throw UNRESOLVED_ASSET.failure(uri).initCause(e);
        }
      } else {
        context.info("Could not find asset " + value + " on source path");
        return null;
      }
    } else {
      return null;
    }
  }
}
