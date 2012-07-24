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

import juzu.request.WindowContext;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class MockWindowContext implements WindowContext {

  /** . */
  private String id;

  /** . */
  private String ns;

  /** . */
  private String baseValue;

  public MockWindowContext() {
    setBaseValue("window");
  }

  public String getBaseValue() {
    return baseValue;
  }

  public void setBaseValue(String baseValue) throws NullPointerException {
    if (baseValue == null) {
      throw new NullPointerException();
    }

    //
    this.baseValue = baseValue;
    this.id = baseValue + "_id";
    this.ns = baseValue + "_ns";
  }

  public String getId() {
    return id;
  }

  public String getNamespace() {
    return ns;
  }
}
