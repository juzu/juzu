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

package juzu.impl.bridge;

import juzu.Response;
import juzu.impl.bridge.spi.EventBridge;
import juzu.impl.common.Formatting;
import juzu.impl.plugin.application.ApplicationLifeCycle;
import juzu.impl.asset.AssetServer;
import juzu.impl.bridge.spi.ActionBridge;
import juzu.impl.bridge.spi.RenderBridge;
import juzu.impl.bridge.spi.RequestBridge;
import juzu.impl.bridge.spi.ResourceBridge;
import juzu.impl.compiler.CompilationError;
import juzu.impl.compiler.CompilationException;
import juzu.impl.fs.spi.ReadFileSystem;
import juzu.impl.common.Logger;
import juzu.impl.common.Tools;
import juzu.impl.common.TrimmingException;
import juzu.impl.plugin.module.ModuleLifeCycle;
import juzu.impl.resource.ResourceResolver;

import java.io.Closeable;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class Bridge implements Closeable {

  /** . */
  private final Logger log;

  /** . */
  private final AssetServer server;

  /** . */
  private final BridgeConfig config;

  /** . */
  private final ReadFileSystem<?> resources;

  /** . */
  private final ResourceResolver resolver;

  /** . */
  private final ModuleLifeCycle module;

  /** . */
  public ClassLoader classLoader;

  /** . */
  public ApplicationLifeCycle application;

  public Bridge(Logger log, ModuleLifeCycle module, BridgeConfig config, ReadFileSystem<?> resources, AssetServer server, ResourceResolver resolver) {
    this.log = log;
    this.module = module;
    this.config = config;
    this.resources = resources;
    this.server = server;
    this.resolver = resolver;
  }

  public BridgeConfig getConfig() {
    return config;
  }

  public void refresh() throws Exception {

    if (application == null) {
      application = new ApplicationLifeCycle(
          log,
          module,
          config.injectImpl,
          config.name,
          resources,
          server,
          resolver);
    }

    //
    application.refresh();
  }

  public void invoke(RequestBridge requestBridge) throws Throwable {
    if (requestBridge instanceof  ActionBridge) {
      processAction((ActionBridge)requestBridge);
    } else if (requestBridge instanceof RenderBridge) {
      render((RenderBridge)requestBridge);
    } else if (requestBridge instanceof ResourceBridge) {
      serveResource((ResourceBridge)requestBridge);
    } else {
      throw new AssertionError();
    }
  }

  public void processAction(final ActionBridge requestBridge) throws Throwable {
    try {
      application.getApplication().invoke(requestBridge);
    }
    finally {
      requestBridge.close();
    }
  }

  public void processEvent(final EventBridge requestBridge) throws Throwable {
    try {
      application.getApplication().invoke(requestBridge);
    }
    finally {
      requestBridge.close();
    }
  }

  public void render(final RenderBridge requestBridge) throws Throwable {

    //
    Collection<CompilationError> errors = null;
    try {
      refresh();
    }
    catch (CompilationException e) {
      errors = e.getErrors();
    }

    //
    if (errors == null || errors.isEmpty()) {

      //
      if (errors != null) {
        requestBridge.purgeSession();
      }

      //
      try {
        application.getApplication().invoke(requestBridge);
      } finally {
        requestBridge.close();
      }
/*      catch (TrimmingException e) {
        if (config.isProd()) {
          throw e.getSource();
        }
        else {
          StringWriter writer = new StringWriter();
          PrintWriter printer = new PrintWriter(writer);
          Formatting.renderStyleSheet(printer);
          Formatting.renderThrowable(printer, e);
          requestBridge.setResponse(Response.content(503, writer.getBuffer()));
        }
      }*/
    }
    else {
      try {
        StringWriter writer = new StringWriter();
        PrintWriter printer = new PrintWriter(writer);
        Formatting.renderErrors(printer, errors);
        requestBridge.setResponse(Response.ok(writer.getBuffer()));
      }
      finally {
        requestBridge.close();
      }
    }
  }

  public void serveResource(final ResourceBridge requestBridge) throws Throwable{
    try {
      application.getApplication().invoke(requestBridge);
    } finally {
      requestBridge.close();
    }
/*    catch (TrimmingException e) {

      //
      logThrowable(e);

      // Internal server error
      Response response;
      if (!config.isProd()) {
        StringWriter writer = new StringWriter();
        PrintWriter printer = new PrintWriter(writer);
        printer.print("<html>\n");
        printer.print("<head>\n");
        printer.print("</head>\n");
        printer.print("<body>\n");
        Formatting.renderStyleSheet(printer);
        Formatting.renderThrowable(printer, e);
        printer.print("</body>\n");
        response = Response.content(503, writer.getBuffer());
      } else {
        response = Response.content(500, "todo");
      }

      // Set response
      requestBridge.setResponse(response);
    }*/
  }

  private void logThrowable(Throwable t) {
    log.log(t.getMessage(), t);
  }

  public void close() {
    Tools.safeClose(application);
  }
}
