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

package org.juzu.impl.request;

import org.juzu.impl.inject.ScopingContext;
import org.juzu.impl.spi.request.ActionBridge;
import org.juzu.impl.spi.request.RenderBridge;
import org.juzu.impl.spi.request.RequestBridge;
import org.juzu.impl.spi.request.ResourceBridge;
import org.juzu.metadata.ControllerMethod;
import org.juzu.request.ActionContext;
import org.juzu.request.ApplicationContext;
import org.juzu.request.RenderContext;
import org.juzu.request.RequestContext;
import org.juzu.request.ResourceContext;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class Request implements ScopingContext
{

   /** . */
   private final RequestBridge bridge;

   /** . */
   private final RequestContext context;

   /** . */
   private final Object[] args;

   public Request(ApplicationContext application, ControllerMethod method, Object[] args, RequestBridge bridge)
   {
      RequestContext context;
      if (bridge instanceof RenderBridge)
      {
         context = new RenderContext(application, method, (RenderBridge)bridge);
      }
      else if (bridge instanceof ActionBridge)
      {
         context = new ActionContext(application, method, (ActionBridge)bridge);
      }
      else
      {
         context = new ResourceContext(application, method, (ResourceBridge)bridge);
      }

      
      this.context = context;
      this.bridge = bridge;
      this.args = args;
   }

   public Object[] getArgs()
   {
      return args;
   }

   public RequestContext getContext()
   {
      return context;
   }

   public final Object getContextualValue(Scope scope, Object key)
   {
      switch (scope)
      {
         case FLASH:
            return bridge.getFlashValue(key);
         case REQUEST:
            return bridge.getRequestValue(key);
         case SESSION:
            return bridge.getSessionValue(key);
         case IDENTITY:
            return bridge.getIdentityValue(key);
         default:
            throw new AssertionError();
      }
   }

   public final void setContextualValue(Scope scope, Object key, Object value)
   {
      switch (scope)
      {
         case FLASH:
            bridge.setFlashValue(key, value);
            break;
         case REQUEST:
            bridge.setRequestValue(key, value);
            break;
         case SESSION:
            bridge.setSessionValue(key, value);
            break;
         case IDENTITY:
            bridge.setIdentityValue(key, value);
            break;
         default:
            throw new AssertionError();
      }
   }

   public boolean isActive(Scope scope)
   {
      return scope.isActive(this);
   }
}
