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

import juzu.PropertyMap;
import juzu.PropertyType;
import juzu.Response;
import juzu.asset.AssetLocation;
import juzu.impl.metadata.Descriptor;
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
  private String[] stylesheets;

  /** . */
  private AssetDescriptor descriptor;

  /** . */
  private PluginContext context;

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

  @Override
  public Descriptor init(PluginContext context) throws Exception {
    JSON config = context.getConfig();
    AssetDescriptor descriptor;
    if (config != null) {
      String packageName = config.getString("package");
      AssetLocation location = AssetLocation.safeValueOf(config.getString("location"));
      if (location == null) {
        location = AssetLocation.CLASSPATH;
      }
      List<AssetMetaData> scripts = load(packageName, location, config.getList("scripts", JSON.class));
      List<AssetMetaData> stylesheets = load(packageName, location, config.getList("stylesheets", JSON.class));
      descriptor = new AssetDescriptor(scripts, stylesheets);
    } else {
      descriptor = new AssetDescriptor(Collections.<AssetMetaData>emptyList(), Collections.<AssetMetaData>emptyList());
    }
    this.descriptor = descriptor;
    this.context = context;
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
        if (!value.startsWith("/") && location == AssetLocation.CLASSPATH) {
          value = "/" + packageName.replace('.', '/') + "/" + value;
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
    this.stylesheets = process(descriptor.getStylesheets(), stylesheetManager);
  }

  private String[] process(List<AssetMetaData> data, AssetManager manager) throws Exception {
    ArrayList<String> assets = new ArrayList<String>();
    for (AssetMetaData script : data) {

      // Validate assets
      AssetLocation location = script.getLocation();
      URL url;
      if (location == AssetLocation.CLASSPATH) {
        url = context.getApplicationResolver().resolve(script.getValue());
        if (url == null) {
          throw new Exception("Could not resolve classpath assets " + script.getValue());
        }
      } else if (location == AssetLocation.SERVER) {
        if (!script.getValue().startsWith("/")) {
          url = context.getServerResolver().resolve("/" + script.getValue());
          if (url == null) {
            throw new Exception("Could not resolve server assets " + script.getValue());
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
    if (request.getContext().getPhase() == Phase.VIEW) {
      Response response = request.getResponse();
      if (response instanceof Response.Render && (scripts.length > 0 || stylesheets.length > 0)) {
        Response.Render render = (Response.Render)response;

        // Add assets
        PropertyMap properties = new PropertyMap(render.getProperties());
        properties.addValues(PropertyType.STYLESHEET, stylesheets);
        properties.addValues(PropertyType.SCRIPT, scripts);

        // Use a new response
        request.setResponse(new Response.Render(properties, render.getStreamable()));
      }
    }
  }
}
