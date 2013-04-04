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

package juzu.test.protocol.mock;

import juzu.request.WindowContext;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class MockWindowContext implements WindowContext {

  /** . */
  private String id;

  /** . */
  private String ns;

  /** . */
  private String baseValue;

  public MockWindowContext() {
    setBaseValue("window");
  }

  public String getBaseValue() {
    return baseValue;
  }

  public void setBaseValue(String baseValue) throws NullPointerException {
    if (baseValue == null) {
      throw new NullPointerException();
    }

    //
    this.baseValue = baseValue;
    this.id = baseValue + "_id";
    this.ns = baseValue + "_ns";
  }

  public String getId() {
    return id;
  }

  public String getNamespace() {
    return ns;
  }
}
