package org.juzu.portlet;

import org.juzu.impl.request.RenderBridge;

import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import java.io.IOException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class PortletRenderBridge extends PortletMimeBridge<RenderRequest, RenderResponse> implements RenderBridge
{

   public PortletRenderBridge(RenderRequest request, RenderResponse response) throws IOException
   {
      super(request, response);
   }
}
