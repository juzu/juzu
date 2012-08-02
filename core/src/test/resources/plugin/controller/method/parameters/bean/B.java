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

package plugin.controller.method.parameters.bean;

import juzu.Param;

import java.util.List;

/** @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a> */
@Param
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
