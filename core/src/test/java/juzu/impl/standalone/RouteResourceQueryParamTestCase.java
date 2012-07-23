package juzu.impl.standalone;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RouteResourceQueryParamTestCase extends AbstractRouteQueryParamTestCase {

  @Override
  protected String[] getApplication() {
    return new String[]{"standalone", "route", "resource", "queryparam"};
  }
}
