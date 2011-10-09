package org.juzu.portlet;

import org.juzu.URLBuilder;
import org.juzu.application.Phase;
import org.juzu.impl.request.URLBuilderContext;

import javax.portlet.RenderResponse;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class PortletURLBuilderContext implements URLBuilderContext
{

   /** . */
   private final RenderResponse response;

   PortletURLBuilderContext(RenderResponse response)
   {
      this.response = response;
   }

   public URLBuilder createURLBuilder(Phase phase)
   {
      switch (phase)
      {
         case ACTION:
            return new PortletURLBuilder(response.createActionURL());
         case RENDER:
            return new PortletURLBuilder(response.createRenderURL());
         default:
            throw new AssertionError();
      }
   }
}
