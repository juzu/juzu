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
import juzu.impl.plugin.application.ApplicationContext;
import juzu.impl.bridge.spi.MimeBridge;
import juzu.impl.common.MethodHandle;
import juzu.impl.plugin.controller.descriptor.MethodDescriptor;
import juzu.io.AppendableStream;
import juzu.io.BinaryOutputStream;
import juzu.io.Stream;
import juzu.portlet.JuzuPortlet;
import juzu.request.Phase;

import javax.portlet.BaseURL;
import javax.portlet.MimeResponse;
import javax.portlet.PortletMode;
import javax.portlet.PortletModeException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletURL;
import javax.portlet.WindowState;
import javax.portlet.WindowStateException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
abstract class PortletMimeBridge<Rq extends PortletRequest, Rs extends MimeResponse> extends PortletRequestBridge<Rq, Rs> implements MimeBridge {

  /** . */
  private Response.Content<?> response;

  PortletMimeBridge(ApplicationContext application, Rq request, Rs response, boolean prod) {
    super(application, request, response, prod);
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

  public String renderURL(MethodHandle target, Map<String, String[]> parameters, PropertyMap properties) {

    //
    MethodDescriptor method = application.getDescriptor().getControllers().getMethodByHandle(target);

    //
    BaseURL url;
    switch (method.getPhase()) {
      case ACTION:
        url = resp.createActionURL();
        break;
      case VIEW:
        url = resp.createRenderURL();
        break;
      case RESOURCE:
        url = resp.createResourceURL();
        break;
      default:
        throw new AssertionError("Unexpected phase " + method.getPhase());
    }

    // Set generic parameters
    url.setParameters(parameters);

    //
    boolean escapeXML = false;
    if (properties != null) {
      Boolean escapeXMLProperty = properties.getValue(PropertyType.ESCAPE_XML);
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
      url.setParameter("juzu.op", method.getId());
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

  public void setResponse(Response response) throws IllegalStateException, IOException {
    super.setResponse(response);
    if (response instanceof Response.Content<?>) {
      this.response = (Response.Content<?>)response;
    } else {
      throw new IllegalArgumentException();
    }
  }

  @Override
  public void send() throws IOException {
    if (response != null) {
      //
      String mimeType = response.getMimeType();
      if (mimeType != null) {
        this.resp.setContentType(mimeType);
      }

      // Send content
      if (response.getKind() == Stream.Char.class) {
        ((Response.Content<Stream.Char>)response).send(new AppendableStream(this.resp.getWriter()));
      }
      else {
        ((Response.Content<Stream.Binary>)response).send(new BinaryOutputStream(this.resp.getPortletOutputStream()));
      }
    }
  }
}
