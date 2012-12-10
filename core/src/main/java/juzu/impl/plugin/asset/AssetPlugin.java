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
import juzu.asset.Asset;
import juzu.asset.AssetLocation;
import juzu.impl.metadata.Descriptor;
import juzu.impl.plugin.application.ApplicationException;
import juzu.impl.asset.AssetManager;
import juzu.impl.asset.AssetMetaData;
import juzu.impl.plugin.application.ApplicationPlugin;
import juzu.impl.plugin.application.descriptor.ApplicationDescriptor;
import juzu.impl.request.Request;
import juzu.impl.request.RequestFilter;
import juzu.impl.common.JSON;
import juzu.impl.resource.ResourceResolver;
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
  private Asset[] scripts;

  /** . */
  private Asset[] stylesheets;

  /** . */
  private AssetDescriptor descriptor;

  /** . */
  @Inject
  @Named("juzu.asset_manager.script")
  AssetManager scriptManager;

  /** . */
  @Inject
  @Named("juzu.asset_manager.stylesheet")
  AssetManager stylesheetManager;

  /** . */
  @Inject
  @Named("juzu.resource_resolver.classpath")
  ResourceResolver classPathResolver;

  /** . */
  @Inject
  @Named("juzu.resource_resolver.server")
  ResourceResolver serverResolver;

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
  public Descriptor init(ApplicationDescriptor application, JSON config) throws Exception {
    if (config != null) {
      String packageName = config.getString("package");
      AssetLocation location = AssetLocation.safeValueOf(config.getString("location"));
      if (location == null) {
        location = AssetLocation.CLASSPATH;
      }
      List<AssetMetaData> scripts = load(packageName, location, config.getList("scripts", JSON.class));
      List<AssetMetaData> stylesheets = load(packageName, location, config.getList("stylesheets", JSON.class));
      return descriptor = new AssetDescriptor(scripts, stylesheets);
    } else {
      return descriptor = new AssetDescriptor(Collections.<AssetMetaData>emptyList(), Collections.<AssetMetaData>emptyList());
    }
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

  private Asset[] process(List<AssetMetaData> data, AssetManager manager) throws Exception {
    ArrayList<Asset> assets = new ArrayList<Asset>();
    for (AssetMetaData script : data) {
      String id = script.getId();
      if (id != null) {
        assets.add(Asset.ref(id));
      }
      else {
        assets.add(Asset.of(script.getLocation(), script.getValue()));
      }

      // Validate assets
      AssetLocation location = script.getLocation();
      URL url;
      if (location == AssetLocation.CLASSPATH) {
        url = classPathResolver.resolve(script.getValue());
        if (url == null) {
          throw new Exception("Could not resolve classpath assets " + script.getValue());
        }
      } else if (location == AssetLocation.SERVER && !script.getValue().startsWith("/")) {
        url = serverResolver.resolve("/" + script.getValue());
        if (url == null) {
          throw new Exception("Could not resolve server assets " + script.getValue());
        }
      } else {
        url = null;
      }

      //
      manager.addAsset(script, url);
    }
    return assets.toArray(new Asset[assets.size()]);
  }

  public void invoke(Request request) throws ApplicationException {
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
