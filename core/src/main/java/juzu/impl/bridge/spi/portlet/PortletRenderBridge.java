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

package juzu.impl.bridge.spi.portlet;

import juzu.PropertyMap;
import juzu.PropertyType;
import juzu.Response;
import juzu.impl.asset.Asset;
import juzu.impl.bridge.Bridge;
import juzu.impl.common.Formatting;
import juzu.impl.compiler.CompilationException;
import juzu.impl.inject.ScopedContext;
import juzu.impl.bridge.spi.RenderBridge;
import juzu.request.Phase;
import org.w3c.dom.Comment;
import org.w3c.dom.Element;

import javax.portlet.MimeResponse;
import javax.portlet.PortletConfig;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceURL;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
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
  public void invoke() throws Exception {
    try {
      bridge.refresh();
    }
    catch (CompilationException e) {
      StringWriter buffer = new StringWriter();
      PrintWriter printer = new PrintWriter(buffer);
      Formatting.renderErrors(printer, e.getErrors());
      setResponse(Response.error(buffer.toString()));
      purgeSession();
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
      if (stylesheetsProp != null) {
        Iterable<Asset> stylesheets = bridge.application.getStylesheetManager().resolveAssets(stylesheetsProp);
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
        Iterable<Asset> scripts = bridge.application.getScriptManager().resolveAssets(scriptsProp);
        for (Asset script : scripts) {
          String url = getAssetURL(script);
          Element elt = this.resp.createElement("script");
          elt.setAttribute("type", "text/javascript");
          elt.setAttribute("src", url);
          // This comment is needed for liferay to make the script pass the minifier
          // it forces to have a <script></script> tag
          String data = bridge.application.getName() + " script ";
          Comment comment = elt.getOwnerDocument().createComment(data);
          elt.appendChild(comment);
          resp.addProperty(MimeResponse.MARKUP_HEAD_ELEMENT, elt);
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
      case CLASSPATH:
        if (bridge.module.context.getRunMode().isStatic()) {
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

  @Override
  public void end() {
    super.end();

    //
    ScopedContext context = getFlashContext(false);
    if (context != null) {
      context.close();
    }
  }
}
