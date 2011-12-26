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

package org.juzu.test.request;

import org.juzu.URLBuilder;
import org.juzu.impl.utils.JSON;
import org.juzu.metadata.ControllerMethod;

import java.util.HashMap;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class MockURLBuilder implements URLBuilder
{

   /** . */
   private final ControllerMethod method;

   /** . */
   private final Map<String, String> parameters;

   /** . */
   private Boolean escapeXML;

   public MockURLBuilder(ControllerMethod method)
   {
      this.method = method;
      this.parameters = new HashMap<String, String>();
      this.escapeXML = null;
   }

   public URLBuilder setParameter(String name, String value)
   {
      if (name == null)
      {
         throw new NullPointerException("No null parameter accepted");
      }
      if (value == null)
      {
         parameters.remove(name);
      }
      else
      {
         parameters.put(name, value);
      }
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
      JSON url = new JSON();
      url.add("op", method.getId());
      url.add("parameters", parameters);
      if (escapeXML != null)
      {
         url.add("escapeXML", escapeXML);
      }
      return url.toString();
   }
}
