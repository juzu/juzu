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

import org.junit.Assert;
import org.juzu.Response;
import org.juzu.impl.spi.request.ResourceBridge;

import java.io.IOException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class MockResourceBridge extends MockMimeBridge<Response.Mime.Resource> implements ResourceBridge
{

   /** . */
   private int status;
   
   public MockResourceBridge(MockClient client, String methodId)
   {
      super(client, methodId);
   }

   public void assertOk()
   {
      assertStatus(200);
   }

   public void assertNotFound()
   {
      assertStatus(404);
   }

   public void assertStatus(int status)
   {
      Assert.assertEquals(status, this.status);
   }

   @Override
   public void setResponse(Response.Mime response) throws IllegalStateException, IOException
   {
      super.setResponse(response);
      
      //
      if (response instanceof Response.Mime.Resource)
      {
         Response.Mime.Resource resource = (Response.Mime.Resource)response;
         status = resource.getStatus();
      }
      else
      {
         status = 200;
      }
   }
}
