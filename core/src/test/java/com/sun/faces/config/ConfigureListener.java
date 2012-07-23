package com.sun.faces.config;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Do nothing, used to make org.jboss.portletbridge.testing.pluto:pluto-container happy, it should be removed
 * at some point.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class ConfigureListener implements ServletContextListener {

  public void contextInitialized(ServletContextEvent sce) {
  }

  public void contextDestroyed(ServletContextEvent sce) {
  }
}
