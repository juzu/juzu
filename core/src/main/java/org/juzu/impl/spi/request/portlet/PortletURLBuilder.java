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

import org.juzu.URLBuilder;

import javax.portlet.BaseURL;
import java.io.IOException;
import java.io.StringWriter;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class PortletURLBuilder implements URLBuilder
{

   /** . */
   private final BaseURL url;

   /** . */
   private Boolean escapeXML;

   PortletURLBuilder(BaseURL url)
   {
      this.url = url;
      this.escapeXML = null;
   }

   public URLBuilder setParameter(String name, String value)
   {
      if (name == null)
      {
         throw new NullPointerException("No null name accepted");
      }
      url.setParameter(name, value);
      return this;
   }

   public URLBuilder setParameter(String name, String[] value) throws NullPointerException
   {
      if (name == null)
      {
         throw new NullPointerException("No null name accepted");
      }
      url.setParameter(name, value);
      return this;
   }

   public URLBuilder escapeXML(Boolean escapeXML)
   {
      this.escapeXML = escapeXML;
      return this;
   }

   @Override
   public String toString()
   {
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
}
