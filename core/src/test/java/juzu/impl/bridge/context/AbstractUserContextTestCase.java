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

package juzu.impl.bridge.context;

import juzu.test.AbstractWebTestCase;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class AbstractUserContextTestCase extends AbstractWebTestCase {

  /** . */
  public static Locale locale;

  protected void test(URL initialURL) throws Exception {
    locale = null;
    HttpURLConnection conn = (HttpURLConnection)initialURL.openConnection();
    conn.addRequestProperty("Accept-Language", "fr-FR");
    assertEquals(200, conn.getResponseCode());
    assertEquals(Locale.FRANCE, locale);
  }
}
