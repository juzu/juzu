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
import org.juzu.io.Stream;
import org.juzu.io.BinaryOutputStream;
import org.juzu.io.AppendableStream;
import org.juzu.test.AbstractTestCase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class MockMimeBridge extends MockRequestBridge implements MimeBridge
{

   /** . */
   private Object result;

   /** . */
   private String mimeType;

   public MockMimeBridge(MockClient client, String methodId, Map<String, String[]> parameters)
   {
      super(client, methodId, parameters);
   }

   public String assertStringResult()
   {
      return AbstractTestCase.assertInstanceOf(String.class, result);
   }

   public byte[] assertBinaryResult()
   {
      return AbstractTestCase.assertInstanceOf(byte[].class, result);
   }

   public String getMimeType()
   {
      return mimeType;
   }

   public void end(Response response) throws IllegalStateException, IOException
   {
      if (response instanceof Response.Content<?>)
      {
         Response.Content content = (Response.Content)response;
         if (content.getKind() == Stream.Binary.class)
         {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            BinaryOutputStream bos = new BinaryOutputStream(baos);
            content.send(bos);
            result = baos.toByteArray();
         }
         else 
         {
            StringBuilder builder = new StringBuilder();
            content.send(new AppendableStream(builder));
            result = builder.toString();
         }
         mimeType = content.getMimeType();
      }
   }
}
