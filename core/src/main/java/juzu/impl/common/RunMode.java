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

import juzu.PropertyType;

/**
 * The run mode affects the behavior of Juzu at runtime.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public enum RunMode {

  /**
   * Production.
   */
  PROD(false, false, true),

  /**
   * Development.
   */
  DEV(false, true, false),

  /**
   * Live mode.
   */
  LIVE(true, true, false);

  /** The run mode property. */
  public static PropertyType<RunMode> PROPERTY = new PropertyType<RunMode>(){};

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

  /** True if assets should be minified when possible. */
  final boolean minifyAssets;

  /** . */
  final String value;

  private RunMode(boolean dynamic, boolean prettyFail, boolean minifyAssets) {
    this.dynamic = dynamic;
    this.prettyFail = prettyFail;
    this.value = name().toLowerCase();
    this.minifyAssets = minifyAssets;
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

  public boolean getMinifyAssets() {
    return minifyAssets;
  }
}
