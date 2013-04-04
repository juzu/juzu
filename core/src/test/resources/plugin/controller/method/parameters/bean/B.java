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

package plugin.controller.method.parameters.bean;

import juzu.Mapped;

import java.util.List;

/** @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a> */
@Mapped
public class B {
  private String a;
  private String[] b;
  private List<String> c;

  public String d;
  public String[] e;
  public List<String> f;

  public String getA() {
    return a;
  }

  public void setA(final String a) {
    this.a = a;
  }

  public String[] getB() {
    return b;
  }

  public void setB(final String[] b) {
    this.b = b;
  }

  public List<String> getC() {
    return c;
  }

  public void setC(final List<String> c) {
    this.c = c;
  }
}
