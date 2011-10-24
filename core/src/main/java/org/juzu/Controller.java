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

import java.lang.reflect.InvocationTargetException;
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

   private Object[] getArgs(RequestContext context)
   {
      ControllerMethod method = context.getMethod();

      // Prepare method parameters
      List<ControllerParameter> params = method.getArgumentParameters();
      Object[] args = new Object[params.size()];
      for (int i = 0;i < args.length;i++)
      {
         String[] values = context.getParameters().get(params.get(i).getName());
         args[i] = (values != null && values.length > 0) ? values[0] : null;
      }

      //
      return args;
   }

   public Response processAction(ActionContext context)
   {
      try
      {
         ControllerMethod method = context.getMethod();
         Object[] args = getArgs(context);
         actionContext = context;
         return (Response)method.getMethod().invoke(this, args);
      }
      catch (Exception e)
      {
         throw new UnsupportedOperationException("handle me gracefully", e);
      }
      finally
      {
         actionContext = null;
      }
   }

   public void render(RenderContext context)
   {
      try
      {
         ControllerMethod method = context.getMethod();
         Object[] args = getArgs(context);
         renderContext = context;
         method.getMethod().invoke(this, args);
      }
      catch (Exception e)
      {
         throw new UnsupportedOperationException("handle me gracefully", e);
      }
      finally
      {
         renderContext = null;
      }
   }

   public void serveResource(ResourceContext context)
   {
      try
      {
         ControllerMethod method = context.getMethod();
         Object[] args = getArgs(context);
         resourceContext = context;
         method.getMethod().invoke(this, args);
      }
      catch (Exception e)
      {
         throw new UnsupportedOperationException("handle me gracefully", e);
      }
      finally
      {
         resourceContext = null;
      }
   }
}
