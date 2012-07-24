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
