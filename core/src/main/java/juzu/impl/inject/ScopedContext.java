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

package juzu.impl.inject;

import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

/**
 * <p></p>An helper class for managing scoped entries. It implements the {@link HttpSessionBindingListener} interface
 * which invokes the {@link #close()} method when the servlet container invokes the {@link
 * #valueUnbound(javax.servlet.http.HttpSessionBindingEvent)} callback.</p> <p/> <p>At the moment we do not support
 * serialization.</p>
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public final class ScopedContext implements HttpSessionBindingListener, Iterable<Scoped> {

  /** . */
  private HashMap<Object, Scoped> state;

  public Scoped get(Object key) throws NullPointerException {
    if (key == null) {
      throw new NullPointerException("No null key accepted");
    }
    return state != null ? state.get(key) : null;
  }

  public void set(Object key, Scoped scoped) throws NullPointerException {
    if (key == null) {
      throw new NullPointerException("No null key accepted");
    }
    if (scoped == null) {
      if (state != null) {
        state.remove(key);
      }
    }
    else {
      if (state == null) {
        state = new HashMap<Object, Scoped>();
      }
      state.put(key, scoped);
    }
  }

  public int size() {
    return state != null ? state.size() : 0;
  }

  public Iterator<Scoped> iterator() {
    return state == null ? Collections.<Scoped>emptyList().iterator() : state.values().iterator();
  }

  public void valueBound(HttpSessionBindingEvent event) {
    // Nothing to do
  }

  public void valueUnbound(HttpSessionBindingEvent event) {
    close();
  }

  public void close() {
    if (state != null && state.size() > 0) {
      for (Iterator<Scoped> i = state.values().iterator();i.hasNext();) {
        Scoped scoped = i.next();
        i.remove();
        try {
          scoped.destroy();
        }
        catch (Throwable t) {
          // Should not happen somehow ?
          // question is does the destroy() method provide some guarantee it does not happen ?
          System.out.println("Error when destroying object (should log better of course)");
          t.printStackTrace();
        }
      }
    }
  }
}
