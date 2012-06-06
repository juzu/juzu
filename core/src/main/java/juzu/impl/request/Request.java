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

package juzu.impl.request;

import juzu.Response;
import juzu.Scope;
import juzu.impl.application.ApplicationContext;
import juzu.impl.application.ApplicationException;
import juzu.impl.controller.descriptor.ControllerMethod;
import juzu.impl.inject.Scoped;
import juzu.impl.inject.ScopingContext;
import juzu.impl.spi.inject.InjectManager;
import juzu.impl.spi.request.ActionBridge;
import juzu.impl.spi.request.RenderBridge;
import juzu.impl.spi.request.RequestBridge;
import juzu.impl.spi.request.ResourceBridge;
import juzu.request.ActionContext;
import juzu.request.RenderContext;
import juzu.request.RequestContext;
import juzu.request.ResourceContext;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class Request implements ScopingContext
{

   public static Request getCurrent()
   {
      return current.get();
   }

   /** . */
   private static final ThreadLocal<Request> current = new ThreadLocal<Request>();

   /** . */
   private final ApplicationContext application;
   
   /** . */
   private final RequestBridge bridge;

   /** . */
   private final RequestContext context;

   /** . */
   private final Map<String, String[]> parameters;

   /** . */
   private final Object[] args;

   /** The response. */
   private Response response;

   public Request(
      ApplicationContext application,
      ControllerMethod method,
      Map<String, String[]> parameters,
      Object[] args, 
      RequestBridge bridge)
   {
      RequestContext context;
      if (bridge instanceof RenderBridge)
      {
         context = new RenderContext(this, application, method, (RenderBridge)bridge);
      }
      else if (bridge instanceof ActionBridge)
      {
         context = new ActionContext(this, application, method, (ActionBridge)bridge);
      }
      else
      {
         context = new ResourceContext(this, application, method, (ResourceBridge)bridge);
      }

      //
      this.context = context;
      this.bridge = bridge;
      this.args = args;
      this.parameters = parameters;
      this.application = application;
   }

   public ApplicationContext getApplication()
   {
      return application;
   }

   public RequestBridge getBridge()
   {
      return bridge;
   }

   public Response getResponse()
   {
      return response;
   }

   public void setResponse(Response response)
   {
      this.response = response;
   }

   public Object[] getArgs()
   {
      return args;
   }

   public Map<String, String[]> getParameters()
   {
      return parameters;
   }

   public RequestContext getContext()
   {
      return context;
   }

   public final Scoped getContextualValue(Scope scope, Object key)
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

   public final void setContextualValue(Scope scope, Object key, Scoped value)
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
      switch (scope)
      {
         case IDENTITY:
            return false;
         default:
            return true;
      }
   }

   /** . */
   private int index = 0;

   public void invoke() throws ApplicationException
   {
      boolean set = current.get() == null;
      try
      {
         if (set)
         {
            current.set(this);
         }

         if (index >= 0 && index < application.getPlugins().size())
         {
            RequestLifeCycle plugin = application.getPlugins().get(index);
            try
            {
               index++;
               plugin.invoke(this);
            }
            finally
            {
               index--;
            }
         }
         else if (index == application.getPlugins().size())
         {
            //
            Object ret = doInvoke(this, args, application.getInjectManager());
   
            //
            if (ret instanceof Response)
            {
               // We should check that it matches....
               // btw we should try to enforce matching during compilation phase
               // @Action -> Response.Action
               // @View -> Response.Mime
               // as we can do it
               response = (Response)ret;
            }
         }
         else
         {
            throw new AssertionError();
         }
      }
      finally
      {
         if (set)
         {
            current.set(null);
         }
      }
   }

   private static <B, I> Object doInvoke(Request request, Object[] args, InjectManager<B, I> manager) throws ApplicationException
   {
      RequestContext context = request.getContext();
      Class<?> type = context.getMethod().getType();
      B bean = manager.resolveBean(type);

      if (bean != null)
      {
         I instance = null;
         try
         {
            Object o;
            try
            {
               // Get the bean
               instance = manager.create(bean);

               // Get a reference
               o = manager.get(bean, instance);
            }
            catch (InvocationTargetException e)
            {
               throw new ApplicationException(e.getCause());
            }

            // Begin request callback
            if (o instanceof juzu.request.RequestLifeCycle)
            {
               ((juzu.request.RequestLifeCycle)o).beginRequest(context);
            }

            // Invoke method on controller
            try
            {
               return context.getMethod().getMethod().invoke(o, args);
            }
            catch (InvocationTargetException e)
            {
               throw new ApplicationException(e.getCause());
            }
            catch (IllegalAccessException e)
            {
               throw new UnsupportedOperationException("hanle me gracefully", e);
            }
            finally
            {
               if (o instanceof juzu.request.RequestLifeCycle)
               {
                  try
                  {
                     ((juzu.request.RequestLifeCycle)o).endRequest(context);
                  }
                  catch (Exception e)
                  {
                     // Log me
                  }
               }
            }
         }
         finally
         {
            if (instance != null)
            {
               manager.release(bean, instance);
            }
         }
      }
      else
      {
         return null;
      }
   }
}
