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

/**
 * The run mode affects the behavior of Juzu at runtime.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public enum RunMode {

  /**
   * Production.
   */
  PROD(false, false),

  /**
   * Development.
   */
  DEV(false, true),

  /**
   * Live mode.
   */
  LIVE(true, true);

  /** . */
  private static final RunMode[] ALL = values();

  /**
   * Parse the run mode
   *
   * @param s the string to parse
   * @return the corresponding run mode or null if none can be matched
   * @throws NullPointerException if the string argument is null
   */
  public static RunMode parse(String s) throws NullPointerException {
    if (s == null) {
      throw new NullPointerException("No null string argument accepted");
    }
    for (RunMode runMode : ALL) {
      String name = runMode.name();
      if (name.equalsIgnoreCase(s)) {
        return runMode;
      }
    }
    return null;
  }

  /** Controls if the code should be compiled during the execution of the application. */
  final boolean dynamic;

  /**
   * Controls an error should be displayed:
   * <ul>
   *   <li>true: the error should be displayed for a developer with the goal to debug and fix the problem, for instance
   *   a stack trace.</li>
   *   <li>false: the error should be displayed in a normal person, for instance an http code.</li>
   * </ul>
   */
  final boolean prettyFail;

  /** . */
  final String value;

  private RunMode(boolean dynamic, boolean prettyFail) {
    this.dynamic = dynamic;
    this.prettyFail = prettyFail;
    this.value = name().toLowerCase();
  }

  public String getValue() {
    return value;
  }

  public boolean isStatic() {
    return !dynamic;
  }

  public boolean isDynamic() {
    return dynamic;
  }

  public boolean getPrettyFail() {
    return prettyFail;
  }
}
