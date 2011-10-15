package org.juzu.portlet;

import org.juzu.URLBuilder;
import org.juzu.application.Phase;
import org.juzu.impl.request.RenderBridge;
import org.juzu.text.Printer;
import org.juzu.text.WriterPrinter;

import javax.portlet.MimeResponse;
import javax.portlet.PortletRequest;
import java.io.IOException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class PortletMimeBridge<Rq extends PortletRequest, Rs extends MimeResponse> extends PortletRequestBridge<Rq, Rs> implements RenderBridge
{

   /** . */
   private final Printer printer;

   public PortletMimeBridge(Rq request, Rs response) throws IOException
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
         case RESOURCE:
            return new URLBuilderImpl(response.createResourceURL());
         default:
            throw new AssertionError("Unexpected phase " + phase);
      }
   }

}
