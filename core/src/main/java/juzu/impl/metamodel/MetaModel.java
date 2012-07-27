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

package juzu.impl.metamodel;

import juzu.impl.compiler.ProcessingContext;
import juzu.impl.compiler.ProcessingException;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class MetaModel<P extends MetaModelPlugin<M, P>, M extends MetaModel<P, M>> extends MetaModelObject {

  /** . */
  public ProcessingContext env;

  /** . */
  protected final EventQueue dispatch = new EventQueue();

  /** . */
  protected final EventQueue queue = new EventQueue();

  /** . */
  boolean forward = false;

  /** . */
  protected MetaModelContext<P, M> context;

  @Override
  public final void queue(MetaModelEvent event) {
//    if (!queuing) {
//      throw new IllegalStateException("Not queueing");
//    }

    // Enqueue locally
    queue.queue(event);
    dispatch.queue(event);

    // Enqueue to parent context if any
    if (metaModel != null) {
      metaModel.queue(event);
    }
  }

  public void init(ProcessingContext env) {}

  public Set<Class<? extends Annotation>> getSupportedAnnotations() { return Collections.emptySet(); }

  public void postActivate(ProcessingContext env) {}

  public void prePassivate() {}

  public void postProcessAnnotations() throws ProcessingException {}

  public void processEvents() {}

  public void postProcessEvents() {}

  public final EventQueue getQueue() {
    return queue;
  }
}
