/*
 * Copyright (C) 2011 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.juzu.portlet;

import org.juzu.Phase;
import org.juzu.URLBuilder;
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
