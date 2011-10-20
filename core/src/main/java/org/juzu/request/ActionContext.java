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

import org.juzu.Phase;
import org.juzu.Response;
import org.juzu.metadata.ControllerMethod;
import org.juzu.metadata.ControllerParameter;
import org.juzu.impl.request.ActionBridge;

import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ActionContext extends RequestContext
{

   /** . */
   private ActionBridge bridge;

   protected ActionContext()
   {
   }

   public ActionContext(ClassLoader classLoader, ActionBridge bridge)
   {
      super(classLoader);

      //
      this.bridge = bridge;
   }

   @Override
   protected ActionBridge getBridge()
   {
      return bridge;
   }

   @Override
   public Phase getPhase()
   {
      return Phase.ACTION;
   }

   private void map(Response response, ControllerMethod method)
   {
      response.setParameter("op", method.getId());
   }

   private Response createResponse()
   {
      return bridge.createResponse();
   }

   public Response createResponse(ControllerMethod method)
   {
      Response response = createResponse();
      map(response, method);
      return response;
   }

   public Response createResponse(ControllerMethod method, Object arg)
   {
      Response response = createResponse();
      map(response, method);
      List<ControllerParameter> argumentParameters = method.getArgumentParameters();
      if (arg != null)
      {
         response.setParameter(argumentParameters.get(0).getName(), arg.toString());
      }
      return response;
   }

   public Response createResponse(ControllerMethod method, Object[] args)
   {
      Response response = createResponse();
      map(response, method);
      List<ControllerParameter> argumentParameters = method.getArgumentParameters();
      for (int i = 0;i < argumentParameters.size();i++)
      {
         Object value = args[i];
         if (value != null)
         {
            ControllerParameter argParameter = argumentParameters.get(i);
            response.setParameter(argParameter.getName(), value.toString());
         }
      }
      return response;
   }
}
