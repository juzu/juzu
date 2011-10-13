package org.juzu.portlet;

import org.juzu.URLBuilder;
import org.juzu.application.Phase;
import org.juzu.impl.request.RenderBridge;
import org.juzu.text.Printer;
import org.juzu.text.WriterPrinter;

import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import java.io.IOException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class PortletRenderBridge extends PortletRequestBridge<RenderRequest, RenderResponse> implements RenderBridge
{

   /** . */
   private final Printer printer;

   public PortletRenderBridge(RenderRequest request, RenderResponse response) throws IOException
   {
      super(request, response);

      //
      this.printer = new WriterPrinter(response.getWriter());
   }

   public Printer getPrinter()
   {
      return printer;
   }

   public URLBuilder createURLBuilder(Phase phase)
   {
      switch (phase)
      {
         case ACTION:
            return new URLBuilderImpl(response.createActionURL());
         case RENDER:
            return new URLBuilderImpl(response.createRenderURL());
         default:
            throw new AssertionError();
      }
   }
}
