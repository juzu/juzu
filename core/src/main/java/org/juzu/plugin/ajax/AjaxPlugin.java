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

package org.juzu.plugin.ajax;

import org.juzu.Response;
import org.juzu.impl.application.ApplicationException;
import org.juzu.impl.asset.Registration;
import org.juzu.impl.asset.Router;
import org.juzu.impl.request.Request;
import org.juzu.metadata.ApplicationDescriptor;
import org.juzu.metadata.ControllerMethod;
import org.juzu.plugin.Plugin;
import org.juzu.request.RenderContext;
import org.juzu.text.Printer;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
@Singleton
public class AjaxPlugin extends Plugin
{

   /** . */
   private Registration<PluginAsset> pluginRegistration;

   /** . */
   private Registration<ApplicationAsset> applicationRegistration;

   @Inject
   public AjaxPlugin(
      ApplicationDescriptor desc,
      @Named("plugin") Router plugin,
      @Named("application") Router application)
   {
      this.pluginRegistration = plugin.register("ajax.js", PluginAsset.class);
      this.applicationRegistration = application.register("ajax.js", new ApplicationAsset(desc));
   }

   @PreDestroy
   public void destroy()
   {
      pluginRegistration.cancel();
      applicationRegistration.cancel();
   }

   @Override
   public void invoke(final Request request) throws ApplicationException
   {
      super.invoke(request);

      //
      if (request.getContext() instanceof RenderContext)
      {
         Response response = request.getResponse();
         if (response instanceof Response.Render)
         {
            final Response.Render foo = (Response.Render)response;
            response = new Response.Render()
            {
               @Override
               public String getTitle()
               {
                  return foo.getTitle();
               }

               @Override
               public Collection<String> getScripts()
               {
                  try
                  {
                     ArrayList<String> scripts = new ArrayList<String>(foo.getScripts());
                     StringBuilder sb1 = new StringBuilder();
                     pluginRegistration.getRoute().getContext().renderURL(sb1);
                     scripts.add(sb1.toString());
                     StringBuilder sb2 = new StringBuilder();
                     applicationRegistration.getRoute().getContext().renderURL(sb2);
                     scripts.add(sb2.toString());
                     return scripts;
                  }
                  catch (IOException e)
                  {
                     throw new UnsupportedOperationException("todo");
                  }
               }

               public void send(Printer printer) throws IOException
               {

                  // FOR NOW WE DO WITH THE METHOD NAME
                  // BUT THAT SHOULD BE REVISED TO USE THE ID INSTEAD

                  //
                  printer.write("<div class=\"jz\">\n");
                  
                  //
                  for (Map.Entry<String, ControllerMethod> entry : applicationRegistration.getRoute().table.entrySet())
                  {
                     String baseURL = ((RenderContext)request.getContext()).createURLBuilder(entry.getValue()).toString();
                     printer.write("<div data-method-id=\"");
                     printer.write(entry.getValue().getId());
                     printer.write("\" data-url=\"");
                     printer.write(baseURL);
                     printer.write("\"/>");
                  }

                  // The page
                  foo.send(printer);

                  //
                  printer.write("</div>");
               }
            };
            request.setResponse(response);
         }
      }
   }
}
