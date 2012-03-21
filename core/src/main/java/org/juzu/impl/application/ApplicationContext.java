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

import org.juzu.Param;
import org.juzu.impl.application.metadata.ApplicationDescriptor;
import org.juzu.impl.controller.descriptor.ControllerMethodResolver;
import org.juzu.impl.controller.descriptor.ControllerMethod;
import org.juzu.impl.controller.descriptor.ControllerParameter;
import org.juzu.impl.inject.Export;
import org.juzu.impl.inject.ScopeController;
import org.juzu.impl.request.Request;
import org.juzu.impl.request.RequestLifeCycle;
import org.juzu.impl.spi.inject.InjectManager;
import org.juzu.impl.spi.request.ActionBridge;
import org.juzu.impl.spi.request.RenderBridge;
import org.juzu.impl.spi.request.RequestBridge;
import org.juzu.impl.spi.request.ResourceBridge;
import org.juzu.impl.spi.template.TemplateStub;
import org.juzu.impl.utils.Spliterator;
import org.juzu.request.Phase;
import org.juzu.request.RequestContext;
import org.juzu.template.Template;
import org.juzu.template.TemplateRenderContext;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
@Export
@Singleton
public class ApplicationContext
{

   /** . */
   private final ApplicationDescriptor descriptor;

   /** . */
   final InjectManager injectManager;

   /** . */
   private final ControllerMethodResolver controllerResolver;

   /** . */
   public ArrayList<RequestLifeCycle> plugins;

   @Inject
   public ApplicationContext(InjectManager injectManager, ApplicationDescriptor descriptor) throws Exception
   {
      this.descriptor = descriptor;
      this.injectManager = injectManager;
      this.controllerResolver = new ControllerMethodResolver(descriptor.getController());
      this.plugins = getPlugins(injectManager);
   }

   private <B, I> ArrayList<RequestLifeCycle> getPlugins(InjectManager<B, I> manager) throws Exception
   {
      ArrayList<RequestLifeCycle> plugins = new ArrayList<RequestLifeCycle>();
      for (B pluginBean : manager.resolveBeans(RequestLifeCycle.class))
      {
         I pluginInstance = manager.create(pluginBean);
         RequestLifeCycle plugin = (RequestLifeCycle)manager.get(pluginBean, pluginInstance);
         plugins.add(plugin);
      }
      return plugins;
   }

   public List<RequestLifeCycle> getPlugins()
   {
      return plugins;
   }

   public ClassLoader getClassLoader()
   {
      return injectManager.getClassLoader();
   }

   public ApplicationDescriptor getDescriptor()
   {
      return descriptor;
   }

   public InjectManager getInjectManager()
   {
      return injectManager;
   }

   public void invoke(RequestBridge bridge) throws ApplicationException
   {
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

      //
      String methodId = bridge.getProperty(RequestContext.METHOD_ID);

      //
      Map<String, String[]> parameters = new HashMap<String, String[]>();
      for (Map.Entry<String, String[]> entry : bridge.getParameters().entrySet())
      {
         String name = entry.getKey();
         String[] value = entry.getValue();
         if (name.startsWith("juzu."))
         {
            if (name.equals("juzu.op"))
            {
               methodId = value[0];
            }
         }
         else
         {
            parameters.put(name, value);
         }
      }

      //
      ControllerMethod method;
      if (methodId == null)
      {
         method = controllerResolver.resolve(parameters.keySet());
      }
      else
      {
         method = controllerResolver.resolve(phase, methodId, parameters.keySet());
      }

      //
      if (method == null)
      {
         StringBuilder sb = new StringBuilder("handle me gracefully : no method could be resolved for " +
            "phase=" + phase + " and parameters={");
         int index = 0;
         for (Map.Entry<String, String[]> entry : bridge.getParameters().entrySet())
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

      //
      Object[] args = getArgs(method, parameters);
      Request request = new Request(this, method, parameters, args, bridge);

      //
      ClassLoader oldCL = Thread.currentThread().getContextClassLoader();
      try
      {
         ClassLoader classLoader = injectManager.getClassLoader();
         Thread.currentThread().setContextClassLoader(classLoader);
         ScopeController.begin(request);
         request.invoke();
      }
      finally
      {
         ScopeController.end();
         Thread.currentThread().setContextClassLoader(oldCL);
      }
   }

   public Object resolveBean(String name) throws ApplicationException
   {
      return resolveBean(injectManager, name);
   }

   private Object[] getArgs(ControllerMethod method, Map<String, String[]> parameterMap)
   {
      // Prepare method parameters
      List<ControllerParameter> params = method.getArguments();
      Class<?>[] paramsType = method.getMethod().getParameterTypes();
      Object[] args = new Object[params.size()];
      for (int i = 0;i < args.length;i++)
      {
         ControllerParameter param = params.get(i);
         Object[] values = null;
         if (paramsType[i].isAnnotationPresent(Param.class))
         {
            // build bean parameter
            Object o = null;
            try
            {
               o = createMappedBean(paramsType[i], param.getName(), parameterMap);
            }
            catch (Exception e)
            {
            }
            values = new Object[]{o};
         }
         else
         {
           values = parameterMap.get(param.getName());
         }
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
                  ArrayList<Object> list = new ArrayList<Object>(values.length);
                  for (Object value : values)
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

   private <T> T createMappedBean(Class<T> clazz, String beanName, Map<String, String[]> parameters) throws IllegalAccessException, InstantiationException
   {
      // Extract parameters
      Map<String, String[]> beanParams = new HashMap<String, String[]>();
      String prefix = beanName + ".";
      for (String key : parameters.keySet())
      {
         if (key.startsWith(prefix))
         {
            String paramName = key.substring(prefix.length());
            beanParams.put(paramName, parameters.get(key));
         }
      }
      
      // Build bean
      T bean = clazz.newInstance();
      for (String key : beanParams.keySet())
      {
         String[] value = beanParams.get(key);
         String setterName = "set" + key.substring(0, 1).toUpperCase() + key.substring(1);
         boolean success = callSetter(setterName, clazz, bean, value, String[].class);
         if (!success)
         {
            success = callSetter(setterName, clazz, bean, value[0], String.class);
         }
         if (!success)
         {
            success = callSetter(setterName, clazz, bean, Arrays.asList(value), List.class);
         }
         if (!success)
         {
            try
            {
               Field f = clazz.getField(key);
               if (String[].class.equals(f.getType()))
               {
                  f.set(bean, value);
               }
               else if (String.class.equals(f.getType()))
               {
                  f.set(bean, value[0]);
               }
               else if (List.class.equals(f.getType()))
               {
                  f.set(bean, Arrays.asList(value));
               }

            }
            catch (NoSuchFieldException e)
            {
            }
         }

      }
      return bean;
   }

   <T> boolean callSetter(String methodName, Class<T> clazz, T target, Object value, Class type)
   {
      try
      {
         Method m = clazz.getMethod(methodName, type);
         m.invoke(target, value);
         return true;
      }
      catch (Exception e) {
         return false;
      }
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

   public TemplateStub resolveTemplateStub(String path)
   {
      try
      {
         StringBuilder id = new StringBuilder(descriptor.getTemplates().getPackageName());
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
         ClassLoader cl = injectManager.getClassLoader();
         Class<?> stubClass = cl.loadClass(id.toString());
         return(TemplateStub)stubClass.newInstance();
      }
      catch (Exception e)
      {
         throw new UnsupportedOperationException("handle me gracefully", e);
      }
   }

   public TemplateRenderContext render(final Template template, final Map<String, ?> parameters, final Locale locale)
   {
      //
      TemplateStub stub = resolveTemplateStub(template.getPath());

      //
      ApplicationTemplateRenderContext context = new ApplicationTemplateRenderContext(
         ApplicationContext.this,
         stub,
         parameters,
         locale);

      //
      return context;
   }
}
