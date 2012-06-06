package juzu.impl.compiler;

import java.util.Formatter;
import java.util.Locale;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class CompilationMessage {

  /** . */
  private final MessageCode code;

  /** . */
  private final Object[] arguments;

  public CompilationMessage(MessageCode code, Object... arguments) {
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
