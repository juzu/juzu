package juzu.impl.request.spi.servlet;

import juzu.impl.application.ApplicationContext;
import juzu.impl.asset.AssetManager;
import juzu.impl.utils.Logger;
import juzu.request.Phase;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ServletBridgeContext {

  /** . */
  final ApplicationContext application;

  /** . */
  final AssetManager scriptManager;

  /** . */
  final AssetManager stylesheetManager;

  /** . */
  final Logger log;

  public ServletBridgeContext(
    ApplicationContext application,
    AssetManager scriptManager,
    AssetManager stylesheetManager,
    Logger log) {
    this.application = application;
    this.scriptManager = scriptManager;
    this.stylesheetManager = stylesheetManager;
    this.log = log;
  }

  public ApplicationContext getApplication() {
    return application;
  }

  public AssetManager getScriptManager() {
    return scriptManager;
  }

  public AssetManager getStylesheetManager() {
    return stylesheetManager;
  }

  public Logger getLog() {
    return log;
  }

  public ServletRequestBridge create(HttpServletRequest req, HttpServletResponse resp) {
    Phase phase = Phase.RENDER;
    Map<String, String[]> parameters = new HashMap<String, String[]>();
    String methodId = null;
    for (Map.Entry<String, String[]> entry : ((Map<String, String[]>)req.getParameterMap()).entrySet()) {
      String name = entry.getKey();
      String[] value = entry.getValue();
      if (name.equals("juzu.phase")) {
        phase = Phase.valueOf(value[0]);
      }
      else if (name.equals("juzu.op")) {
        methodId = value[0];
      }
      else {
        parameters.put(name, value);
      }
    }

    //
    switch (phase) {
      case RENDER:
        return new ServletRenderBridge(this, req, resp, methodId, parameters);
      case ACTION:
        return new ServletActionBridge(this, req, resp, methodId, parameters);
      case RESOURCE:
        return new ServletResourceBridge(this, req, resp, methodId, parameters);
      default:
        throw new UnsupportedOperationException("todo");
    }
  }
}
