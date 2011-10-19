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

import org.juzu.Phase;
import org.juzu.URLBuilder;
import org.juzu.impl.request.MimeBridge;
import org.juzu.text.Printer;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class MockMimeBridge extends MockRequestBridge implements MimeBridge
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

   public URLBuilder createURLBuilder(Phase phase)
   {
      return new MockURLBuilder(phase);
   }

   public Printer getPrinter()
   {
      return printer;
   }
}
