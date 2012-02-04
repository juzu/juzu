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

package org.juzu;

import org.juzu.metadata.ControllerMethod;
import org.juzu.metadata.ControllerParameter;
import org.juzu.request.ActionContext;
import org.juzu.request.RenderContext;
import org.juzu.request.RequestContext;
import org.juzu.request.ResourceContext;

import java.util.ArrayList;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class Controller
{

   /** . */
   protected ActionContext actionContext;

   /** . */
   protected RenderContext renderContext;

   /** . */
   protected ResourceContext resourceContext;

   public void beginRequest(RequestContext context)
   {
      if (context instanceof ActionContext)
      {
         actionContext = (ActionContext)context;
      }
      else if (context instanceof RenderContext)
      {
         renderContext = (RenderContext)context;
      }
      else if (context instanceof ResourceContext)
      {
         resourceContext = (ResourceContext)context;
      }
   }
   
   public void endRequest()
   {
      this.actionContext = null;
      this.renderContext = null;
      this.resourceContext = null;
   }
}
