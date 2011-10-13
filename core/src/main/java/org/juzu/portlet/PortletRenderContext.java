package org.juzu.portlet;

import org.juzu.URLBuilder;
import org.juzu.application.Phase;
import org.juzu.impl.request.RenderContext;
import org.juzu.text.Printer;
import org.juzu.text.WriterPrinter;

import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import java.io.IOException;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class PortletRenderContext extends RenderContext
{

   /** . */
   private final RenderRequest request;

   /** . */
   private final RenderResponse response;

   /** . */
   private final Printer printer;

   public PortletRenderContext(ClassLoader classLoader, RenderRequest request, RenderResponse response) throws IOException
   {
      super(classLoader);

      //
      this.request = request;
      this.response = response;
      this.printer = new WriterPrinter(response.getWriter());
   }

   @Override
   public Printer getPrinter()
   {
      return printer;
   }

   @Override
   public Map<String, String[]> getParameters()
   {
      return request.getParameterMap();
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
