package juzu.impl.router;

import juzu.UndeclaredIOException;
import juzu.impl.common.QualifiedName;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RouteMatchTestCase extends AbstractControllerTestCase {

  private void assertMatch(String expected, Route route, Map<QualifiedName, String> parameters) {
    RouteMatch match = route.matches(parameters);
    assertNotNull("Was expecting to match " + expected, match);
    StringBuilder sb = new StringBuilder();
    URIWriter writer = new URIWriter(sb);
    try {
      match.render(writer);
      for (Map.Entry<QualifiedName, String> entry : match.getUnmatched().entrySet()) {
        writer.appendQueryParameter(entry.getKey().getName(), entry.getValue());
      }
    }
    catch (IOException e) {
      throw new UndeclaredIOException(e);
    }
  }

  @Test
  public void testPath() {
    Router router = new Router();
    Route a = router.append("/a");
    RouteMatch match = a.matches(Collections.<QualifiedName, String>emptyMap());
    assertNotNull(match);
    assertMatch("/a?a=foo", a, Collections.<QualifiedName, String>singletonMap(Names.A, "foo"));
  }

  @Test
  public void testPathParam() {
    Router router = new Router();
    Route a = router.append("/{a}");
    assertNull(a.matches(Collections.<QualifiedName, String>emptyMap()));
    assertMatch("/foo", a, Collections.<QualifiedName, String>singletonMap(Names.A, "foo"));
  }
}
