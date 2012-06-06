package juzu.impl.plugin.asset;

import juzu.PropertyMap;
import juzu.Response;
import juzu.asset.Asset;
import juzu.asset.AssetType;
import juzu.impl.application.ApplicationException;
import juzu.impl.application.metadata.ApplicationDescriptor;
import juzu.impl.asset.AssetManager;
import juzu.impl.asset.AssetMetaData;
import juzu.impl.asset.Manager;
import juzu.impl.request.Request;
import juzu.impl.request.RequestLifeCycle;
import juzu.request.Phase;

import javax.inject.Inject;
import java.util.ArrayList;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class AssetLifeCycle extends RequestLifeCycle {

  /** . */
  private final Asset[] scripts;

  /** . */
  private final Asset[] stylesheets;

  @Inject
  public AssetLifeCycle(ApplicationDescriptor desc,
                        @Manager(AssetType.SCRIPT) AssetManager scriptManager,
                        @Manager(AssetType.STYLESHEET) AssetManager stylesheetManager) {
    AssetDescriptor descriptor = (AssetDescriptor)desc.getPlugin("asset");

    //
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

  @Override
  public void invoke(Request request) throws ApplicationException {
    request.invoke();

    //
    if (request.getContext().getPhase() == Phase.RENDER) {
      Response response = request.getResponse();
      if (response instanceof Response.Render && (scripts.length > 0 || stylesheets.length > 0)) {
        Response.Render render = (Response.Render)response;

        // Add assets
        PropertyMap properties = new PropertyMap(render.getProperties());
        properties.addValues(Response.Render.STYLESHEET, stylesheets);
        properties.addValues(Response.Render.SCRIPT, scripts);

        // Use a new response
        request.setResponse(new Response.Render(properties, render.getStreamable()));
      }
    }
  }
}
