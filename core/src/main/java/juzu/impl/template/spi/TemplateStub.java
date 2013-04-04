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

package juzu.impl.template.spi;

import juzu.impl.common.Logger;
import juzu.template.TemplateExecutionException;
import juzu.template.TemplateRenderContext;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The stub for a template.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public abstract class TemplateStub {

  /** . */
  private static final int CONSTRUCTED = 0;

  /** . */
  private static final int INITIALIZING = 1;

  /** . */
  private static final int INITIALIZED = 2;

  /** . */
  protected final String id;

  /** . */
  private final AtomicInteger status;

  protected TemplateStub(String id) {

    if (id == null) {
      id = getClass().getName().substring(0, getClass().getName().length() - 1); // Remove trailing _;
    }

    this.id = id;
    this.status = new AtomicInteger(CONSTRUCTED);
  }

  public String getId() {
    return id;
  }

  /**
   * Initialize the stub with the associated classloader.
   *
   * @param loader the class loader
   * @throws NullPointerException if the loader argument is null
   */
  public final void init(ClassLoader loader) throws NullPointerException {
    if (loader == null) {
      throw new NullPointerException("No null loader accepted");
    }
    while (true) {
      switch (status.get()) {
        case CONSTRUCTED:
          if (status.compareAndSet(CONSTRUCTED, INITIALIZING)) {
            int next = CONSTRUCTED;
            try {
              doInit(loader);
              next = INITIALIZED;
            }
            finally {
              status.set(next);
            }
          }
          break;
        case INITIALIZING:
          break;
        default:
          return;
      }
    }
  }

  /**
   * Renders the template.
   *
   * @param renderContext the render context
   * @throws TemplateExecutionException any execution exception
   * @throws IOException any io exception
   * @throws IllegalStateException if the stub is not initialized
   */
  public final void render(TemplateRenderContext renderContext) throws TemplateExecutionException, IOException, IllegalStateException {
    while (true) {
      switch (status.get()) {
        case INITIALIZING:
          // Wait
          break;
        case INITIALIZED:
          doRender(renderContext);
          return;
        default:
          throw new IllegalStateException("Template stub is not initialized " + status.get());
      }
    }
  }

  /**
   * Init the template with the associated resource.
   *
   * @param loader   the class loader
   */
  protected abstract void doInit(ClassLoader loader);

  /**
   * Performs template rendering.
   *
   * @param renderContext the render context
   * @throws TemplateExecutionException any execution exception
   * @throws IOException any io exception
   */
  protected abstract void doRender(TemplateRenderContext renderContext)
      throws TemplateExecutionException, IOException;

}
