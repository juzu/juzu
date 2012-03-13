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

package org.juzu.impl.spi.request.portlet;

import org.juzu.Response;
import org.juzu.request.Phase;
import org.juzu.impl.spi.request.MimeBridge;
import org.juzu.io.BinaryOutputStream;
import org.juzu.io.BinaryStream;
import org.juzu.io.CharStream;
import org.juzu.io.AppendableStream;

import javax.portlet.BaseURL;
import javax.portlet.MimeResponse;
import javax.portlet.PortletRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
abstract class PortletMimeBridge<Rq extends PortletRequest, Rs extends MimeResponse> extends PortletRequestBridge<Rq, Rs> implements MimeBridge
{

   /** . */
   private String mimeType;
   
   /** . */
   private Object result;

   /** . */
   private final boolean buffer;

   PortletMimeBridge(Rq request, Rs response, boolean buffer) throws IOException
   {
      super(request, response);

      //
      this.buffer = buffer;
   }
   
   public void commit() throws IOException
   {
      if (result != null)
      {
         if (mimeType != null)
         {
            response.setContentType(mimeType);
         }
         if (result instanceof String)
         {
            response.getWriter().write((String)result);
         }
         else 
         {
            response.getPortletOutputStream().write((byte[])result);
         }
      }
   }

   public String renderURL(Phase phase, Boolean escapeXML, Map<String, String[]> parameters)
   {
      BaseURL url;
      switch (phase)
      {
         case ACTION:
            url = response.createActionURL();
            break;
         case RENDER:
            url = response.createRenderURL();
            break;
         case RESOURCE:
            url = response.createResourceURL();
            break;
         default:
            throw new AssertionError("Unexpected phase " + phase);
      }

      //
      url.setParameters(parameters);

      //
      if (escapeXML != null && escapeXML)
      {
         try
         {
            StringWriter writer = new StringWriter();
            url.write(writer, true);
            return writer.toString();
         }
         catch (IOException ignore)
         {
            // This should not happen
            return "";
         }
      }
      else
      {
         return url.toString();
      }
   }

   public void setResponse(Response response) throws IllegalStateException, IOException
   {
      Response.Content content = (Response.Content)response;
      
      //
      String mimeType = content.getMimeType();
      if (mimeType != null)
      {
         if (buffer)
         {
            this.mimeType = mimeType;
         }
         else
         {
            this.response.setContentType(mimeType);
         }
      }
      
      // Send content
      if (content.getKind() == CharStream.class)
      {
         CharStream stream;
         if (buffer)
         {
            StringBuilder sb = new StringBuilder();
            stream = new AppendableStream(sb);
            ((Response.Content<CharStream>)response).send(stream);
            result = sb.toString();
         }
         else
         {
            ((Response.Content<CharStream>)response).send(new AppendableStream(this.response.getWriter()));
         }
      }
      else
      {
         BinaryStream stream;
         if (buffer)
         {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            stream = new BinaryOutputStream(baos);
            ((Response.Content<BinaryStream>)response).send(stream);
            result = baos.toByteArray();
         }
         else
         {
            ((Response.Content<BinaryStream>)response).send(new BinaryOutputStream(this.response.getPortletOutputStream()));
         }
      }
   }
}
