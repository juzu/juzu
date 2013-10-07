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

import juzu.PropertyType;
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
import juzu.request.Result;
import juzu.io.Chunk;
import juzu.io.Stream;
import juzu.io.Streamable;
import juzu.plugin.ajax.Ajax;
import juzu.request.Phase;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
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
    if (request.getPhase() == Phase.VIEW) {
      Result result = request.getResult();
      if (result instanceof Result.Status) {
        Result.Status status = (Result.Status)result;
        if (status.decorated) {
          final Streamable wrapped = status.streamable;
          Streamable wrapper = new Streamable() {
            public void send(final Stream stream) throws IllegalStateException {
              Stream our = new Stream() {
                boolean done = false;
                public void provide(Chunk chunk) {
                  if (chunk instanceof Chunk.Data && !done) {
                    done = true;
                    stream.provide(new Chunk.Property<String>("juzu.ajax", PropertyType.ASSET));
                    // FOR NOW WE DO WITH THE METHOD NAME
                    // BUT THAT SHOULD BE REVISED TO USE THE ID INSTEAD
                    StringBuilder sb = new StringBuilder();
                    sb.append("<div class=\"jz\">\n");
                    for (Map.Entry<String, Method> entry : table.entrySet()) {
                      String baseURL = request.createDispatch(entry.getValue()).toString();
                      sb.append("<div data-method-id=\"");
                      sb.append(entry.getValue().getId());
                      sb.append("\" data-url=\"");
                      sb.append(baseURL);
                      sb.append("\"/>");
                      sb.append("</div>");
                    }
                    sb.append("</div>");
                    stream.provide(Chunk.create(sb));
                  }
                  stream.provide(chunk);
                }
                public void close(Thread.UncaughtExceptionHandler errorHandler) {
                  stream.close(errorHandler);
                }
              };
              wrapped.send(our);
            }
          };
          request.setResult(new Result.Status(status.code, true, wrapper));
        }
      }
    }
  }
}
