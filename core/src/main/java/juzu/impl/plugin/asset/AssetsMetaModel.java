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

  /**
   * Links resource URLs to target: some resources may have the same target, it should
   * be an issue that we detect in the resolving phase and produce an error.
   */
  private final HashMap<URL, String> resources = new HashMap<URL, String>();

  public void addAsset(Asset asset) {

    //
    if (asset.isApplication()) {
      URL url = resolveResource(asset.value);
      if (url != null) {
        addResource(asset.value, url);
      }
    }

    //
    assets.add(asset);
  }

  public void removeAsset(Asset asset) {

    //
    if (asset.isApplication()) {
      URL url = resolveResource(asset.value);
      if (url != null) {
        removeResource(asset.value, url);
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

  /**
   * Associate a resource with a physical path in the application.
   *
   * @param path the resource path
   * @param resource the resource
   */
  public void addResource(String path, URL resource) {
    String existing = resources.get(resource);
    if (existing != null) {
      throw new UnsupportedOperationException("Cannot add resource " + resource + " : conflict " + path + " != " + existing);
    } else {
      resources.put(resource, path);
    }
  }

  /**
   * Remove a resource association from the application.
   *
   * @param path the resource path
   * @param resource the resource
   */
  public void removeResource(String path, URL resource) {
    String existing = resources.get(resource);
    if (path.equals(existing)) {
      resources.remove(resource);
    } else {
      throw new UnsupportedOperationException("Resource conflict for resource " + resource + " : path " + path +
          " not matched by " + existing);
    }
  }

  /**
   * Resolve a relative resource for this application, this method does not modify the current application.
   *
   * @param path the resource value
   * @return the related resource URL or null if it cannot be resolved
   * @throws ProcessingException relate any processing issue
   */
  public URL resolveResource(String path) throws ProcessingException {
    ApplicationMetaModel application = (ApplicationMetaModel)metaModel;
    ProcessingContext context = application.getProcessingContext();
    boolean relative = path.length() == 0 || path.charAt(0) != '/';
    if (relative) {
      context.info("Found classpath asset " + path);
      Name qn = application.getHandle().getPackageName().append("assets");
      FileObject src;
      try {
        src = context.getResource(StandardLocation.SOURCE_PATH, qn, path);
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
          throw UNRESOLVED_ASSET.failure(path).initCause(e);
        }
      }
      if (src != null) {
        URI uri = src.toUri();
        context.info("Found asset " + path + " on source path " + uri);
        try {
          return uri.toURL();
        }
        catch (MalformedURLException e) {
          throw UNRESOLVED_ASSET.failure(uri).initCause(e);
        }
      } else {
        context.info("Could not find asset " + path + " on source path");
        return null;
      }
    } else {
      return null;
    }
  }

  public Map<URL, String> getResources() {
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
}
