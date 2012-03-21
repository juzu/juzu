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

package org.juzu.impl.controller.descriptor;

import org.juzu.impl.utils.Cardinality;
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
   private final Cardinality cardinality;

   /** . */
   private final String value;

   /** . */
   private final Class type;

//   public ControllerParameter(String name) throws NullPointerException
//   {
//      this(name, Cardinality.SINGLE, null);
//   }

   public ControllerParameter(String name, Cardinality cardinality) throws NullPointerException
   {
      this(name, cardinality, null);
   }

   public ControllerParameter(String name, Cardinality cardinality, String value) throws NullPointerException
   {
      this(name, cardinality, value, null);
   }

   public ControllerParameter(String name, Cardinality cardinality, String value, Class type) throws NullPointerException
   {
      if (name == null)
      {
         throw new NullPointerException("No null parameter name accepted");
      }
      if (cardinality == null)
      {
         throw new NullPointerException("No null parameter cardinality accepted");
      }

      //
      this.name = name;
      this.cardinality = cardinality;
      this.value = value;
      this.type = type;
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
    * Returns the parameter cardinality.
    *
    * @return the parameter cardinality
    */
   public Cardinality getCardinality()
   {
      return cardinality;
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

   public Class getType()
   {
      return type;
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
