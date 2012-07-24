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

package juzu.impl.template.spi.juzu.dialect.gtmpl;

import juzu.io.CharArray;

/**
 * This object encapsulate the generation of a method that outputs the specified text.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
class TextConstant {

  /** . */
  final String name;

  /** . */
  final String text;

  TextConstant(String name, String text) {
    this.name = name;
    this.text = text;
  }

  String getDeclaration() {
    StringBuilder sb = new StringBuilder("");
    juzu.impl.common.Tools.escape(text, sb);
    return "public static final " + CharArray.Simple.class.getName() + " " + name + " = new " + CharArray.Simple.class.getName() + "('" + sb + "');";
  }
}
