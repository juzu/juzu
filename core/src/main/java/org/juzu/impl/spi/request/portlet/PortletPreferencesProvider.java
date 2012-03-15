package org.juzu.impl.spi.request.portlet;

import org.juzu.impl.request.Request;

import javax.inject.Provider;
import javax.portlet.PortletPreferences;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class PortletPreferencesProvider implements Provider<PortletPreferences>
{
   public PortletPreferences get()
   {
      Request request = Request.getCurrent();
      PortletRequestBridge bridge = (PortletRequestBridge)request.getBridge();
      return bridge.request.getPreferences();
   }
}
