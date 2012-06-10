/*
 * Copyright (C) 2011 eXo Platform SAS.
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

package juzu.impl.request.spi.portlet;

import juzu.PropertyMap;
import juzu.PropertyType;
import juzu.Response;
import juzu.URLBuilder;
import juzu.impl.request.spi.MimeBridge;
import juzu.io.AppendableStream;
import juzu.io.BinaryOutputStream;
import juzu.io.Stream;
import juzu.portlet.JuzuPortlet;
import juzu.request.Phase;
import juzu.request.RequestContext;

import javax.portlet.BaseURL;
import javax.portlet.MimeResponse;
import javax.portlet.PortletMode;
import javax.portlet.PortletModeException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletURL;
import javax.portlet.WindowState;
import javax.portlet.WindowStateException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
abstract class PortletMimeBridge<Rq extends PortletRequest, Rs extends MimeResponse> extends PortletRequestBridge<Rq, Rs> implements MimeBridge {

  /** . */
  private String mimeType;

  /** . */
  private Object result;

  /** . */
  private final boolean buffer;

  PortletMimeBridge(PortletBridgeContext context, Rq request, Rs response, boolean buffer, boolean prod) {
    super(context, request, response, prod);

    //
    this.buffer = buffer;
  }

  public void commit() throws IOException {
    if (result != null) {
      if (mimeType != null) {
        resp.setContentType(mimeType);
      }
      if (result instanceof String) {
        resp.getWriter().write((String)result);
      }
      else {
        resp.getPortletOutputStream().write((byte[])result);
      }
    }
  }

  public <T> String checkPropertyValidity(Phase phase, PropertyType<T> propertyType, T propertyValue) {
    if (propertyType == JuzuPortlet.PORTLET_MODE) {
      if (phase == Phase.RESOURCE) {
        return "Resource URL don't have portlet modes";
      }
      PortletMode portletMode = (PortletMode)propertyValue;
      for (Enumeration<PortletMode> e = req.getPortalContext().getSupportedPortletModes();e.hasMoreElements();) {
        PortletMode next = e.nextElement();
        if (next.equals(portletMode)) {
          return null;
        }
      }
      return "Unsupported portlet mode " + portletMode;
    }
    else if (propertyType == JuzuPortlet.WINDOW_STATE) {
      if (phase == Phase.RESOURCE) {
        return "Resource URL don't have windwo state";
      }
      WindowState windowState = (WindowState)propertyValue;
      for (Enumeration<WindowState> e = req.getPortalContext().getSupportedWindowStates();e.hasMoreElements();) {
        WindowState next = e.nextElement();
        if (next.equals(windowState)) {
          return null;
        }
      }
      return "Unsupported window state " + windowState;
    }
    else {
      // For now we ignore other properties
      return null;
    }
  }

  public String renderURL(Phase phase, Map<String, String[]> parameters, PropertyMap properties) {
    BaseURL url;
    switch (phase) {
      case ACTION:
        url = resp.createActionURL();
        break;
      case RENDER:
        url = resp.createRenderURL();
        break;
      case RESOURCE:
        url = resp.createResourceURL();
        break;
      default:
        throw new AssertionError("Unexpected phase " + phase);
    }

    // Set generic parameters
    url.setParameters(parameters);

    //
    boolean escapeXML = false;
    if (properties != null) {
      Boolean escapeXMLProperty = properties.getValue(URLBuilder.ESCAPE_XML);
      if (escapeXMLProperty != null && Boolean.TRUE.equals(escapeXMLProperty)) {
        escapeXML = true;
      }

      // Handle portlet mode
      PortletMode portletModeProperty = properties.getValue(JuzuPortlet.PORTLET_MODE);
      if (portletModeProperty != null) {
        if (url instanceof PortletURL) {
          try {
            ((PortletURL)url).setPortletMode(portletModeProperty);
          }
          catch (PortletModeException e) {
            throw new IllegalArgumentException(e);
          }
        }
        else {
          throw new IllegalArgumentException();
        }
      }

      // Handle window state
      WindowState windowStateProperty = properties.getValue(JuzuPortlet.WINDOW_STATE);
      if (windowStateProperty != null) {
        if (url instanceof PortletURL) {
          try {
            ((PortletURL)url).setWindowState(windowStateProperty);
          }
          catch (WindowStateException e) {
            throw new IllegalArgumentException(e);
          }
        }
        else {
          throw new IllegalArgumentException();
        }
      }

      // Set method id
      String methodId = properties.getValue(RequestContext.METHOD_ID);
      if (methodId != null) {
        url.setParameter("juzu.op", methodId);
      }
    }

    //
    if (escapeXML) {
      try {
        StringWriter writer = new StringWriter();
        url.write(writer, true);
        return writer.toString();
      }
      catch (IOException ignore) {
        // This should not happen
        return "";
      }
    }
    else {
      return url.toString();
    }
  }

  public void end(Response response) throws IllegalStateException, IOException {
    if (response instanceof Response.Content<?>) {
      Response.Content<?> content = (Response.Content<?>)response;

      //
      String mimeType = content.getMimeType();
      if (mimeType != null) {
        if (buffer) {
          this.mimeType = mimeType;
        }
        else {
          this.resp.setContentType(mimeType);
        }
      }

      // Send content
      if (content.getKind() == Stream.Char.class) {
        Stream.Char stream;
        if (buffer) {
          StringBuilder sb = new StringBuilder();
          stream = new AppendableStream(sb);
          ((Response.Content<Stream.Char>)response).send(stream);
          result = sb.toString();
        }
        else {
          ((Response.Content<Stream.Char>)response).send(new AppendableStream(this.resp.getWriter()));
        }
      }
      else {
        Stream.Binary stream;
        if (buffer) {
          ByteArrayOutputStream baos = new ByteArrayOutputStream();
          stream = new BinaryOutputStream(baos);
          ((Response.Content<Stream.Binary>)response).send(stream);
          result = baos.toByteArray();
        }
        else {
          ((Response.Content<Stream.Binary>)response).send(new BinaryOutputStream(this.resp.getPortletOutputStream()));
        }
      }
    }
  }
}
