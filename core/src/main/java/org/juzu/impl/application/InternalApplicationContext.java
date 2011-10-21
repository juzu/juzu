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

import org.juzu.ActionScoped;
import org.juzu.AmbiguousResolutionException;
import org.juzu.Path;
import org.juzu.Phase;
import org.juzu.RenderScoped;
import org.juzu.ResourceScoped;
import org.juzu.Response;
import org.juzu.impl.spi.request.ActionBridge;
import org.juzu.impl.spi.request.RenderBridge;
import org.juzu.impl.request.Request;
import org.juzu.impl.spi.request.RequestBridge;
import org.juzu.impl.spi.request.ResourceBridge;
import org.juzu.metadata.ApplicationDescriptor;
import org.juzu.metadata.ControllerMethod;
import org.juzu.metadata.ControllerParameter;
import org.juzu.impl.cdi.Export;
import org.juzu.impl.cdi.ScopeController;
import org.juzu.request.ActionContext;
import org.juzu.request.ApplicationContext;
import org.juzu.request.MimeContext;
import org.juzu.request.RenderContext;
import org.juzu.request.RequestContext;
import org.juzu.impl.spi.cdi.Container;
import org.juzu.impl.spi.template.TemplateStub;
import org.juzu.impl.utils.Spliterator;
import org.juzu.request.ResourceContext;
import org.juzu.template.Template;
import org.juzu.text.Printer;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

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
   final Container container;

   /** . */
   private final ControllerResolver controllerResolver;

   /** . */
   private static final ThreadLocal<Request> current = new ThreadLocal<Request>();

   public InternalApplicationContext()
   {
      Bootstrap bootstrap = Bootstrap.foo.get();

      //
      this.descriptor = bootstrap.descriptor;
      this.container = bootstrap.container;
      this.controllerResolver = new ControllerResolver(bootstrap.descriptor);
   }

   public ApplicationDescriptor getDescriptor()
   {
      return descriptor;
   }

   public void invoke(RequestBridge bridge)
   {
      ClassLoader classLoader = container.getClassLoader();

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
      Request request = new Request(classLoader, bridge);

      //
      ClassLoader oldCL = Thread.currentThread().getContextClassLoader();
      try
      {
         Thread.currentThread().setContextClassLoader(classLoader);
         current.set(request);
         ScopeController.begin(request);
         Object ret = doInvoke(request, method);
         if (phase == Phase.ACTION && ret != null && ret instanceof Response)
         {
            try
            {
               ((ActionBridge)bridge).setResponse((Response)ret);
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

   public Object resolveBean(String name)
   {
      BeanManager mgr = container.getManager();
      Set<Bean<?>> beans = mgr.getBeans(name);
      switch (beans.size())
      {
         case 0:
            return null;
         case 1:
            Bean<?> bean = beans.iterator().next();
            CreationalContext<?> cc = mgr.createCreationalContext(bean);
            return mgr.getReference(bean, bean.getBeanClass(), cc);
         default:
            throw new AmbiguousResolutionException();
      }
   }

   private Object doInvoke(Request request, ControllerMethod method)
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
         BeanManager mgr = container.getManager();
         Set<? extends Bean> beans = mgr.getBeans(type);

         if (beans.size() == 1)
         {
            CreationalContext<?> cc = null;
            try
            {
               // Get the bean
               Bean bean = beans.iterator().next();
               cc = mgr.createCreationalContext(bean);

               // Get a reference
               Object o = mgr.getReference(bean, type, cc);

               // Prepare method parameters
               List<ControllerParameter> params = method.getArgumentParameters();
               Object[] args = new Object[params.size()];
               for (int i = 0;i < args.length;i++)
               {
                  String[] values = context.getParameters().get(params.get(i).getName());
                  args[i] = (values != null && values.length > 0) ? values[0] : null;
               }

               //
               return method.getMethod().invoke(o, args);
            }
            catch (Exception e)
            {
               throw new UnsupportedOperationException("handle me gracefully", e);
            }
            finally
            {
               if (cc != null)
               {
                  cc.release();
               }
            }
         }
         else
         {
            return null;
         }
      }
   }

   public Printer getPrinter()
   {
      Request req = current.get();
      RequestContext context = req.getContext();
      if (context instanceof MimeContext)
      {
         return ((MimeContext)context).getPrinter();
      }
      else
      {
         throw new AssertionError("does not make sense");
      }
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
         ClassLoader cl = Thread.currentThread().getContextClassLoader();
         Class<?> stubClass = cl.loadClass(id.toString());
         return(TemplateStub)stubClass.newInstance();
      }
      catch (Exception e)
      {
         throw new UnsupportedOperationException("handle me gracefully");
      }
   }

   @Produces
   public Template resolveTemplate(InjectionPoint point)
   {
      Path path = point.getAnnotated().getAnnotation(Path.class);
      return new Template(this, path.value());
   }

   @Produces
   @RenderScoped
   public RenderContext getRenderContext()
   {
      return (RenderContext)current.get().getContext();
   }

   @Produces
   @ActionScoped
   public ActionContext getActionContext()
   {
      return (ActionContext)current.get().getContext();
   }

   @Produces
   @ResourceScoped
   public ResourceContext getResourceContext()
   {
      return (ResourceContext)current.get().getContext();
   }

   @Override
   public void render(Template template, Printer printer, Map<String, ?> attributes, Locale locale) throws IOException
   {
      Printer toUse = printer != null ? printer : getPrinter();

      //
      TemplateStub stub = resolveTemplateStub(template.getPath());

      //
      ApplicationTemplateRenderContext context = new ApplicationTemplateRenderContext(this, toUse, attributes, locale);

      //
      stub.render(context);

      //
      String title = context.getTitle();
      if (printer == null && title != null)
      {
         RequestContext ctx = current.get().getContext();
         if (ctx instanceof RenderContext)
         {
            ((RenderContext)ctx).setTitle(title);
         }
      }
   }
}
