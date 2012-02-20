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

import org.juzu.impl.utils.JSON;
import org.juzu.request.Phase;
import org.juzu.impl.application.ApplicationException;
import org.juzu.metadata.ControllerMethod;
import org.juzu.test.AbstractTestCase;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A conversation between a client and the application.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class MockClient
{

   private MockRequestBridge create(String url)
   {
      MockRequestBridge request;
      ControllerMethod method;
      JSON json;
      try
      {
         json = (JSON)JSON.parse(url);
         method = application.getContext().getDescriptor().getControllerMethodById(json.getString("op"));
         Phase phase = method.getPhase();
         String methodId = method.getId();
         switch (phase)
         {
            case ACTION:
               request = new MockActionBridge(this, methodId);
               break;
            case RENDER:
               request =  new MockRenderBridge(this, methodId);
               break;
            case RESOURCE:
               request =  new MockResourceBridge(this, methodId);
               break;
            default:
               throw AbstractTestCase.failure("Not yet supported " + phase);
         }
      }
      catch (Exception e)
      {
         throw AbstractTestCase.failure(e);
      }

      //
      try
      {
         JSON jsonParams = json.getJSON("parameters");
         for (String name : jsonParams.names())
         {
            List<? extends String> value = jsonParams.getList(name, String.class);
            request.getParameters().put(name, value.toArray(new String[value.size()]));
         }
      }
      catch (Exception e)
      {
         throw AbstractTestCase.failure(e);
      }

      //
      return request;
   }

   /** . */
   final MockApplication<?> application;

   /** . */
   private final Map<Object, Object> session;

   /** . */
   private Map<Object, Object> flash;

   /** . */
   private final LinkedList<Map<Object, Object>> flashHistory;

   public MockClient(MockApplication<?> application)
   {
      this.application = application;
      this.session = new HashMap<Object, Object>();
      this.flash  = null;
      this.flashHistory = new LinkedList<Map<Object, Object>>();
   }

   public MockRenderBridge render(String methodId) throws ApplicationException
   {
      MockRenderBridge render = new MockRenderBridge(this, methodId);
      invoke(render);
      return render;
   }

   public MockRenderBridge render() throws ApplicationException
   {
      MockRenderBridge render = new MockRenderBridge(this);
      invoke(render);
      return render;
   }

   public MockRequestBridge invoke(String url) throws ApplicationException
   {
      MockRequestBridge request = create(url);
      invoke(request);
      return request;
   }

   public Object getFlashValue(Object key)
   {
      return flash != null ? flash.get(key) : null;
   }

   public void setFlashValue(Object key, Object value)
   {
      if (flash == null)
      {
         flash = new HashMap<Object, Object>();
      }
      flash.put(key, value);
   }

   private void invoke(MockRequestBridge request) throws ApplicationException
   {
      if (request instanceof MockActionBridge)
      {
         application.invoke(request);
      }
      else if (request instanceof MockRenderBridge)
      {
         application.invoke(request);
         flashHistory.addFirst(flash != null ? flash : Collections.emptyMap());
         flash = null;
      }
      else
      {
         application.invoke(request);
      }
   }

   public Map<Object, Object> getFlash()
   {
      return flash;
   }

   public Map<Object, Object> getFlash(int index)
   {
      if (index < 0)
      {
         throw new IndexOutOfBoundsException("Wrong index " + index);
      }
      if (index == 0)
      {
         return flash;
      }
      else
      {
         return flashHistory.get(index - 1);
      }
   }

   public Map<Object, Object> getSession()
   {
      return session;
   }
}
