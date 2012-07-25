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

package juzu;

import juzu.asset.Asset;

import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class PropertyType<T> {

  /** Script. */
  public static PropertyType<Asset> SCRIPT = new PropertyType<Asset>(){};

  /** Stylesheet. */
  public static PropertyType<Asset> STYLESHEET = new PropertyType<Asset>(){};

  /** Title. */
  public static PropertyType<String> TITLE = new PropertyType<String>(){};

  /** Path. */
  public static final PropertyType<String> PATH = new PropertyType<String>(){};

  /** Redirect after action. */
  public static final PropertyType<Boolean> REDIRECT_AFTER_ACTION = new PropertyType<Boolean>(){};

  /** Header. */
  public static final PropertyType<Map.Entry<String, String[]>> HEADER = new PropertyType<Map.Entry<String, String[]>>(){};

  /** Mime type. */
  public static PropertyType<String> MIME_TYPE = new PropertyType<String>(){};

  /** Escape XML. */
  public static PropertyType<Boolean> ESCAPE_XML = new PropertyType<Boolean>(){};

  protected PropertyType() throws NullPointerException {
  }

  public final T cast(Object o) {
    return (T)o;
  }

  @Override
  public final boolean equals(Object obj) {
    return obj == this || obj != null && getClass().equals(obj.getClass());
  }

  @Override
  public final int hashCode() {
    return getClass().hashCode();
  }
}
