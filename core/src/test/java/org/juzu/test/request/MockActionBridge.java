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
import org.juzu.impl.spi.request.ActionBridge;
import org.juzu.test.AbstractTestCase;

import java.io.IOException;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class MockActionBridge extends MockRequestBridge<Response.Action> implements ActionBridge
{

   /** . */
   private Response response;

   public MockActionBridge(MockClient client, String methodId)
   {
      super(client, methodId);
   }

   public void assertNoResponse()
   {
      assertResponse(null);
   }

   public void assertRedirect(String location)
   {
      assertResponse(new Response.Action.Redirect(location));
   }

   public void assertRender(String expectedMethodId, Map<String, String> expectedArguments)
   {
      Response.Action.Render resp = new Response.Action.Render(expectedMethodId);
      resp.getParameters().putAll(expectedArguments);
      assertResponse(resp);
   }

   private void assertResponse(Response expectedResponse)
   {
      AbstractTestCase.assertEquals("Was expecting a response " + expectedResponse + " instead of  " + response,
         expectedResponse,
         response);
   }

   public void setResponse(Response.Action response) throws IllegalStateException, IOException
   {
      if (this.response != null)
      {
         throw new IllegalStateException();
      }

      //
      this.response = response;
   }
}
