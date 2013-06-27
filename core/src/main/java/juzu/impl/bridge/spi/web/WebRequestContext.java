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
package juzu.impl.bridge.spi.web;

import juzu.asset.AssetLocation;
import juzu.impl.asset.AssetManager;
import juzu.impl.compiler.CompilationException;
import juzu.impl.io.SafeStream;
import juzu.impl.plugin.amd.AMDPlugin;
import juzu.impl.plugin.asset.AssetPlugin;
import juzu.request.Result;
import juzu.request.RequestParameter;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

/** @author Julien Viet */
public abstract class WebRequestContext {

  public final void send(CompilationException e) throws IOException {
    send(e.result(), true);
  }

  public final void send(Result.Error error, boolean verbose) throws IOException {
    send(null, null, error.asStatus(verbose));
  }

  public final void send(AssetPlugin assetPlugin, AMDPlugin amdPlugin, Result.Status response) throws IOException {

    //
    AsyncStream stream = getStream(response.code);

    //
    if (response.decorated) {

      //
      AssetManager stylesheetManager;
      AssetManager scriptManager;
      AssetManager amdManager;
      if (assetPlugin != null) {
        stylesheetManager = assetPlugin.getStylesheetManager();
        scriptManager = assetPlugin.getScriptManager();
      } else {
        stylesheetManager = null;
        scriptManager = null;
      }
      
      if (amdPlugin != null) {
        amdManager = amdPlugin.getAMDManager();
      } else {
        amdManager = null;
      }

      //
      stream = new WebStream((HttpStream)stream, stylesheetManager, scriptManager, amdManager) {
        @Override
        public String renderAssetURL(AssetLocation location, String uri) {
          try {
            StringBuilder sb = new StringBuilder();
            WebRequestContext.this.renderAssetURL(location, uri, sb);
            return sb.toString();
          }
          catch (IOException e) {
            e.printStackTrace();
            throw new UnsupportedOperationException("handle me ", e);
          }
        }
      };
    }

    //
    try {
      response.streamable.send(new SafeStream(stream));
    } finally {
      stream.end();
    }
  }

  public abstract Map<String, RequestParameter> getParameters();

  public abstract String getRequestURI();

  public abstract String getPath();

  public abstract String getRequestPath();

  public abstract void setContentType(String mimeType, Charset charset);

  public abstract void setStatus(int status);

  public abstract void setHeaders(Iterable<Map.Entry<String, String[]>> headers);

  public abstract void sendRedirect(String location) throws IOException;

  public abstract HttpStream getStream(int status);

  public abstract void renderAssetURL(AssetLocation location, String uri, Appendable appendable) throws IOException;
}
