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

package juzu.plugin.less.impl.lesser;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class Compilation extends Result {

  /** . */
  private final String value;

  public Compilation(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("Compilation[");
    if (value.length() < 40) {
      sb.append(value);
    }
    else {
      sb.append(value, 0, 40).append("...");
    }
    sb.append("]");
    return sb.toString();
  }
}
