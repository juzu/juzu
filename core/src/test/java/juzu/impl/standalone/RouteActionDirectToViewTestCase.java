package juzu.impl.standalone;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RouteActionDirectToViewTestCase extends AbstractRouteActionToViewTestCase {

  @Override
  protected String[] getApplication() {
    return new String[]{"standalone", "route", "action", "directtoview"};
  }
}
