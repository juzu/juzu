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
import juzu.asset.AssetType;
import juzu.impl.plugin.application.ApplicationException;
import juzu.impl.asset.AssetManager;
import juzu.impl.asset.AssetMetaData;
import juzu.impl.asset.Manager;
import juzu.impl.plugin.application.ApplicationPlugin;
import juzu.impl.request.Request;
import juzu.impl.request.RequestFilter;
import juzu.impl.common.JSON;
import juzu.request.Phase;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
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
  @Manager(AssetType.SCRIPT)
  AssetManager scriptManager;

  /** . */
  @Inject
  @Manager(AssetType.STYLESHEET)
  AssetManager stylesheetManager;

  public AssetPlugin() {
    super("asset");
  }

  @Override
  public AssetDescriptor init(ClassLoader loader, JSON config) throws Exception {
    String packageName = config.getString("package");
    List<AssetMetaData> scripts = load(packageName, config.getList("scripts", JSON.class));
    List<AssetMetaData> stylesheets = load(packageName, config.getList("stylesheets", JSON.class));
    return descriptor = new AssetDescriptor(packageName, scripts, stylesheets);
  }

  private List<AssetMetaData> load(String packageName, List<? extends JSON> scripts) {
    List<AssetMetaData> abc = Collections.emptyList();
    if (scripts != null && scripts.size() > 0) {
      abc = new ArrayList<AssetMetaData>();
      for (JSON script : scripts) {
        String id = script.getString("id");
        AssetLocation location = AssetLocation.safeValueOf(script.getString("location"));

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
  public void start() {
    ArrayList<Asset> scripts = new ArrayList<Asset>();
    for (AssetMetaData script : descriptor.getScripts()) {
      String id = script.getId();
      if (id != null) {
        scripts.add(Asset.ref(id));
      }
      else {
        scripts.add(Asset.uri(script.getLocation(), script.getValue()));
      }
      scriptManager.addAsset(script);
    }

    //
    ArrayList<Asset> stylesheets = new ArrayList<Asset>();
    for (AssetMetaData stylesheet : descriptor.getStylesheets()) {
      String id = stylesheet.getId();
      if (id != null) {
        stylesheets.add(Asset.ref(stylesheet.getId()));
      }
      else {
        stylesheets.add(Asset.uri(stylesheet.getLocation(), stylesheet.getValue()));
      }
      stylesheetManager.addAsset(stylesheet);
    }

    //
    this.scripts = scripts.toArray(new Asset[scripts.size()]);
    this.stylesheets = stylesheets.toArray(new Asset[stylesheets.size()]);
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
