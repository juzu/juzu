package juzu.impl.standalone;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RouteActionBodyParamTestCase extends AbstractRouteBodyParamTestCase {

  @Override
  protected String[] getApplication() {
    return new String[]{"standalone", "route", "action", "bodyparam"};
  }

}
