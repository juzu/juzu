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

package juzu.impl.common;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class TrimmingException extends Exception {

  public static void invoke(Callback callback) throws TrimmingException {
    try {
      callback.call();
    }
    catch (Throwable t) {
      throw new TrimmingException(t);
    }
  }

  /** . */
  private final String toString;

  /** . */
  private final Throwable source;

  private TrimmingException(Throwable t) {
    this(t, new Exception().getStackTrace().length);
  }

  private TrimmingException(Throwable t, int toTrim) {
    // Trim the trace
    StackTraceElement[] trace = t.getStackTrace();
    StackTraceElement[] trimmed = new StackTraceElement[trace.length - toTrim];
    System.arraycopy(trace, 0, trimmed, 0, trimmed.length);
    setStackTrace(trimmed);

    //
    this.toString = t.toString();
    this.source = t;

    // Recursively build the causes
    Throwable cause = t.getCause();
    if (cause != null) {
      initCause(new TrimmingException(cause, toTrim));
    }
  }

  public Throwable getSource() {
    return source;
  }

  @Override
  public String toString() {
    return toString;
  }

  public static interface Callback {
    void call() throws Throwable;
  }
}
