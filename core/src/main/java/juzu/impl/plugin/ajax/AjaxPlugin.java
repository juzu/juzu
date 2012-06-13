package juzu.impl.plugin.ajax;

import juzu.PropertyMap;
import juzu.Response;
import juzu.asset.Asset;
import juzu.asset.AssetLocation;
import juzu.asset.AssetType;
import juzu.impl.application.ApplicationException;
import juzu.impl.application.ApplicationDescriptor;
import juzu.impl.asset.AssetManager;
import juzu.impl.asset.AssetMetaData;
import juzu.impl.asset.Manager;
import juzu.impl.controller.descriptor.ControllerMethod;
import juzu.impl.plugin.Plugin;
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
public class AjaxPlugin extends Plugin implements RequestFilter {

  /** . */
  Map<String, ControllerMethod> table;

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
    Map<String, ControllerMethod> table = new HashMap<String, ControllerMethod>();
    for (ControllerMethod cm : desc.getController().getMethods()) {
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
        properties.addValues(Response.Render.SCRIPT, Asset.ref("juzu.ajax"));

        //
        final Streamable<Stream.Char> decorated = render.getStreamable();
        Streamable<Stream.Char> decorator = new Streamable<Stream.Char>() {
          public void send(Stream.Char stream) throws IOException {
            // FOR NOW WE DO WITH THE METHOD NAME
            // BUT THAT SHOULD BE REVISED TO USE THE ID INSTEAD

            //
            stream.append("<div class=\"jz\">\n");

            //
            for (Map.Entry<String, ControllerMethod> entry : table.entrySet()) {
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
