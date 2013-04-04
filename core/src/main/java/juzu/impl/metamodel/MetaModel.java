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
  public ProcessingContext processingContext;

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

  public ProcessingContext getProcessingContext() {
    return processingContext;
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
