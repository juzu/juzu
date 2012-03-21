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

import org.juzu.PropertyType;
import org.juzu.Response;
import org.juzu.impl.spi.request.ActionBridge;
import org.juzu.request.Phase;
import org.juzu.request.RequestContext;
import org.juzu.test.AbstractTestCase;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class MockActionBridge extends MockRequestBridge implements ActionBridge
{

   /** . */
   private Response response;

   public MockActionBridge(MockClient client, String methodId, Map<String, String[]> parameters)
   {
      super(client, methodId, parameters);
   }
   
   public String assertUpdate()
   {
      if (response instanceof Response.Update)
      {
         Response.Update update = (Response.Update)response;
         return renderURL(Phase.RENDER, update.getParameters(), update.getProperties());
      }
      else
      {
         throw AbstractTestCase.failure("Was expecting an update instead of " + response);
      }
   }

   public void assertNoResponse()
   {
      assertResponse(null);
   }

   public void assertRedirect(String location)
   {
      assertResponse(new Response.Redirect(location));
   }

   public void assertRender(String expectedMethodId, Map<String, String> expectedArguments)
   {
      Response.Update resp = new Response.Update().setProperty(RequestContext.METHOD_ID, expectedMethodId);
      for (Map.Entry<String, String> entry : expectedArguments.entrySet())
      {
         resp.setParameter(entry.getKey(), entry.getValue());
      }
      assertResponse(resp);
   }

   private void assertResponse(Response expectedResponse)
   {
      if (expectedResponse instanceof Response.Update)
      {
         Response.Update expected = (Response.Update) expectedResponse;
         Response.Update resp = (Response.Update) response;
         
         AbstractTestCase.assertEquals(expected.getParameters().size(), expected.getParameters().size());
         for(String key : resp.getParameters().keySet())
         {
            AbstractTestCase.assertEquals(
               Arrays.asList(expected.getParameters().get(key)),
               Arrays.asList(resp.getParameters().get(key)));
         }
      }
      else
      {
         AbstractTestCase.assertEquals("Was expecting a response " + expectedResponse + " instead of  " + response,
            expectedResponse,
            response);
      }
   }

   public void setResponse(Response response) throws IllegalStateException, IOException
   {
      if (this.response != null)
      {
         throw new IllegalStateException();
      }

      //
      this.response = response;
   }
}
