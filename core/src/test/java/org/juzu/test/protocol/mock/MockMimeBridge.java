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

package org.juzu.test.protocol.mock;

import org.juzu.Response;
import org.juzu.impl.spi.request.MimeBridge;
import org.juzu.impl.utils.JSON;
import org.juzu.request.Phase;
import org.juzu.text.Printer;

import java.io.IOException;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class MockMimeBridge extends MockRequestBridge implements MimeBridge
{

   /** . */
   private final MockPrinter printer;

   public MockMimeBridge(MockClient client)
   {
      super(client);

      //
      printer = new MockPrinter();
   }

   public String getContent()
   {
      return printer.getContent().toString();
   }

   public String renderURL(Phase phase, Boolean escapeXML, Map<String, String[]> parameters)
   {
      JSON url = new JSON();
      url.add("phase", phase.name());
      url.add("parameters", parameters);
      if (escapeXML != null)
      {
         url.add("escapeXML", escapeXML);
      }
      return url.toString();
   }

   public Printer getPrinter()
   {
      return printer;
   }

   public void setResponse(Response response) throws IllegalStateException, IOException
   {
      if (response instanceof Response.Content)
      {
         Response.Content stream = (Response.Content)response;
         stream.send(printer);
      }
      else
      {
         throw new UnsupportedOperationException("Cannot handle response " + response);
      }
   }
}
