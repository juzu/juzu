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

  /** Script type literal. */
  public static class SCRIPT extends PropertyType<Asset> {}

  /** Script type literal instance. */
  public static SCRIPT SCRIPT = new SCRIPT();

  /** Stylesheet type literal. */
  public static class STYLESHEET extends PropertyType<Asset> {}

  /** Stylesheet literal instance. */
  public static STYLESHEET STYLESHEET = new STYLESHEET();

  /** Stylesheet type literal. */
  public static class TITLE extends PropertyType<String> {}

  /** Stylesheet literal instance. */
  public static TITLE TITLE = new TITLE();

  /** . */
  public static final class PATH extends PropertyType<String> {}

  /** . */
  public static final PropertyType.PATH PATH = new PropertyType.PATH();

  /** . */
  public static final class REDIRECT_AFTER_ACTION extends PropertyType<Boolean> {}

  /** . */
  public static final REDIRECT_AFTER_ACTION REDIRECT_AFTER_ACTION = new REDIRECT_AFTER_ACTION();

  /** Header type literal. */
  public static class HEADER extends PropertyType<Map.Entry<String, String[]>> {}

  /** Header literal instance. */
  public static HEADER HEADER = new HEADER();

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
