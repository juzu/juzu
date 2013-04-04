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
