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

import org.juzu.URLBuilder;
import org.juzu.impl.spi.request.MimeBridge;
import org.juzu.metadata.ApplicationDescriptor;
import org.juzu.metadata.ControllerMethod;
import org.juzu.metadata.ControllerParameter;
import org.juzu.text.Printer;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class MimeContext extends RequestContext
{

   /** . */
   private final ApplicationContext application; 
   
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

   public URLBuilder createURLBuilder(ControllerMethod method, Object arg)
   {
      URLBuilder builder = createURLBuilder(method);

      //
      ControllerParameter param = method.getArgumentParameters().get(0);
      if (arg != null)
      {
         builder.setParameter(param.getName(), String.valueOf(arg));
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
            builder.setParameter(method.getArgumentParameters().get(i).getName(), String.valueOf(value));
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
}
