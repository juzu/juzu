/*
 * Copyright 2013 eXo Platform SAS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package juzu.impl.plugin.ajax;

import juzu.PropertyMap;
import juzu.PropertyType;
import juzu.Response;
import juzu.asset.AssetLocation;
import juzu.impl.plugin.PluginDescriptor;
import juzu.impl.plugin.PluginContext;
import juzu.impl.asset.AssetManager;
import juzu.impl.asset.AssetMetaData;
import juzu.impl.plugin.application.ApplicationPlugin;
import juzu.impl.plugin.controller.ControllerPlugin;
import juzu.impl.request.Method;
import juzu.impl.request.Request;
import juzu.impl.request.RequestFilter;
import juzu.io.Stream;
import juzu.io.Streamable;
import juzu.plugin.ajax.Ajax;
import juzu.request.RenderContext;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class AjaxPlugin extends ApplicationPlugin implements RequestFilter {

  /** . */
  Map<String, Method> table;

  @Inject
  ControllerPlugin controllerPlugin;

  @Inject
  @Named("juzu.asset_manager.script")
  AssetManager manager;

  public AjaxPlugin() {
    super("ajax");
  }

  @Override
  public PluginDescriptor init(PluginContext context) throws Exception {
    return context.getConfig() != null ? new PluginDescriptor() : null;
  }

  @PostConstruct
  public void start() throws Exception {

    //
    URL url = AjaxPlugin.class.getClassLoader().getResource("juzu/impl/plugin/ajax/script.js");
    if (url == null) {
      throw new Exception("Not found script.js");
    }

    //
    manager.addAsset(
        new AssetMetaData(
            "juzu.ajax",
            AssetLocation.APPLICATION,
            "/juzu/impl/plugin/ajax/script.js",
            "jquery"),
        url);

    //
    Map<String, Method> table = new HashMap<String, Method>();
    for (Method cm : controllerPlugin.getDescriptor().getMethods()) {
      Ajax ajax = cm.getMethod().getAnnotation(Ajax.class);
      if (ajax != null) {
        table.put(cm.getName(), cm);
      }
    }

    //
    this.table = table;
  }

  public void invoke(final Request request) {
    request.invoke();

    //
    if (request.getContext() instanceof RenderContext) {
      Response response = request.getResponse();
      if (response instanceof Response.Render) {
        Response.Render render = (Response.Render)response;

        //
        PropertyMap properties = new PropertyMap(response.getProperties());

        //
        properties.addValues(PropertyType.SCRIPT, "juzu.ajax");

        //
        final Streamable<Stream.Char> decorated = render.getStreamable();
        Streamable<Stream.Char> decorator = new Streamable<Stream.Char>() {

          public Class<Stream.Char> getKind() {
            return Stream.Char.class;
          }

          public void send(final Stream.Char stream) throws IOException {
            // FOR NOW WE DO WITH THE METHOD NAME
            // BUT THAT SHOULD BE REVISED TO USE THE ID INSTEAD

            //
            stream.append("<div class=\"jz\">\n");

            //
            for (Map.Entry<String, Method> entry : table.entrySet()) {
              String baseURL = request.getContext().createDispatch(entry.getValue()).toString();
              stream.append("<div data-method-id=\"");
              stream.append(entry.getValue().getId());
              stream.append("\" data-url=\"");
              stream.append(baseURL);
              stream.append("\"/>");
              stream.append("</div>");
            }

            // The page
            decorated.send(new Stream.Char() {
              public Char append(java.lang.CharSequence csq) throws IOException {
                stream.append(csq);
                return this;
              }

              public Char append(java.lang.CharSequence csq, int start, int end) throws IOException {
                stream.append(csq, start, end);
                return this;
              }

              public Char append(char c) throws IOException {
                stream.append(c);
                return this;
              }

              public void flush() throws IOException {
                stream.flush();
              }

              public void close() throws IOException {
                try {
                  stream.append("</div>");
                }
                finally {
                  stream.close();
                }
              }
            });
          }
        };

        //
        request.setResponse(new Response.Render(properties, decorator));
      }
    }
  }
}
