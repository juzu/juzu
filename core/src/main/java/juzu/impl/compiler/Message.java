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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class Message {

  /** . */
  static final Pattern PATTERN = Pattern.compile("\\[" + "([^\\]]+)" + "\\]\\(" + "(.*)" + "\\)$", Pattern.DOTALL);

  /** . */
  static final Pattern SPLIT_PATTERN = Pattern.compile("(?<!\\\\),");

  public static Message parse(String s) {
    List<String> arguments = Collections.emptyList();
    Matcher matcher = PATTERN.matcher(s);
    if (matcher.find()) {
      String codeKey = matcher.group(1);
      MessageCode code = MessageCode.decode(codeKey);
      if (matcher.group(2).length() > 0) {
        arguments = new ArrayList<String>();
        for (String argument : SPLIT_PATTERN.split(matcher.group(2), 0)) {
          int prev = 0;
          while (true) {
            int pos = argument.indexOf('\\', prev);
            if (pos == -1) {
              break;
            } else {
              argument = argument.substring(0, pos) + argument.charAt(pos + 1) + argument.substring(pos + 2);
              prev = pos + 2;
            }
          }
          arguments.add(argument);
        }
      }
      return new Message(code, arguments.toArray());
    } else {
      return null;
    }
  }

  /** . */
  private final MessageCode code;

  /** . */
  private final String[] arguments;

  public Message(MessageCode code, Object... arguments) {

    String[] array = new String[arguments.length];
    for (int i = 0;i < arguments.length;i++) {
      array[i] = String.valueOf(arguments[i]);
    }

    //
    this.code = code;
    this.arguments = array;
  }

  public MessageCode getCode() {
    return code;
  }

  public String[] getArguments() {
    return arguments;
  }

  public StringBuilder format(StringBuilder sb, boolean formal) {
    if (formal) {
      sb = sb.append("[").append(code.getKey()).append("](");
      for (int i = 0;i < arguments.length;i++) {
        if (i > 0) {
          sb.append(',');
        }
        String value = String.valueOf(arguments[i]);
        for (int j = 0;j < value.length();j++) {
          char c = value.charAt(j);
          switch (c) {
            case ',':
            case ')':
            case '\\':
              sb.append('\\');
            default:
              sb.append(c);
          }
        }
      }
      sb.append(")");
    }
    else {
      new Formatter(sb).format(Locale.getDefault(), code.getMessage(), (Object[])arguments).flush();
    }
    return sb;
  }

  public String format() {
    return format(new StringBuilder(), true).toString();
  }

  public String toDisplayString() {
    return format(new StringBuilder(), false).toString();
  }

  public String toString() {
    return format(new StringBuilder("CompilationMessage["), false).append("]").toString();
  }
}
