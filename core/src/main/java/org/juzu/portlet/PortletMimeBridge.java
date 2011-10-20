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
import org.juzu.impl.request.MimeBridge;
import org.juzu.metadata.ControllerMethod;
import org.juzu.text.Printer;
import org.juzu.text.WriterPrinter;

import javax.portlet.BaseURL;
import javax.portlet.MimeResponse;
import javax.portlet.PortletRequest;
import java.io.IOException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
abstract class PortletMimeBridge<Rq extends PortletRequest, Rs extends MimeResponse> extends PortletRequestBridge<Rq, Rs> implements MimeBridge
{

   /** . */
   private final Printer printer;

   PortletMimeBridge(Rq request, Rs response) throws IOException
   {
      super(request, response);

      //
      this.printer = new WriterPrinter(response.getWriter());
   }

   public Printer getPrinter()
   {
      return printer;
   }

   public URLBuilder createURLBuilder(ControllerMethod method)
   {
      Phase phase = method.getPhase();
      BaseURL url;
      switch (phase)
      {
         case ACTION:
            url = response.createActionURL();
            break;
         case RENDER:
            url = response.createRenderURL();
            break;
         case RESOURCE:
            url = response.createResourceURL();
            break;
         default:
            throw new AssertionError("Unexpected phase " + phase);
      }
      url.setParameter("op", method.getId());
      return new URLBuilderImpl(url);
   }

}
