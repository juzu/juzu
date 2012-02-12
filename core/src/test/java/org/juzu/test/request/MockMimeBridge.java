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

package org.juzu.test.request;

import org.juzu.Response;
import org.juzu.URLBuilder;
import org.juzu.impl.spi.request.MimeBridge;
import org.juzu.metadata.ControllerMethod;
import org.juzu.text.Printer;

import java.io.IOException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class MockMimeBridge<R extends Response.Mime> extends MockRequestBridge<R> implements MimeBridge<R>
{

   /** . */
   private final MockPrinter printer;

   public MockMimeBridge(MockClient client, String methodId)
   {
      super(client, methodId);

      //
      printer = new MockPrinter();
   }

   public String getContent()
   {
      return printer.getContent().toString();
   }

   public URLBuilder createURLBuilder(ControllerMethod method)
   {
      return new MockURLBuilder(method);
   }

   public Printer getPrinter()
   {
      return printer;
   }

   public void setResponse(Response.Mime response) throws IllegalStateException, IOException
   {
      if (response instanceof Response.Mime.Stream)
      {
         Response.Mime.Stream stream = (Response.Mime.Stream)response;
         stream.send(printer);
      }
      else
      {
         throw new UnsupportedOperationException();
      }
   }
}
