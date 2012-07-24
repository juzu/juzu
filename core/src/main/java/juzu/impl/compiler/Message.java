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

package juzu.impl.compiler;

import java.util.Formatter;
import java.util.Locale;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class Message {

  /** . */
  private final MessageCode code;

  /** . */
  private final Object[] arguments;

  public Message(MessageCode code, Object... arguments) {
    this.code = code;
    this.arguments = arguments;
  }

  public MessageCode getCode() {
    return code;
  }

  public Object[] getArguments() {
    return arguments;
  }

  public StringBuilder format(StringBuilder sb, boolean formal) {
    if (formal) {
      sb = sb.append("[").append(code.getKey()).append("](");
      for (int i = 0;i < arguments.length;i++) {
        if (i > 0) {
          sb.append(',');
        }
        sb.append(String.valueOf(arguments[i]));
      }
      sb.append(")");
    }
    else {
      new Formatter(sb).format(Locale.getDefault(), code.getMessage(), arguments).flush();
    }
    return sb;
  }

  public String toFormalString() {
    return format(new StringBuilder(), true).toString();
  }

  public String toDisplayString() {
    return format(new StringBuilder(), false).toString();
  }

  public String toString() {
    return format(new StringBuilder("CompilationMessage["), false).append("]").toString();
  }
}
