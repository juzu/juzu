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

package org.juzu.request;

import org.juzu.Response;
import org.juzu.URLBuilder;
import org.juzu.impl.spi.request.MimeBridge;
import org.juzu.metadata.ApplicationDescriptor;
import org.juzu.metadata.ControllerMethod;
import org.juzu.metadata.ControllerParameter;
import org.juzu.text.Printer;
import org.juzu.text.WriterPrinter;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class MimeContext extends RequestContext
{

   /** . */
   private final ApplicationContext application;

   /** The response. */
   private Response.Mime response;
   
   protected MimeContext(ApplicationContext application, ControllerMethod method, ClassLoader classLoader)
   {
      super(method, classLoader);
      
      //
      this.application = application;
   }

   @Override
   protected abstract MimeBridge getBridge();

   public URLBuilder createURLBuilder(ControllerMethod method)
   {
      URLBuilder builder = getBridge().createURLBuilder(method);

      // Bridge escape XML value
      ApplicationDescriptor desc = application.getDescriptor();
      builder.escapeXML(desc.getEscapeXML());

      //
      return builder;
   }

   private void setValue(URLBuilder builder, ControllerParameter param, Object value)
   {
      switch (param.getCardinality())
      {
         case SINGLE:
         {
            builder.setParameter(param.getName(), String.valueOf(value));
            break;
         }
         case ARRAY:
         {
            int length = Array.getLength(value);
            String[] array = new String[length];
            for (int i = 0;i < length;i++)
            {
               Object component = Array.get(value, i);
               array[i] = String.valueOf(component);
            }
            builder.setParameter(param.getName(), array);
            break;
         }
         case LIST:
         {
            Collection<?> c = (Collection<?>)value;
            int length = c.size();
            String[] array = new String[length];
            Iterator<?> iterator = c.iterator();
            for (int i = 0;i < length;i++)
            {
               Object element = iterator.next();
               array[i] = String.valueOf(element);
            }
            builder.setParameter(param.getName(), array);
            break;
         }
         default:
            throw new UnsupportedOperationException("Not yet implemented");
      }
   }
   
   public URLBuilder createURLBuilder(ControllerMethod method, Object arg)
   {
      URLBuilder builder = createURLBuilder(method);

      //
      ControllerParameter param = method.getArgumentParameters().get(0);
      if (arg != null)
      {
         setValue(builder, param, arg);
      }

      //
      return builder;
   }

   public URLBuilder createURLBuilder(ControllerMethod method, Object[] args)
   {
      URLBuilder builder = createURLBuilder(method);

      // Fill in argument parameters
      for (int i = 0;i < args.length;i++)
      {
         Object value = args[i];
         if (value != null)
         {
            setValue(builder, method.getArgumentParameters().get(i), value);
         }
      }
      return builder;
   }

   /**
    * Returns the current printer.
    *
    * @return the printer
    */
   public Printer getPrinter()
   {
      return getBridge().getPrinter();
   }

   @Override
   public Response.Mime getResponse()
   {
      return response;
   }

   public void setResponse(Response.Mime response) throws IOException, IllegalStateException
   {
      // Consume response here
      StringBuilder buffer = new StringBuilder();
      WriterPrinter printer = new WriterPrinter(buffer);
      response.send(printer);
      if (response instanceof Response.Mime.Render)
      {
         response = Response.ok(((Response.Mime.Render)response).getTitle(), buffer.toString());
      }
      else
      {
         response = Response.status(((Response.Mime.Resource)response).getStatus(), buffer.toString());
      }
      
      //
      this.response = response;
   }
}
