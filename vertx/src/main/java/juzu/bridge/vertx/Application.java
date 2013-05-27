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

package juzu.bridge.vertx;

import juzu.impl.bridge.Bridge;
import juzu.impl.bridge.BridgeConfig;
import juzu.impl.common.Content;
import juzu.impl.common.JSON;
import juzu.impl.common.Logger;
import juzu.impl.common.Name;
import juzu.impl.common.RunMode;
import juzu.impl.fs.spi.ReadFileSystem;
import juzu.impl.fs.spi.disk.DiskFileSystem;
import juzu.impl.inject.spi.InjectorProvider;
import juzu.impl.plugin.module.Module;
import juzu.impl.plugin.module.ModuleContext;
import juzu.impl.plugin.module.ModuleLifeCycle;
import juzu.impl.resource.ClassLoaderResolver;
import juzu.impl.resource.ResourceResolver;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.HttpServerResponse;
import org.vertx.java.deploy.Container;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class Application {

  /** . */
  private final Container container;

  /** . */
  private final Vertx vertx;

  /** . */
  private final ClassLoader loader;

  /** . */
  private final DiskFileSystem sourcePath;

  /** . */
  private final Name main;

  /** . */
  private ModuleLifeCycle.Dynamic<File> lifeCycle;

  /** . */
  final int port;

  Application(
      Container container,
      Vertx vertx,
      ClassLoader loader,
      DiskFileSystem sourcePath,
      Name main,
      int port) {

    //
    this.container = container;
    this.vertx = vertx;
    this.loader = loader;
    this.sourcePath = sourcePath;
    this.main = main;
    this.lifeCycle = null;
    this.port = port;
  }

  public void start() throws Exception {

    //
    final Logger log = new Logger() {
      final org.vertx.java.core.logging.Logger logger = container.getLogger();

      public void log(CharSequence msg) {
        logger.info(msg);
      }

      public void log(CharSequence msg, Throwable t) {
        logger.info(msg, t);
      }
    };

    //
    lifeCycle = new ModuleLifeCycle.Dynamic<File>(
        log,
        loader,
        sourcePath
    );

    //
    lifeCycle.refresh(true);

    //
    final ResourceResolver r = new ClassLoaderResolver(loader);
    Module module = new Module(new ModuleContext() {
      public ClassLoader getClassLoader() {
        return loader;
      }
      public JSON getConfig() throws Exception {
        ReadFileSystem<String[]> c = lifeCycle.getClasses();
        Content f = c.getContent(new String[]{"juzu", "config.json"}).getObject();
        return (JSON)JSON.parse(f.getCharSequence().toString());
      }
      public ResourceResolver getServerResolver() {
        return r;
      }
      public ReadFileSystem<?> getResourcePath() {
        throw new UnsupportedOperationException("?");
      }
      public ModuleLifeCycle<?> getLifeCycle() {
        return lifeCycle;
      }
      public RunMode getRunMode() {
        return RunMode.DEV;
      }
    });

    //
    Map<String,String> cfg = new HashMap<String, String>();
    cfg.put(BridgeConfig.INJECT, InjectorProvider.INJECT_GUICE.getValue());
    cfg.put(BridgeConfig.APP_NAME, main.toString());
    BridgeConfig config = new BridgeConfig(cfg);

    // Bind vertx singleton
    config.injectImpl.bindBean(Vertx.class, null, vertx);

    //
    final Bridge bridge = new Bridge(log, module, config, lifeCycle.getClasses(), null, new ClassLoaderResolver(lifeCycle.getClassLoader()));

    //
    HttpServer server = vertx.createHttpServer().requestHandler(new Handler<HttpServerRequest>() {
      juzu.impl.bridge.spi.web.Handler h;

      public void handle(final HttpServerRequest req) {
        try {
          if (bridge.refresh(true)) {
            h = null;
          }
          if (h == null) {
            h = new juzu.impl.bridge.spi.web.Handler(bridge);
          }
        }
        catch (Exception e) {
          e.printStackTrace();
          return;
        }

        //
        HttpServerResponse response = req.response;
        URL assetURL = bridge.application.getScriptManager().resolveAsset(req.path);
        if (assetURL == null) {
          assetURL = bridge.application.getStylesheetManager().resolveAsset(req.path);
        }
        boolean served = false;
        if (assetURL != null && "file".equals(assetURL.getProtocol())) {
          try {
            response.sendFile(new File(assetURL.toURI()).getAbsolutePath());
            served = true;
          }
          catch (URISyntaxException ignore) {
          }
        }

        // Send 404 code
        if (!served) {
          String contentType = req.headers().get("Content-Type");
          if (contentType != null && contentType.startsWith("application/x-www-form-urlencoded")) {
            req.bodyHandler(new Handler<Buffer>() {
              public void handle(Buffer buffer) {
                new VertxWebBridge(bridge, Application.this, req, buffer, log).handle(h);
              }
            });
          }
          else {
            new VertxWebBridge(bridge, Application.this, req, null, log).handle(h);
          }
        }
      }
    }).listen(port);
  }

  public void stop() throws Exception {
    lifeCycle = null;
  }
}
