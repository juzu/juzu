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

package juzu.impl.bridge.spi.portlet;

import juzu.PropertyMap;
import juzu.PropertyType;
import juzu.Response;
import juzu.impl.asset.Asset;
import juzu.impl.bridge.Bridge;
import juzu.impl.common.Formatting;
import juzu.impl.common.Tools;
import juzu.impl.compiler.CompilationException;
import juzu.impl.bridge.spi.RenderBridge;
import juzu.impl.plugin.asset.AssetPlugin;
import juzu.io.Stream;
import juzu.io.Streams;
import juzu.request.Phase;
import org.w3c.dom.Comment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.portlet.MimeResponse;
import javax.portlet.PortletConfig;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceURL;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class PortletRenderBridge extends PortletMimeBridge<RenderRequest, RenderResponse> implements RenderBridge {

  public PortletRenderBridge(Bridge bridge, RenderRequest request, RenderResponse response, PortletConfig config) {
    super(bridge, request, response, config);
  }

  @Override
  protected Phase getPhase() {
    return Phase.VIEW;
  }

  @Override
  public Stream createStream(String mimeType, Charset charset) throws IOException {
    if (mimeType != null) {
      this.resp.setContentType(mimeType);
    }

    // We use a writer during render phase as the developer may have set
    // a charset that is not the portlet container provided charset
    // and therefore it is safer to use the writer of the portlet container
    return Streams.closeable(charset, this.resp.getWriter());
  }

  @Override
  public void invoke() throws Exception {
    try {
      bridge.refresh();
    }
    catch (CompilationException e) {
      StringWriter buffer = new StringWriter();
      PrintWriter printer = new PrintWriter(buffer);
      Formatting.renderErrors(printer, e.getErrors());
      setResponse(Response.error(buffer.toString()));
      return;
    }

    //
    super.invoke();
  }

  @Override
  protected void sendProperties() throws IOException {
    if (response instanceof Response.Content) {
      Response.Content content = (Response.Content)response;

      // Http headers
      super.sendProperties();

      //
      PropertyMap properties = content.getProperties();

      //
      String title = properties.getValue(PropertyType.TITLE);
      if (title != null) {
        resp.setTitle(title);
      }

      //
      Iterable<String> scriptsProp = properties.getValues(PropertyType.SCRIPT);
      Iterable<String> stylesheetsProp = properties.getValues(PropertyType.STYLESHEET);
      Iterable<Map.Entry<String, String>> metas = properties.getValues(PropertyType.META_TAG);
      Iterable<Element> headers = properties.getValues(PropertyType.HEADER_TAG);

      //
      if (metas != null) {
        for (Map.Entry<String, String> entry : metas) {
          Element elt = this.resp.createElement("meta");
          elt.setAttribute("name", entry.getKey());
          elt.setAttribute("content", entry.getValue());
          resp.addProperty(MimeResponse.MARKUP_HEAD_ELEMENT, elt);
        }
      }

      //
      AssetPlugin assetPlugin = (AssetPlugin)bridge.getApplication().getPlugin("asset");

      //
      if (stylesheetsProp != null) {
        Iterable<Asset> stylesheets = assetPlugin.getStylesheetManager().resolveAssets(stylesheetsProp);
        for (Asset stylesheet : stylesheets) {
          int pos = stylesheet.getURI().lastIndexOf('.');
          String ext = pos == -1 ? "css" : stylesheet.getURI().substring(pos + 1);
          Element elt = this.resp.createElement("link");
          elt.setAttribute("media", "screen");
          elt.setAttribute("rel", "stylesheet");
          elt.setAttribute("type", "text/" + ext);
          elt.setAttribute("href", getAssetURL(stylesheet));
          resp.addProperty(MimeResponse.MARKUP_HEAD_ELEMENT, elt);
        }
      }

      //
      if (scriptsProp != null) {
        Iterable<Asset> scripts = assetPlugin.getScriptManager().resolveAssets(scriptsProp);
        for (Asset script : scripts) {
          String url = getAssetURL(script);
          Element elt = this.resp.createElement("script");
          elt.setAttribute("type", "text/javascript");
          elt.setAttribute("src", url);
          // This comment is needed for liferay to make the script pass the minifier
          // it forces to have a <script></script> tag
          String data = bridge.getApplication().getName() + " script ";
          Comment comment = elt.getOwnerDocument().createComment(data);
          elt.appendChild(comment);
          resp.addProperty(MimeResponse.MARKUP_HEAD_ELEMENT, elt);
        }
      }

      //
      if (headers != null) {
        for (Element header : headers) {
          Element elt = resp.createElement(header.getTagName());
          for (Node child : Tools.children(header)) {
            child = elt.getOwnerDocument().importNode(header, true);
            elt.appendChild(child);
          }
          resp.addProperty(MimeResponse.MARKUP_HEAD_ELEMENT, header);
        }
      }
    }
  }

  private String getAssetURL(Asset asset) {
    StringBuilder sb;
    String url;
    String uri = asset.getURI();
    switch (asset.getLocation()) {
      case SERVER:
        sb = new StringBuilder();
        if (!uri.startsWith("/")) {
          sb.append(req.getContextPath());
          sb.append('/');
        }
        sb.append(uri);
        url = sb.toString();
        break;
      case APPLICATION:
        if (bridge.getRunMode().isStatic()) {
          sb = new StringBuilder();
          sb.append(req.getContextPath()).append("/assets");
          if (!uri.startsWith("/")) {
            sb.append('/');
          }
          sb.append(uri);
          url = sb.toString();
        }
        else {
          ResourceURL r = resp.createResourceURL();
          r.setParameter("juzu.request", "assets");
          r.setResourceID(uri);
          url = r.toString();
        }
        break;
      case URL:
        url = uri;
        break;
      default:
        throw new AssertionError();
    }
    return url;
  }
}
