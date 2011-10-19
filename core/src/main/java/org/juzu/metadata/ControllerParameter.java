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

package org.juzu.metadata;

import org.juzu.impl.utils.Tools;

/**
 * A parameter of a controller
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class ControllerParameter
{

   /** . */
   private final String name;

   /** . */
   private final String value;

   public ControllerParameter(String name) throws NullPointerException
   {
      this(name, null);
   }

   public ControllerParameter(String name, String value) throws NullPointerException
   {
      if (name == null)
      {
         throw new NullPointerException("No null parameter name accepted");
      }

      //
      this.name = name;
      this.value = value;
   }

   /**
    * Returns the parameter name.
    *
    * @return the parameter name
    */
   public String getName()
   {
      return name;
   }

   /**
    * Returns the value matched by a controller parameter or null if the parameter can match any value.
    *
    * @return the parameter value
    */
   public String getValue()
   {
      return value;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (obj == this)
      {
         return true;
      }
      else if (obj instanceof ControllerParameter)
      {
         ControllerParameter that = (ControllerParameter)obj;
         return name.equals(that.name) && Tools.safeEquals(value, that.value);
      }
      else
      {
         return false;
      }
   }

   @Override
   public String toString()
   {
      return "ControllerParameter[name=" + name + ",value=" + value + "]";
   }
}
