package juzu.impl.asset;

import juzu.impl.application.ApplicationRuntime;
import juzu.impl.utils.Tools;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class AssetServer {

  /** . */
  HashSet<ApplicationRuntime<?, ?, ?>> runtimes = new HashSet<ApplicationRuntime<?, ?, ?>>();

  public AssetServer() {
  }

  public void register(ApplicationRuntime<?, ?, ?> assetManager) {
    runtimes.add(assetManager);
  }

  public void unregister(ApplicationRuntime<?, ?, ?> assetManager) {
    runtimes.remove(assetManager);
  }

  public boolean doGet(String path, ServletContext ctx, HttpServletResponse resp) throws ServletException, IOException {
    if (path != null && path.length() > 0) {
      for (ApplicationRuntime<?, ?, ?> runtime : runtimes) {
        String contentType;
        InputStream in;
        if (runtime.getScriptManager().isClassPath(path)) {
          contentType = "text/javascript";
          in = runtime.getContext().getClassLoader().getResourceAsStream(path.substring(1));
        }
        else if (runtime.getStylesheetManager().isClassPath(path)) {
          contentType = "text/css";
          in = runtime.getContext().getClassLoader().getResourceAsStream(path.substring(1));
        }
        else {
          in = ctx.getResourceAsStream(path);
          if (in != null) {
            int pos = path.lastIndexOf('/');
            String name = pos == -1 ? path : path.substring(pos + 1);
            contentType = ctx.getMimeType(name);
          } else {
            contentType = null;
            in = null;
          }
        }
        if (in != null) {
          if (contentType != null) {
            resp.setContentType(contentType);
          }
          Tools.copy(in, resp.getOutputStream());
          return true;
        }
      }
    }
    return false;
  }
}
