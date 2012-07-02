package juzu.impl.bridge.spi.portlet;

import juzu.impl.application.ApplicationContext;
import juzu.impl.asset.AssetManager;
import juzu.impl.bridge.Bridge;
import juzu.impl.utils.Logger;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class PortletBridgeContext {

  /** . */
  final Bridge bridge;

  public PortletBridgeContext(Bridge bridge) {
    this.bridge = bridge;
  }

  public ApplicationContext getApplication() {
    return bridge.runtime.getContext();
  }

  public AssetManager getAssetManager() {
    return bridge.runtime.getScriptManager();
  }

  public Logger getLog() {
    return bridge.runtime.getLogger();
  }

  public PortletActionBridge create(ActionRequest req, ActionResponse resp) {
    return new PortletActionBridge(this, req, resp, bridge.config.prod);
  }

  public PortletRenderBridge create(RenderRequest req, RenderResponse resp, boolean buffer) {
    return new PortletRenderBridge(this, req, resp, buffer, bridge.config.prod);
  }

  public PortletResourceBridge create(ResourceRequest req, ResourceResponse resp, boolean buffer) {
    return new PortletResourceBridge(this, req, resp, buffer, bridge.config.prod);
  }
}
