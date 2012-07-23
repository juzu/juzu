package juzu.impl.standalone;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RouteResourcePathParamTestCase extends AbstractRoutePathParamTestCase {

  @Override
  protected String[] getApplication() {
    return new String[]{"standalone", "route", "resource", "pathparam"};
  }
}
