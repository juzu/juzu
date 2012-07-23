package juzu.impl.standalone;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.openqa.selenium.WebDriver;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RouteViewPathMappingTestCase extends AbstractRoutePathMappingTestCase {

  @Drone
  WebDriver driver;

  @Override
  protected String[] getApplication() {
    return new String[]{"standalone", "route", "view", "pathmapping"};
  }
}
