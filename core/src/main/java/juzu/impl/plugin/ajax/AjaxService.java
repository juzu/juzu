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
import juzu.Response;
import juzu.asset.AssetLocation;
import juzu.impl.plugin.ServiceContext;
import juzu.impl.plugin.ServiceDescriptor;
import juzu.impl.asset.AssetManager;
import juzu.impl.plugin.application.ApplicationService;
import juzu.impl.plugin.controller.ControllerService;
import juzu.impl.request.ControllerHandler;
import juzu.impl.request.Request;
import juzu.impl.request.RequestFilter;
import juzu.impl.request.Stage;
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
public class AjaxService extends ApplicationService implements RequestFilter<Stage.Unmarshalling> {

  /** . */
  Map<String, ControllerHandler> table;

  @Inject
  ControllerService controllerPlugin;

  @Inject
  AssetManager manager;

  public AjaxService() {
    super("ajax");
  }

  @Override
  public ServiceDescriptor init(ServiceContext context) throws Exception {
    return context.getConfig() != null ? new ServiceDescriptor() : null;
  }

  @PostConstruct
  public void start() throws Exception {

    //
    URL url = AjaxService.class.getClassLoader().getResource("juzu/impl/plugin/ajax/script.js");
    if (url == null) {
      throw new Exception("Not found script.js");
    }

    //
    manager.createDeployment().addAsset(
        "juzu.ajax",
        "script",
        AssetLocation.APPLICATION,
        "/juzu/impl/plugin/ajax/script.js",
        null,
        null, // Think about providing a minified version
        null,
        url,
        "jquery").deploy();

    //
    Map<String, ControllerHandler> table = new HashMap<String, ControllerHandler>();
    for (ControllerHandler cm : controllerPlugin.getDescriptor().getHandlers()) {
      Ajax ajax = cm.getMethod().getAnnotation(Ajax.class);
      if (ajax != null) {
        table.put(cm.getName(), cm);
      }
    }

    //
    this.table = table;
  }

  @Override
  public Class<Stage.Unmarshalling> getStageType() {
    return Stage.Unmarshalling.class;
  }

  @Override
  public Response handle(Stage.Unmarshalling argument) {
    final Request request = argument.getRequest();
    Response result = argument.invoke();

    //
    if (request.getPhase() == Phase.VIEW) {
      if (result instanceof Response.Content) {
        Response.Status status = (Response.Status)result;
        final Streamable wrapped = status.streamable();
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
                  for (Map.Entry<String, ControllerHandler> entry : table.entrySet()) {
                    String baseURL = request.createDispatch(entry.getValue()).toString();
                    sb.append("<div data-method-id=\"");
                    sb.append(entry.getValue().getId());
                    sb.append("\" data-url=\"");
                    sb.append(baseURL);
                    sb.append("\"></div>");
                  }
                  stream.provide(Chunk.create(sb));
                }
                stream.provide(chunk);
              }
              public void close(Thread.UncaughtExceptionHandler errorHandler) {
                stream.provide(Chunk.create("</div>"));
                stream.close(errorHandler);
              }
            };
            wrapped.send(our);
          }
        };
        result = new Response.Content(status.getCode(), wrapper);
      }
    }

    //
    return result;
  }
}
