/*
 * Copyright 2013 eXo Platform SAS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package juzu.impl.bridge.spi.portlet;

import juzu.request.UserContext;

import javax.portlet.PortletRequest;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class PortletUserContext implements UserContext {

  /** . */
  final PortletRequest request;

  PortletUserContext(PortletRequest request) {
    this.request = request;
  }

  public Locale getLocale() {
    return request.getLocale();
  }

  public Iterable<Locale> getLocales() {
    return new Iterable<Locale>() {
      public Iterator<Locale> iterator() {
        return new Iterator<Locale>() {
          Enumeration<Locale> e = request.getLocales();
          public boolean hasNext() {
            return e.hasMoreElements();
          }
          public Locale next() {
            return e.nextElement();
          }
          public void remove() {
            throw new UnsupportedOperationException();
          }
        };
      }
    };
  }
}
