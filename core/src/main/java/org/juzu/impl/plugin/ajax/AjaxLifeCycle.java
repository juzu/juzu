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
import org.juzu.asset.Asset;
import org.juzu.asset.AssetLocation;
import org.juzu.asset.AssetType;
import org.juzu.impl.application.ApplicationException;
import org.juzu.impl.application.metadata.ApplicationDescriptor;
import org.juzu.impl.asset.AssetMetaData;
import org.juzu.impl.asset.Manager;
import org.juzu.impl.asset.AssetManager;
import org.juzu.impl.controller.descriptor.ControllerMethod;
import org.juzu.impl.request.RequestLifeCycle;
import org.juzu.impl.request.Request;
import org.juzu.io.Stream;
import org.juzu.io.Streamable;
import org.juzu.plugin.ajax.Ajax;
import org.juzu.request.RenderContext;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
@Singleton
public class AjaxLifeCycle extends RequestLifeCycle
{

   /** . */
   final ApplicationDescriptor desc;

   /** . */
   final Map<String, ControllerMethod> table;

   @Inject
   public AjaxLifeCycle(
      ApplicationDescriptor desc,
      @Manager(AssetType.SCRIPT) AssetManager manager)
   {
      manager.addAsset(new AssetMetaData("ajax.plugin", AssetLocation.CLASSPATH, "/org/juzu/impl/plugin/ajax/script.js", "jquery"));

      //
      Map<String, ControllerMethod> table = new HashMap<String, ControllerMethod>();
      for (ControllerMethod cm : desc.getController().getMethods())
      {
         Ajax ajax = cm.getMethod().getAnnotation(Ajax.class);
         if (ajax != null)
         {
            table.put(cm.getName(), cm);
         }
      }

      //
      this.desc = desc;
      this.table = table;
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

            //
            properties.addValues(Response.Render.SCRIPT, Asset.ref("ajax.plugin"));

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
                  for (Map.Entry<String, ControllerMethod> entry : table.entrySet())
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
