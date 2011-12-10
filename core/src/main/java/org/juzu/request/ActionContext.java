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

import org.juzu.Response;
import org.juzu.impl.spi.request.ActionBridge;
import org.juzu.metadata.ControllerMethod;
import org.juzu.metadata.ControllerParameter;

import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ActionContext extends RequestContext
{

   /** . */
   private ActionBridge bridge;

   /** . */
   private boolean sent;

   protected ActionContext()
   {
   }

   public ActionContext(ControllerMethod method, ClassLoader classLoader, ActionBridge bridge)
   {
      super(method, classLoader);

      //
      this.bridge = bridge;
      this.sent = false;
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

   public Response.Redirect redirect(String location)
   {
      return bridge.redirect(location);
   }

   public boolean isSent()
   {
      return sent;
   }

   public Response.Render createResponse(ControllerMethod method) throws IllegalStateException
   {
      if (sent)
      {
         throw new IllegalStateException("Response already created");
      }
      Response.Render response = bridge.createResponse(method);
      sent = true;
      return response;
   }

   public Response.Render createResponse(ControllerMethod method, Object arg) throws IllegalStateException
   {
      Response.Render response = createResponse(method);
      List<ControllerParameter> argumentParameters = method.getArgumentParameters();
      if (arg != null)
      {
         response.setParameter(argumentParameters.get(0).getName(), arg.toString());
      }
      return response;
   }

   public Response.Render createResponse(ControllerMethod method, Object[] args) throws IllegalStateException
   {
      Response.Render response = createResponse(method);
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
