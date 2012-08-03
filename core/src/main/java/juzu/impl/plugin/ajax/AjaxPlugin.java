/*
 * Copyright (C) 2012 eXo Platform SAS.
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

package juzu.impl.plugin.ajax;

import juzu.PropertyMap;
import juzu.PropertyType;
import juzu.Response;
import juzu.asset.Asset;
import juzu.asset.AssetLocation;
import juzu.asset.AssetType;
import juzu.impl.plugin.application.ApplicationException;
import juzu.impl.plugin.application.descriptor.ApplicationDescriptor;
import juzu.impl.asset.AssetManager;
import juzu.impl.asset.AssetMetaData;
import juzu.impl.asset.Manager;
import juzu.impl.plugin.application.ApplicationPlugin;
import juzu.impl.plugin.controller.descriptor.MethodDescriptor;
import juzu.impl.request.Request;
import juzu.impl.request.RequestFilter;
import juzu.io.Stream;
import juzu.io.Streamable;
import juzu.plugin.ajax.Ajax;
import juzu.request.RenderContext;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class AjaxPlugin extends ApplicationPlugin implements RequestFilter {

  /** . */
  Map<String, MethodDescriptor> table;

  @Inject
  ApplicationDescriptor desc;

  @Inject
  @Manager(AssetType.SCRIPT) AssetManager manager;

  public AjaxPlugin() {
    super("ajax");
  }

  @PostConstruct
  public void start() {
    manager.addAsset(new AssetMetaData("juzu.ajax", AssetLocation.CLASSPATH, "/juzu/impl/plugin/ajax/script.js", "jquery"));

    //
    Map<String, MethodDescriptor> table = new HashMap<String, MethodDescriptor>();
    for (MethodDescriptor cm : desc.getControllers().getMethods()) {
      Ajax ajax = cm.getMethod().getAnnotation(Ajax.class);
      if (ajax != null) {
        table.put(cm.getName(), cm);
      }
    }

    //
    this.table = table;
  }

  public void invoke(final Request request) throws ApplicationException {
    request.invoke();

    //
    if (request.getContext() instanceof RenderContext) {
      Response response = request.getResponse();
      if (response instanceof Response.Render) {
        Response.Render render = (Response.Render)response;

        //
        PropertyMap properties = new PropertyMap(response.getProperties());

        //
        properties.addValues(PropertyType.SCRIPT, Asset.ref("juzu.ajax"));

        //
        final Streamable<Stream.Char> decorated = render.getStreamable();
        Streamable<Stream.Char> decorator = new Streamable<Stream.Char>() {
          public void send(Stream.Char stream) throws IOException {
            // FOR NOW WE DO WITH THE METHOD NAME
            // BUT THAT SHOULD BE REVISED TO USE THE ID INSTEAD

            //
            stream.append("<div class=\"jz\">\n");

            //
            for (Map.Entry<String, MethodDescriptor> entry : table.entrySet()) {
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
