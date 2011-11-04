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

package org.juzu.impl.processor;

import org.juzu.Phase;

import java.io.Serializable;
import java.util.ArrayList;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class MethodModel implements Serializable
{

   /** . */
   final ControllerModel controller;

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

   public MethodModel(ControllerModel controller, String id, Phase phase, String name, ArrayList<String> parameterTypes, ArrayList<String> parameterNames)
   {
      this.controller = controller;
      this.id = id;
      this.phase = phase;
      this.name = name;
      this.parameterTypes = parameterTypes;
      this.parameterNames = parameterNames;
   }
}
