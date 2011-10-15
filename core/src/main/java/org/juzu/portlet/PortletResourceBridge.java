package org.juzu.portlet;

import org.juzu.impl.request.ResourceBridge;

import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import java.io.IOException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class PortletResourceBridge extends PortletMimeBridge<ResourceRequest, ResourceResponse> implements ResourceBridge
{

   public PortletResourceBridge(ResourceRequest request, ResourceResponse response) throws IOException
   {
      super(request, response);
   }
}
