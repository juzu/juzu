/*
 * Copyright (C) 2010 eXo Platform SAS.
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

package juzu.impl.router;

/**
 * Specifies how a string value should be encoded in an URL.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public enum EncodingMode {

  /**
   * FORM encodes the whole string with the <code>x-www-form-urlencoded</code> also known as <i>default form
   * encoding</i>. For instance the string "/a/b" is encoded to "%2Fa%2Fb".
   */
  FORM,

  /**
   * PRESERVE_PATH encodes the whole string like the {@link #FORM} but preserve the path separators. For instance the
   * string "/a b" is enocded to "/a+b".
   */
  PRESERVE_PATH

}
