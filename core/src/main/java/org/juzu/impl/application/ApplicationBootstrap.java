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

import org.juzu.impl.inject.Binding;
import org.juzu.impl.inject.Bindings;
import org.juzu.impl.inject.MetaProvider;
import org.juzu.impl.request.Scope;
import org.juzu.impl.spi.inject.InjectBootstrap;
import org.juzu.impl.spi.inject.InjectManager;
import org.juzu.metadata.ApplicationDescriptor;
import org.juzu.metadata.ControllerDescriptor;
import org.juzu.metadata.TemplateDescriptor;
import org.juzu.plugin.Plugin;
import org.juzu.request.ApplicationContext;
import org.juzu.template.Template;

import javax.inject.Provider;
import java.util.ArrayList;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ApplicationBootstrap
{

   /** . */
   public final InjectBootstrap bootstrap;

   /** . */
   public final ApplicationDescriptor descriptor;

   /** . */
   private InternalApplicationContext context;

   public ApplicationBootstrap(InjectBootstrap bootstrap, ApplicationDescriptor descriptor)
   {
      this.bootstrap = bootstrap;
      this.descriptor = descriptor;
   }

   public void start() throws Exception
   {
      _start();
   }
   
   private <B, I> void _start() throws Exception
   {
      // Bind the application descriptor
      bootstrap.bindBean(ApplicationDescriptor.class, descriptor);

      // Bind the application context
      bootstrap.declareBean(ApplicationContext.class, InternalApplicationContext.class);

      // Bind the scopes
      for (Scope scope : Scope.values())
      {
         bootstrap.addScope(scope);
      }

      // Bind the controllers
      for (ControllerDescriptor controller : descriptor.getControllers())
      {
         bootstrap.declareBean(controller.getType(), (Class)null);
      }

      // Bind the templates
      for (TemplateDescriptor template : descriptor.getTemplates())
      {
         bootstrap.declareBean(Template.class, template.getType());
      }

      // Use this instead of cached package
      Class<?> s = descriptor.getApplicationLoader().loadClass(descriptor.getPackageName() + ".package-info");

      // Bind the bean bindings
      Bindings bindings = s.getAnnotation(Bindings.class);
      if (bindings != null)
      {
         for (Binding binding : bindings.value())
         {
            Class<?> clazz = binding.value();
            Class<?> implementation = binding.implementation();

            //
            if (MetaProvider.class.isAssignableFrom(implementation))
            {
               MetaProvider mp = (MetaProvider)implementation.newInstance();
               Provider provider = mp.getProvider(clazz);
               bootstrap.bindProvider(clazz, provider);
            }
            else
            {
               if (implementation == Object.class)
               {
                  implementation = null;
               }
               bootstrap.declareBean((Class)clazz, (Class)implementation);
            }
         }
      }

      //
      List<Class<? extends Plugin>> plugins = descriptor.getPlugins();

      // Declare the plugins
      for (Class<? extends Plugin> pluginType : plugins)
      {
         bootstrap.declareBean(pluginType, null);
      }

      //
      InjectManager<B, I> manager = bootstrap.create();

      //
      B contextBean = manager.resolveBean(ApplicationContext.class);
      I contextInstance = manager.create(contextBean);
      
      // Get plugins
      ArrayList<Plugin> p = new ArrayList<Plugin>();
      for (Class<? extends Plugin> pluginType : plugins)
      {
         B pluginBean = manager.resolveBean(pluginType);
         I pluginInstance = manager.create(pluginBean);
         Object o = manager.get(pluginBean, pluginInstance);
         p.add((Plugin)o);
      }

      //
      this.context = (InternalApplicationContext)manager.get(contextBean, contextInstance);
   }
   
   public InternalApplicationContext getContext()
   {
      return context;
   }

   public void stop()
   {
      // container.stop();
   }
}
