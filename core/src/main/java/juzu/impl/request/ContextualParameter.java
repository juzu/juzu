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

package juzu.impl.request;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ContextualParameter extends Parameter {

  public ContextualParameter(String name, Class<?> type) throws NullPointerException {
    super(name, type);
  }

  @Override
  public ContextualArgument create(final Object value) {
    return new ContextualArgument() {
      @Override
      public Parameter getParameter() {
        return ContextualParameter.this;
      }
      @Override
      public Object getValue() {
        return value;
      }
    };
  }
}
