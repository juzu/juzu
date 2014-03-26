package juzu.impl.bridge.request;

import juzu.impl.common.Tools;
import juzu.impl.request.ContextualParameter;
import juzu.impl.request.EntityUnmarshaller;
import juzu.request.ClientContext;
import juzu.request.RequestParameter;

import java.io.IOException;
import java.util.Map;

/**
* @author Julien Viet
*/
public class FooReader extends EntityUnmarshaller {
  public boolean accept(String mediaType) {
    return mediaType.startsWith("text/foo");
  }
  public void unmarshall(String mediaType, ClientContext context, Iterable<Map.Entry<ContextualParameter, Object>> contextualArguments, Map<String, RequestParameter> parameterArguments) throws IOException {
    byte[] data = Tools.bytes(context.getInputStream());
    Foo foo = new Foo(data);
    for  (Map.Entry<ContextualParameter, Object> parameter : contextualArguments) {
      if (parameter.getKey().getType().equals(Foo.class)) {
        parameter.setValue(foo);
        break;
      }
    }
  }
}
