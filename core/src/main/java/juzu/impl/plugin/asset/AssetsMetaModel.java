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
import juzu.impl.compiler.ElementHandle;
import juzu.impl.compiler.MessageCode;
import juzu.impl.compiler.ProcessingContext;
import juzu.impl.compiler.ProcessingException;
import juzu.impl.metamodel.Key;
import juzu.impl.metamodel.MetaModelObject;
import juzu.impl.plugin.application.metamodel.ApplicationMetaModel;

import javax.tools.FileObject;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
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
  private final HashMap<AssetKey, Asset> assets = new HashMap<AssetKey, Asset>();

  /**
   * Links resource URLs to target: some resources may have the same target, it should
   * be an issue that we detect in the resolving phase and produce an error.
   */
  private final HashMap<String, URL> resources = new HashMap<String, URL>();

  /** . */
  private final ElementHandle.Package pkg;

  public AssetsMetaModel(ElementHandle.Package pkg) {
    this.pkg = pkg;
  }

  public void addAsset(Asset asset) {
    assets.put(asset.key, asset);
  }

  public void removeAsset(Asset asset) {
    assets.remove(asset.key);
  }

  /**
   * Associate a resource with a physical path in the application.
   *
   * @param path the resource path
   * @param resource the resource
   */
  public void addResource(String path, URL resource) {
    URL existing = resources.get(path);
    if (existing != null) {
      throw new UnsupportedOperationException("Cannot add resource " + path + " : added " + resource + " != existing " + existing);
    } else {
      resources.put(path, resource);
    }
  }

  /**
   * Remove a resource association from the application.
   *
   * @param path the resource path
   * @param resource the resource
   */
  public void removeResource(String path, URL resource) {
    URL existing = resources.get(path);
    if (resource.equals(existing)) {
      resources.remove(path);
    } else {
      throw new UnsupportedOperationException("Resource conflict for resource " + path + " : removed " + resource +
          " not matched by existing " + existing);
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
      context.info("Resolving classpath asset " + path);
      Name qn = application.getHandle().getPackageName().append("assets");
      FileObject src;
      try {
        src = context.resolveResourceFromSourcePath(pkg, qn, path);
      }
      catch (Exception e) {
        throw UNRESOLVED_ASSET.failure(path).initCause(e);
      }
      if (src != null) {
        URI uri = src.toUri();
        context.info("Found asset " + path + " on source path " + uri);
        try {
          String scheme = uri.getScheme();
          if (scheme == null) {
            uri = new URI("file:" + uri);
          }
          return uri.toURL();
        }
        catch (URISyntaxException e) {
          throw UNRESOLVED_ASSET.failure(uri).initCause(e);
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

  public Map<String, URL> getResources() {
    return resources;
  }

  public Iterable<Asset> getAssets() {
    return assets.values();
  }

  public Iterable<Asset> getAssets(final String type) {
    return new Iterable<Asset>() {
      public Iterator<Asset> iterator() {
        final Iterator<Asset> i = assets.values().iterator();
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
    for (Iterator<Asset> i = assets.values().iterator();i.hasNext();) {
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
        return new MethodInvocation(AssetServer.class.getName(), "renderAssetURLByPath", Collections.singletonList(path));
      } else {
        String id = parameterMap.get("id");
        if (id != null) {
          return new MethodInvocation(AssetServer.class.getName(), "renderAssetURLById", Collections.singletonList(id));
        }
      }
    }
    return null;
  }
}
