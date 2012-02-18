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

package org.juzu.impl.spi.request.servlet;

import org.juzu.Response;
import org.juzu.URLBuilder;
import org.juzu.metadata.ControllerMethod;
import org.juzu.request.Phase;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ServletURLBuilder implements URLBuilder
{

   /** . */
   private static final String[] EMPTY_STRING_ARRAY = new String[0];

   /** . */
   private final ServletRequestBridge bridge;
   
   /** . */
   private final String methodId;

   /** . */
   private final Phase phase;

   /** . */
   private Map<String, String[]> parameters;

   public ServletURLBuilder(ServletRequestBridge bridge, ControllerMethod method)
   {
      this.bridge = bridge;
      this.methodId = method.getId();
      this.phase = method.getPhase();
      this.parameters = new HashMap<String, String[]>();
   }

   public ServletURLBuilder(ServletRequestBridge bridge, Response.Update update)
   {
      Map<String, String[]> parameters = new HashMap<String, String[]>();
      for (Map.Entry<String, String> entry : update.getParameters().entrySet())
      {
         parameters.put(entry.getKey(), new String[]{entry.getValue()});
      }

      //
      this.bridge = bridge;
      this.phase = Phase.RENDER;
      this.methodId = update.getMethodId();
      this.parameters = parameters;
   }

   public URLBuilder setParameter(String name, String value) throws NullPointerException
   {
      return setParameter(name, value == null ? EMPTY_STRING_ARRAY : new String[]{value});
   }

   public URLBuilder setParameter(String name, String[] value) throws NullPointerException, IllegalArgumentException
   {
      if (name == null)
      {
         throw new NullPointerException();
      }
      if (value == null)
      {
         throw new NullPointerException();
      }
      if (value.length == 0)
      {
         parameters.remove(name);
      }
      else
      {
         for (int i = 0;i < value.length;i++)
         {
            if (value[i] == null)
            {
               throw new IllegalArgumentException("Argument array cannot contain null value");
            }
         }
         parameters.put(name, value.clone());
      }
      return this;
   }

   public URLBuilder escapeXML(Boolean escapeXML)
   {
      // Not implemented for now
      return this;  
   }

   // WARNING : this use java.net.URLEncoder and should be avoided for performance reason
   // for now it's OK.
   @Override
   public String toString()
   {
      StringBuilder buffer = new StringBuilder();
      HttpServletRequest req = bridge.req;
      buffer.append(req.getScheme());
      buffer.append("://");
      buffer.append(req.getServerName());
      int port = req.getServerPort();
      if (port != 80)
      {
         buffer.append(':').append(port);
      }
      buffer.append(req.getContextPath());
      buffer.append(req.getServletPath());
      buffer.append("?op=").append(methodId);
      buffer.append("&phase=").append(phase);
      for (Map.Entry<String, String[]> parameter : parameters.entrySet())
      {
         try
         {
            String encName = URLEncoder.encode(parameter.getKey(), "UTF-8");
            for (String value : parameter.getValue())
            {
               String encValue = URLEncoder.encode(value, "UTF-8");
               buffer.append("&p.").append(encName).append('=').append(encValue);
            }
         }
         catch (UnsupportedEncodingException e)
         {
            // Should not happen
            throw new AssertionError(e);
         }
      }
      return buffer.toString();
   }
}
