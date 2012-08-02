/*
 * Copyright (C) 2012 eXo Platform SAS.
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

package juzu.test.protocol.mock;

import juzu.impl.plugin.application.ApplicationException;
import juzu.impl.common.MethodHandle;
import juzu.impl.plugin.controller.descriptor.ControllersDescriptor;
import juzu.impl.plugin.controller.descriptor.MethodDescriptor;
import juzu.impl.inject.Scoped;
import juzu.impl.inject.ScopedContext;
import juzu.impl.common.JSON;
import juzu.impl.common.Tools;
import juzu.test.AbstractTestCase;

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
public class MockClient {

  private MockRequestBridge create(String url) {
    //
    MockRequestBridge request;
    try {
      JSON json = (JSON)JSON.parse(url);

      //
      JSON jsonParams = json.getJSON("parameters");
      Map<String, String[]> parameters = new HashMap<String, String[]>();
      for (String name : jsonParams.names()) {
        List<? extends String> value = jsonParams.getList(name, String.class);
        parameters.put(name, value.toArray(new String[value.size()]));
      }

      //
      MethodDescriptor method = null;
      if (json.getString("target") != null) {
        MethodHandle target = MethodHandle.parse(json.getString("target"));
        method = application.getContext().getDescriptor().getControllers().getMethodByHandle(target);
      }

      //
      if (method != null) {
        switch (method.getPhase()) {
          case ACTION:
            request = new MockActionBridge(application.getContext(), this, method.getHandle(), parameters);
            break;
          case VIEW:
            request = new MockRenderBridge(application.getContext(), this, method.getHandle(), parameters);
            break;
          case RESOURCE:
            request = new MockResourceBridge(application.getContext(), this, method.getHandle(), parameters);
            break;
          default:
            throw AbstractTestCase.failure("Not yet supported " + method.getPhase());
        }
      } else {
        request = new MockRenderBridge(application.getContext(), this, null, parameters);
      }
    }
    catch (Exception e) {
      throw AbstractTestCase.failure(e);
    }

    //
    return request;
  }

  /** . */
  final MockApplication<?> application;

  /** . */
  private final ControllersDescriptor controllers;

  /** . */
  private ScopedContext session;

  /** . */
  private ScopedContext flash;

  /** . */
  private final LinkedList<List<Scoped>> flashHistory;

  public MockClient(MockApplication<?> application) {
    this.application = application;
    this.session = new ScopedContext();
    this.flash = null;
    this.flashHistory = new LinkedList<List<Scoped>>();
    this.controllers = application.getContext().getDescriptor().getControllers();
  }

  public MockRenderBridge render(String methodId) throws ApplicationException {
    MethodHandle handle = null;
    MethodDescriptor method = null;
    if (methodId != null) {
      method = controllers.getMethodById(methodId);
    } else {
      method = controllers.getResolver().resolve(Collections.<String>emptySet());
    }
    if (method != null) {
      handle = method.getHandle();
    }
    MockRenderBridge render = new MockRenderBridge(application.getContext(), this, handle, new HashMap<String, String[]>());
    invoke(render);
    return render;
  }

  public MockRenderBridge render() throws ApplicationException {
    return render(null);
  }

  public MockRequestBridge invoke(String url) throws ApplicationException {
    MockRequestBridge request = create(url);
    invoke(request);
    return request;
  }

  public Scoped getFlashValue(Object key) {
    return flash != null ? flash.get(key) : null;
  }

  public void setFlashValue(Object key, Scoped value) {
    if (flash == null) {
      flash = new ScopedContext();
    }
    flash.set(key, value);
  }

  private void invoke(MockRequestBridge request) throws ApplicationException {
    try {
      application.invoke(request);
    }
    finally {
      request.close();
      if (request instanceof MockRenderBridge) {
        if (flash != null) {
          flashHistory.addFirst(Tools.list(flash));
          flash.close();
          flash = null;
        }
        else {
          flashHistory.addFirst(Collections.<Scoped>emptyList());
        }
      }
    }
  }

  public List<Scoped> getFlashHistory(int index) {
    if (index < 0) {
      throw new IndexOutOfBoundsException("Wrong index " + index);
    }
    if (index == 0) {
      return Tools.list(flash);
    }
    else {
      return flashHistory.get(index - 1);
    }
  }

  public ScopedContext getSession() {
    return session;
  }

  public void invalidate() {
    session.close();
    session = new ScopedContext();
  }
}
