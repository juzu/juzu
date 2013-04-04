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

import java.io.Serializable;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class MethodInvocation implements Serializable {

  /** . */
  private final String className;

  /** . */
  private final String methodName;

  /** . */
  private final List<String> methodArguments;

  public MethodInvocation(String className, String methodName, List<String> methodArguments) {
    this.className = className;
    this.methodName = methodName;
    this.methodArguments = methodArguments;
  }

  public String getClassName() {
    return className;
  }

  public String getMethodName() {
    return methodName;
  }

  public List<String> getMethodArguments() {
    return methodArguments;
  }
}
