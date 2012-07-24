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

package juzu.impl.template.spi;

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

  protected TemplateStub() {
    this(null);
  }

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
          throw new IllegalStateException("Template stub is not initialized");
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
