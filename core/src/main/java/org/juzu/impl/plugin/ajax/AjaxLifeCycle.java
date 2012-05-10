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

package org.juzu.impl.plugin.ajax;

import org.juzu.PropertyMap;
import org.juzu.Response;
import org.juzu.impl.application.ApplicationException;
import org.juzu.impl.application.metadata.ApplicationDescriptor;
import org.juzu.impl.asset.Registration;
import org.juzu.impl.asset.Router;
import org.juzu.impl.controller.descriptor.ControllerMethod;
import org.juzu.impl.request.RequestLifeCycle;
import org.juzu.impl.request.Request;
import org.juzu.io.Stream;
import org.juzu.io.Streamable;
import org.juzu.request.RenderContext;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
@Singleton
public class AjaxLifeCycle extends RequestLifeCycle
{

   /** . */
   private Registration<PluginAsset> pluginRegistration;

   /** . */
   private Registration<ApplicationAsset> applicationRegistration;

   @Inject
   public AjaxLifeCycle(
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
            Response.Render render = (Response.Render)response;

            //
            PropertyMap properties = new PropertyMap(response.getProperties());
            try
            {
               StringBuilder sb1 = new StringBuilder();
               pluginRegistration.getRoute().getContext().renderURL(sb1);
               StringBuilder sb2 = new StringBuilder();
               applicationRegistration.getRoute().getContext().renderURL(sb2);
               properties.addValue(Response.Render.SCRIPT, sb1.toString());
               properties.addValue(Response.Render.SCRIPT, sb2.toString());
            }
            catch (IOException e)
            {
               throw new UnsupportedOperationException("todo");
            }

            //
            final Streamable<Stream.Char> decorated = render.getStreamable();
            Streamable<Stream.Char> decorator = new Streamable<Stream.Char>()
            {
               public void send(Stream.Char stream) throws IOException
               {
                  // FOR NOW WE DO WITH THE METHOD NAME
                  // BUT THAT SHOULD BE REVISED TO USE THE ID INSTEAD

                  //
                  stream.append("<div class=\"jz\">\n");

                  //
                  for (Map.Entry<String, ControllerMethod> entry : applicationRegistration.getRoute().table.entrySet())
                  {
                     String baseURL = ((RenderContext)request.getContext()).createURLBuilder(entry.getValue()).toString();
                     stream.append("<div data-method-id=\"");
                     stream.append(entry.getValue().getId());
                     stream.append("\" data-url=\"");
                     stream.append(baseURL);
                     stream.append("\"/>");
                     stream.append("</div>");
                  }

                  // The page
                  decorated.send(stream);

                  //
                  stream.append("</div>");
               }
            };

            //
            request.setResponse(new Response.Render(properties, decorator));
         }
      }
   }
}
