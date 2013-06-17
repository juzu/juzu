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

import juzu.PropertyMap;
import juzu.PropertyType;
import juzu.Response;
import juzu.asset.AssetLocation;
import juzu.impl.common.Name;
import juzu.impl.plugin.PluginDescriptor;
import juzu.impl.asset.AssetManager;
import juzu.impl.asset.AssetMetaData;
import juzu.impl.plugin.PluginContext;
import juzu.impl.plugin.application.ApplicationPlugin;
import juzu.impl.request.Request;
import juzu.impl.request.RequestFilter;
import juzu.impl.common.JSON;
import juzu.request.Phase;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class AssetPlugin extends ApplicationPlugin implements RequestFilter {

  /** . */
  private String[] scripts;

  /** . */
  private String[] declaredScripts;

  /** . */
  private String[] stylesheets;

  /** . */
  private String[] declaredStylesheets;

  /** . */
  private AssetDescriptor descriptor;

  /** . */
  private PluginContext context;

  /** The path to the assets dir. */
  private String assetsPath;

  /** . */
  @Inject
  @Named("juzu.asset_manager.script")
  AssetManager scriptManager;

  /** . */
  @Inject
  @Named("juzu.asset_manager.stylesheet")
  AssetManager stylesheetManager;

  public AssetPlugin() {
    super("asset");
  }

  public AssetManager getScriptManager() {
    return scriptManager;
  }

  public AssetManager getStylesheetManager() {
    return stylesheetManager;
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
  public PluginDescriptor init(PluginContext context) throws Exception {
    JSON config = context.getConfig();
    List<AssetMetaData> scripts;
    List<AssetMetaData> declaredScripts;
    List<AssetMetaData> stylesheets;
    List<AssetMetaData> declaredStylesheets;
    String assetsPath;
    if (config != null) {
      String packageName = config.getString("package");
      AssetLocation location = AssetLocation.safeValueOf(config.getString("location"));
      if (location == null) {
        location = AssetLocation.APPLICATION;
      }
      scripts = load(packageName, location, config.getList("scripts", JSON.class));
      declaredScripts = load(packageName, location, config.getList("declaredScripts", JSON.class));
      stylesheets = load(packageName, location, config.getList("stylesheets", JSON.class));
      declaredStylesheets = load(packageName, location, config.getList("declaredStylesheets", JSON.class));
      assetsPath = "/" + Name.parse(application.getPackageName()).append(packageName).toString().replace('.', '/') + "/";
    } else {
      scripts = Collections.emptyList();
      declaredScripts = Collections.emptyList();
      stylesheets = Collections.emptyList();
      declaredStylesheets = Collections.emptyList();
      assetsPath = null;
    }
    this.descriptor = new AssetDescriptor(scripts, declaredScripts, stylesheets, declaredStylesheets);
    this.context = context;
    this.assetsPath = assetsPath;
    return descriptor;
  }

  private List<AssetMetaData> load(
      String packageName,
      AssetLocation defaultLocation,
      List<? extends JSON> scripts) throws Exception {
    List<AssetMetaData> abc = Collections.emptyList();
    if (scripts != null && scripts.size() > 0) {
      abc = new ArrayList<AssetMetaData>();
      for (JSON script : scripts) {
        String id = script.getString("id");
        AssetLocation location = AssetLocation.safeValueOf(script.getString("location"));

        // We handle here location / perhaps we could handle it at compile time instead?
        if (location == null) {
          location = defaultLocation;
        }

        //
        String value = script.getString("src");
        if (!value.startsWith("/") && location == AssetLocation.APPLICATION) {
          value = "/" + application.getPackageName().replace('.', '/') + "/" + packageName.replace('.', '/') + "/" + value;
        }

        //
        AssetMetaData descriptor = new AssetMetaData(
          id,
          location,
          value,
          script.getArray("depends", String.class)
        );
        abc.add(descriptor);
      }
    }
    return abc;
  }

  @PostConstruct
  public void start() throws Exception {
    this.scripts = process(descriptor.getScripts(), scriptManager);
    this.declaredScripts = process(descriptor.getDeclaredScripts(), scriptManager);
    this.stylesheets = process(descriptor.getStylesheets(), stylesheetManager);
    this.declaredStylesheets = process(descriptor.getDeclaredStylesheets(), stylesheetManager);
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

  private String[] process(List<AssetMetaData> data, AssetManager manager) throws Exception {
    ArrayList<String> assets = new ArrayList<String>();
    for (AssetMetaData script : data) {

      // Validate assets
      AssetLocation location = script.getLocation();
      URL url;
      if (location == AssetLocation.APPLICATION) {
        url = resolve(AssetLocation.APPLICATION, script.getValue());
        if (url == null) {
          throw new Exception("Could not resolve application  " + script.getValue());
        }
      } else if (location == AssetLocation.SERVER) {
        if (!script.getValue().startsWith("/")) {
          url = resolve(AssetLocation.SERVER, "/" + script.getValue());
          if (url == null) {
            throw new Exception("Could not resolve server asset " + script.getValue());
          }
        } else {
          url = null;
        }
      } else {
        url = null;
      }

      //
      String id = manager.addAsset(script, url);
      assets.add(id);
    }

    //
    return assets.toArray(new String[assets.size()]);
  }

  public void invoke(Request request) {
    request.invoke();

    //
    if (request.getPhase() == Phase.VIEW) {
      Response response = request.getResponse();
      if (response instanceof Response.Content && (scripts.length > 0 || stylesheets.length > 0)) {
        Response.Content render = (Response.Content)response;

        // Add assets
        PropertyMap properties = new PropertyMap(render.getProperties());
        properties.addValues(PropertyType.STYLESHEET, stylesheets);
        properties.addValues(PropertyType.SCRIPT, scripts);

        // Use a new response
        request.setResponse(new Response.Content(render.getCode(), properties, render.getStreamable()));
      }
    }
  }
}
