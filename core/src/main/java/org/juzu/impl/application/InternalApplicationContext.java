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

package org.juzu.impl.application;

import org.juzu.RequestLifeCycle;
import org.juzu.request.Phase;
import org.juzu.Response;
import org.juzu.impl.inject.Export;
import org.juzu.impl.inject.ScopeController;
import org.juzu.impl.spi.inject.InjectManager;
import org.juzu.impl.spi.request.ActionBridge;
import org.juzu.impl.spi.request.RenderBridge;
import org.juzu.impl.request.Request;
import org.juzu.impl.spi.request.RequestBridge;
import org.juzu.impl.spi.request.ResourceBridge;
import org.juzu.metadata.ApplicationDescriptor;
import org.juzu.metadata.ControllerMethod;
import org.juzu.metadata.ControllerParameter;
import org.juzu.request.ApplicationContext;
import org.juzu.request.RequestContext;
import org.juzu.impl.spi.template.TemplateStub;
import org.juzu.impl.utils.Spliterator;
import org.juzu.template.Template;
import org.juzu.template.TemplateRenderContext;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
@Export
@Singleton
public class InternalApplicationContext extends ApplicationContext
{

   public static RequestContext getCurrentRequest()
   {
      return current.get().getContext();
   }

   /** . */
   private final ApplicationDescriptor descriptor;

   /** . */
   final InjectManager manager;

   /** . */
   private final ControllerResolver controllerResolver;

   /** . */
   static final ThreadLocal<Request> current = new ThreadLocal<Request>();

   @Inject
   public InternalApplicationContext(InjectManager manager, ApplicationDescriptor descriptor)
   {
      this.descriptor = descriptor;
      this.manager = manager;
      this.controllerResolver = new ControllerResolver(descriptor);
   }

   public ApplicationDescriptor getDescriptor()
   {
      return descriptor;
   }

   public void invoke(RequestBridge bridge) throws ApplicationException
   {
      ClassLoader classLoader = manager.getClassLoader();

      //
      Phase phase;
      if (bridge instanceof RenderBridge)
      {
         phase = Phase.RENDER;
      }
      else if (bridge instanceof ActionBridge)
      {
         phase = Phase.ACTION;
      }
      else if (bridge instanceof ResourceBridge)
      {
         phase = Phase.RESOURCE;
      }
      else
      {
         throw new AssertionError();
      }
      ControllerMethod method = controllerResolver.resolve(phase, bridge.getMethodId());

      //
      Request request = new Request(this, method, classLoader, bridge);

      //
      ClassLoader oldCL = Thread.currentThread().getContextClassLoader();
      try
      {
         Thread.currentThread().setContextClassLoader(classLoader);
         current.set(request);
         ScopeController.begin(request);
         
         //

         Object ret = doInvoke(manager, request, method);
         
         //
         Response resp;
         if (ret instanceof Response)
         {
            // We should check that it matches....
            // btw we should try to enforce matching during compilation phase
            // @Action -> Response.Action
            // @View -> Response.Mime
            // as we can do it
            resp = (Response)ret;
         }
         else
         {
            resp = request.getContext().getResponse();
         }

         //
         if (resp != null)
         {
            try
            {
               bridge.setResponse(resp);
            }
            catch (IOException e)
            {
               throw new UnsupportedOperationException("handle me gracefully");
            }
         }
      }
      finally
      {
         current.set(null);
         ScopeController.end();
         Thread.currentThread().setContextClassLoader(oldCL);
      }
   }

   public Object resolveBean(String name) throws ApplicationException
   {
      return resolveBean(manager, name);
   }

   private <B, I> Object resolveBean(InjectManager<B, I> manager, String name) throws ApplicationException
   {
      B bean = manager.resolveBean(name);
      if (bean != null)
      {
         try
         {
            I cc = manager.create(bean);
            return manager.get(bean, cc);
         }
         catch (InvocationTargetException e)
         {
            throw new ApplicationException(e.getCause());
         }
      }
      else
      {
         return null;
      }
   }

   private <B, I> Object doInvoke(InjectManager<B, I> manager, Request request, ControllerMethod method) throws ApplicationException
   {
      RequestContext context = request.getContext();

      //
      if (method == null)
      {
         StringBuilder sb = new StringBuilder("handle me gracefully : no method could be resolved for " +
            "phase=" + context.getPhase() + " and parameters={");
         int index = 0;
         for (Map.Entry<String, String[]> entry : context.getParameters().entrySet())
         {
            if (index++ > 0)
            {
               sb.append(',');
            }
            sb.append(entry.getKey()).append('=').append(Arrays.asList(entry.getValue()));
         }
         sb.append("}");
         throw new UnsupportedOperationException(sb.toString());
      }
      else
      {
         Class<?> type = method.getType();
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
               if (o instanceof RequestLifeCycle)
               {
                  ((RequestLifeCycle)o).beginRequest(context);
               }
               
               // Invoke method on controller
               Object[] args = getArgs(context);
               try
               {
                  return method.getMethod().invoke(o, args);
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
                  if (o instanceof RequestLifeCycle)
                  {
                     try
                     {
                        ((RequestLifeCycle)o).endRequest(context);
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
                  manager.release(instance);
               }
            }
         }
         else
         {
            return null;
         }
      }
   }

   private Object[] getArgs(RequestContext context)
   {
      ControllerMethod method = context.getMethod();

      // Prepare method parameters
      List<ControllerParameter> params = method.getArgumentParameters();
      Object[] args = new Object[params.size()];
      for (int i = 0;i < args.length;i++)
      {
         ControllerParameter param = params.get(i);
         String[] values = context.getParameters().get(param.getName());
         if (values != null)
         {
            switch (param.getCardinality())
            {
               case SINGLE:
                  args[i] = (values.length > 0) ? values[0] : null;
                  break;
               case ARRAY:
                  args[i] = values.clone();
                  break;
               case LIST:
                  ArrayList<String> list = new ArrayList<String>(values.length);
                  for (String value : values)
                  {
                     list.add(value);
                  }
                  args[i] = list;
                  break;
               default:
                  throw new UnsupportedOperationException("Handle me gracefully");
            }
         }
      }

      //
      return args;
   }

   public TemplateStub resolveTemplateStub(String path)
   {
      try
      {
         StringBuilder id = new StringBuilder(descriptor.getTemplatesPackageName());
         String relativePath = path.substring(0, path.indexOf('.'));
         for (String name : Spliterator.split(relativePath, '/'))
         {
            if (id.length() > 0)
            {
               id.append('.');
            }
            id.append(name);
         }
         id.append("_");
         ClassLoader cl = manager.getClassLoader();
         Class<?> stubClass = cl.loadClass(id.toString());
         return(TemplateStub)stubClass.newInstance();
      }
      catch (Exception e)
      {
         throw new UnsupportedOperationException("handle me gracefully", e);
      }
   }

   @Override
   public TemplateRenderContext render(final Template template, final Map<String, ?> parameters, final Locale locale)
   {
      //
      TemplateStub stub = resolveTemplateStub(template.getPath());

      //
      ApplicationTemplateRenderContext context = new ApplicationTemplateRenderContext(
         InternalApplicationContext.this,
         stub,
         parameters,
         locale);
      
      //
      return context;
   }
}
