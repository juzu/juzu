package juzu.impl.spi.request.portlet;

import juzu.impl.application.ApplicationContext;
import juzu.impl.application.ApplicationRuntime;
import juzu.impl.asset.AssetManager;
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
  final ApplicationRuntime runtime;

  /** . */
  final AssetManager assetManager;

  /** . */
  final Logger log;

  /** . */
  final boolean prod;

  public PortletBridgeContext(ApplicationRuntime runtime, AssetManager assetManager, Logger log, boolean prod) {
    this.runtime = runtime;
    this.assetManager = assetManager;
    this.log = log;
    this.prod = prod;
  }

  public ApplicationContext getApplication() {
    return runtime.getContext();
  }

  public AssetManager getAssetManager() {
    return assetManager;
  }

  public Logger getLog() {
    return log;
  }

  public PortletActionBridge create(ActionRequest req, ActionResponse resp) {
    return new PortletActionBridge(this, req, resp, prod);
  }

  public PortletRenderBridge create(RenderRequest req, RenderResponse resp, boolean buffer) {
    return new PortletRenderBridge(this, req, resp, buffer, prod);
  }

  public PortletResourceBridge create(ResourceRequest req, ResourceResponse resp, boolean buffer) {
    return new PortletResourceBridge(this, req, resp, buffer, prod);
  }
}
