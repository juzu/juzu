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
import org.juzu.impl.request.ActionBridge;
import org.juzu.metadata.ControllerMethod;
import org.juzu.test.AbstractTestCase;

import java.util.ArrayList;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class MockActionBridge extends MockRequestBridge implements ActionBridge
{

   /** . */
   private final List<Object> responses = new ArrayList<Object>();

   public MockActionBridge(MockClient client, String methodId)
   {
      super(client, methodId);
   }

   public Response createResponse(ControllerMethod method)
   {
      MockResponse response = new MockResponse(method.getId());
      responses.add(response);
      return response;
   }

   public void redirect(String location)
   {
      responses.add(location);
   }

   public void assertNoResponse()
   {
      AbstractTestCase.assertEquals("Was expecting no response instead of " +
         responses, 0, responses.size());
   }

   public void assertRedirect(String location)
   {
      assertResponse(location);
   }

   private void assertResponse(Object expectedResponse)
   {
      AbstractTestCase.assertEquals("Was expecting a single response " + expectedResponse + " instead of " +
         responses, 1, responses.size());
      AbstractTestCase.assertEquals(expectedResponse, responses.get(0));
   }
}
