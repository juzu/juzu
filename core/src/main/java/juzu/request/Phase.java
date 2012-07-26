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

package juzu.request;

import juzu.Action;
import juzu.Resource;
import juzu.View;

import java.lang.annotation.Annotation;

/**
 * A phase.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public enum Phase {

  /** Action phase. */
  ACTION(Action.class) {
    @Override
    public String id(Annotation annotation) throws ClassCastException {
      return ((Action)annotation).id();
    }
  },

  /** View phase. */
  VIEW(View.class) {
    @Override
    public String id(Annotation annotation) throws ClassCastException {
      return ((View)annotation).id();
    }
  },

  /** Resource phase. */
  RESOURCE(Resource.class) {
    @Override
    public String id(Annotation annotation) throws ClassCastException {
      return ((Resource)annotation).id();
    }
  };

  /** . */
  public final Class<? extends Annotation> annotation;

  Phase(Class<? extends Annotation> annotation) {
    this.annotation = annotation;
  }

  public abstract String id(Annotation annotation) throws ClassCastException;
}
