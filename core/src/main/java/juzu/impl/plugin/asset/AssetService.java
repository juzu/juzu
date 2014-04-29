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

import juzu.PropertyType;
import juzu.Response;
import juzu.asset.AssetLocation;
import juzu.impl.asset.AssetDeployment;
import juzu.impl.common.Name;
import juzu.impl.common.Tools;
import juzu.impl.plugin.ServiceContext;
import juzu.impl.plugin.ServiceDescriptor;
import juzu.impl.asset.AssetManager;
import juzu.impl.asset.AssetMetaData;
import juzu.impl.plugin.application.ApplicationService;
import juzu.impl.request.Request;
import juzu.impl.request.RequestFilter;
import juzu.impl.common.JSON;
import juzu.impl.request.Stage;
import juzu.plugin.asset.Assets;
import juzu.io.Chunk;
import juzu.io.Stream;
import juzu.impl.io.StreamableDecorator;
import juzu.request.Phase;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class AssetService extends ApplicationService implements RequestFilter<Stage.Unmarshalling> {

  /** . */
  private LinkedHashMap<String, Chunk.Property<String>> assets;

  /** . */
  private AssetDescriptor descriptor;

  /** . */
  private ServiceContext context;

  /** The path to the assets dir. */
  private String assetsPath;

  /** . */
  @Inject
  AssetManager assetManager;

  public AssetService() {
    super("asset");
  }

  public AssetManager getAssetManager() {
    return assetManager;
  }

  /**
   * Returns the plugin assets path.
   *
   * @return the assets path
   */
  public String getAssetsPath() {
    return assetsPath;
  }

  @Override
  public ServiceDescriptor init(ServiceContext context) throws Exception {
    JSON config = context.getConfig();
    String assetsPath;
    List<AssetMetaData> assets;
    if (config != null) {
      String packageName = config.getString("package");
      assets = load(packageName, config.getList("assets", JSON.class));
      assetsPath = "/" + Name.parse(application.getPackageName()).append(packageName).toString().replace('.', '/') + "/";
    } else {
      assets = Collections.emptyList();
      assetsPath = null;
    }
    this.descriptor = new AssetDescriptor(assets);
    this.context = context;
    this.assetsPath = assetsPath;
    return descriptor;
  }

  private List<AssetMetaData> load(
      String packageName,
      List<? extends JSON> scripts) throws Exception {
    List<AssetMetaData> abc = Collections.emptyList();
    if (scripts != null && scripts.size() > 0) {
      abc = new ArrayList<AssetMetaData>();
      for (JSON script : scripts) {
        String id = script.getString("id");
        AssetLocation location = AssetLocation.safeValueOf(script.getString("location"));

        //
        if (location == null) {
          location = AssetLocation.APPLICATION;
        }

        //
        String type = script.getString("type");

        //
        String value = script.getString("value");
        if (location == AssetLocation.APPLICATION && !value.startsWith("/")) {
          value = "/" + application.getPackageName().replace('.', '/') + "/" + packageName.replace('.', '/') + "/" + value;
        }
        String minified = script.getString("minified");
        if (location == AssetLocation.APPLICATION && minified != null && !minified.startsWith("/")) {
          minified = "/" + application.getPackageName().replace('.', '/') + "/" + packageName.replace('.', '/') + "/" + minified;
        }

        //
        Integer maxAge = script.getInteger("max-age");

        //
        AssetMetaData descriptor = new AssetMetaData(
          id,
          type,
          location,
          value,
          minified,
          maxAge,
          script.getArray("depends", String.class)
        );
        abc.add(descriptor);
      }
    }
    return abc;
  }

  @PostConstruct
  public void start() throws Exception {
    this.assets = process(descriptor.getAssets());
  }

  public URL resolve(AssetLocation location, String path) {
    switch (location) {
      case APPLICATION:
        return context.getApplicationResolver().resolve(path);
      case SERVER:
        return context.getServerResolver().resolve(path);
      default:
        return null;
    }
  }

  private LinkedHashMap<String, Chunk.Property<String>> process(List<AssetMetaData> data) throws Exception {
    LinkedHashMap<String, Chunk.Property<String>> assets = new LinkedHashMap<String, Chunk.Property<String>>();
    AssetDeployment deployment = assetManager.createDeployment();
    for (AssetMetaData script : data) {

      // Validate and resolve asset resources
      String[] a = new String[] { script.getValue(), script.getMinified() };
      URL[] resources = new URL[2];
      for (int i = 0;i < a.length; i++) {
        URL resource;
        String value = a[i];
        if (value != null) {
          AssetLocation location = script.getLocation();
          if (location == AssetLocation.APPLICATION) {
            URL url = resolve(AssetLocation.APPLICATION, value);
            if (url == null) {
              throw new Exception("Could not resolve application  " + value);
            } else {
              resource = url;
            }
          } else if (location == AssetLocation.SERVER) {
            if (!value.startsWith("/")) {
              URL url = resolve(AssetLocation.SERVER, "/" + value);
              if (url == null) {
                throw new Exception("Could not resolve server asset " + value);
              }
            }
            resource = null;
          } else {
            resource = null;
          }
        } else {
          resource = null;
        }
        resources[i] = resource;
      }


      //
      deployment.addAsset(script.getId(), script.getType(), script.getLocation(), a[0], a[1], script.getMaxAge(), resources[0], script.getDependencies());
      assets.put(script.getId(), new Chunk.Property<String>(script.getId(), PropertyType.ASSET));
    }

    // Should be true
    deployment.deploy();

    //
    return assets;
  }

  private Collection<Chunk.Property<String>> foo(AnnotatedElement elt, List<Chunk.Property<String>> bar) {
    Assets decl = elt.getAnnotation(Assets.class);
    if (decl != null) {
      String[] value = decl.value();
      for (String s : value) {
        if (s.equals("*")) {
          return assets.values();
        } else {
          Chunk.Property<String> p = assets.get(s);
          if (p == null) {
            throw new UnsupportedOperationException("handle me gracefully");
          } else {
            if (bar.size() == 0) {
              bar = new ArrayList<Chunk.Property<String>>();
            }
            bar.add(p);
          }
        }
      }
    }
    if (elt instanceof Method) {
      Method methodElt = (Method)elt;
      if (decl == null) {
        for (Class<?> current = methodElt.getDeclaringClass().getSuperclass();current != null;current = current.getSuperclass()) {
          try {
            methodElt = current.getDeclaredMethod(methodElt.getName(), methodElt.getParameterTypes());
            return foo(methodElt, bar);
          }
          catch (NoSuchMethodException ignore) {
          }
        }
      }
      return foo(methodElt.getDeclaringClass(), bar);
    } else if (elt instanceof Class<?>) {
      Class<?> classElt = (Class<Object>)elt;
      String pkgName;
      if (classElt.getSimpleName().equals("package-info")) {
        pkgName = Tools.parentPackageOf(Tools.parentPackageOf(classElt.getName()));
      } else {
        pkgName = Tools.parentPackageOf(classElt.getName());
      }
      while (pkgName != null) {
        Class<?> currentPackage = Tools.getPackageClass(Thread.currentThread().getContextClassLoader(), pkgName);
        if (currentPackage != null) {
          return foo(currentPackage, bar);
        } else {
          pkgName = Tools.parentPackageOf(pkgName);
        }
      }
      return bar;
    } else {
      return bar;
    }
  }

  @Override
  public Class<Stage.Unmarshalling> getStageType() {
    return Stage.Unmarshalling.class;
  }

  @Override
  public Response handle(Stage.Unmarshalling argument) {
    Response result = argument.invoke();
    Request request = argument.getRequest();
    if (request.getPhase() == Phase.VIEW) {
      if (result instanceof Response.Content) {
        final Collection<Chunk.Property<String>> bar = foo(request.getHandler().getMethod(), Collections.<Chunk.Property<String>>emptyList());
        Response.Status status = (Response.Status)result;
        if ((bar.size() > 0)) {
          status = new Response.Content(status.getCode(), new StreamableDecorator(status.streamable()) {
            @Override
            protected void sendHeader(Stream consumer) {
              for (Chunk.Property<String> asset : bar) {
                consumer.provide(asset);
              }
            }
          });
          result = status;
        }
      }
    }
    return result;
  }
}
