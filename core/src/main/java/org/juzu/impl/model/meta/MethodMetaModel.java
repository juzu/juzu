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

package org.juzu.impl.model.meta;

import org.juzu.Phase;
import org.juzu.impl.processor.ElementHandle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class MethodMetaModel extends MetaModelObject
{

   /** The controller. */
   final ControllerMetaModel controller;

   /** . */
   final ElementHandle.Method handle;

   /** . */
   final String id;

   /** . */
   final Phase phase;

   /** . */
   final String name;

   /** . */
   final ArrayList<String> parameterTypes;

   /** . */
   final ArrayList<String> parameterNames;

   MethodMetaModel(
      ElementHandle.Method handle,
      ControllerMetaModel controller,
      String id,
      Phase phase,
      String name,
      ArrayList<String> parameterTypes,
      ArrayList<String> parameterNames)
   {
      this.handle = handle;
      this.controller = controller;
      this.id = id;
      this.phase = phase;
      this.name = name;
      this.parameterTypes = parameterTypes;
      this.parameterNames = parameterNames;
   }

   public Map<String, ?> toJSON()
   {
      HashMap<String, Object> json = new HashMap<String, Object>();
      json.put("handle", handle);
      json.put("phase", phase);
      json.put("name", name);
      json.put("parameterTypes", new ArrayList<String>(parameterTypes));
      return json;
   }

   public ControllerMetaModel getController()
   {
      return controller;
   }

   public ElementHandle.Method getHandle()
   {
      return handle;
   }

   public String getId()
   {
      return id;
   }

   public Phase getPhase()
   {
      return phase;
   }

   public String getName()
   {
      return name;
   }

   public ArrayList<String> getParameterTypes()
   {
      return parameterTypes;
   }

   public ArrayList<String> getParameterNames()
   {
      return parameterNames;
   }
}
