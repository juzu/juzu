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

package org.juzu.impl.model.meta.controller;

import org.juzu.Action;
import org.juzu.Resource;
import org.juzu.View;
import org.juzu.impl.model.meta.MetaModel;
import org.juzu.impl.model.meta.MetaModelObject;
import org.juzu.impl.utils.JSON;
import org.juzu.metadata.Cardinality;
import org.juzu.request.Phase;
import org.juzu.impl.compiler.ElementHandle;

import javax.lang.model.element.ExecutableElement;
import java.util.ArrayList;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class MethodMetaModel extends MetaModelObject
{

   /** The controller. */
   ControllerMetaModel controller;

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
   final ArrayList<Cardinality> parameterCardinalities;

   /** . */
   final ArrayList<String> parameterNames;

   MethodMetaModel(
      ElementHandle.Method handle,
      String id,
      Phase phase,
      String name,
      ArrayList<String> parameterTypes,
      ArrayList<Cardinality> parameterCardinalities,
      ArrayList<String> parameterNames)
   {
      this.handle = handle;
      this.id = id;
      this.phase = phase;
      this.name = name;
      this.parameterTypes = parameterTypes;
      this.parameterCardinalities = parameterCardinalities;
      this.parameterNames = parameterNames;
   }

   public JSON toJSON()
   {
      JSON json = new JSON();
      json.add("handle", handle);
      json.add("id", id);
      json.add("phase", phase);
      json.add("name", name);
      json.add("parameterTypes", new ArrayList<String>(parameterTypes));
      json.add("parameterNames", new ArrayList<String>(parameterNames));
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

   public ArrayList<Cardinality> getParameterCardinalities()
   {
      return parameterCardinalities;
   }

   public ArrayList<String> getParameterNames()
   {
      return parameterNames;
   }

   @Override
   protected void postAttach(MetaModelObject parent)
   {
      controller = (ControllerMetaModel)parent;
   }

   @Override
   public boolean exist(MetaModel model)
   {
      ExecutableElement methodElt = model.env.get(handle);
      return methodElt != null && (
         methodElt.getAnnotation(View.class) != null ||
         methodElt.getAnnotation(Action.class) != null ||
         methodElt.getAnnotation(Resource.class) != null);
   }
}
