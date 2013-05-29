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
import juzu.impl.common.Tools;
import juzu.impl.fs.spi.ReadFileSystem;
import juzu.impl.fs.spi.disk.DiskFileSystem;
import juzu.impl.inject.spi.InjectorProvider;
import juzu.impl.plugin.module.Module;
import juzu.impl.plugin.module.ModuleContext;
import juzu.impl.plugin.module.ModuleLifeCycle;
import juzu.impl.resource.ClassLoaderResolver;
import juzu.impl.resource.ResourceResolver;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.HttpServerResponse;
import org.vertx.java.core.http.impl.MimeMapping;
import org.vertx.java.deploy.Container;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpCookie;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    //
    HttpServer server = vertx.createHttpServer().requestHandler(new Handler<HttpServerRequest>() {
      juzu.impl.bridge.spi.web.Handler h;

      /** . */
      Module module = null;

      /** . */
      Bridge bridge = null;

      public void handle(final HttpServerRequest req) {

        //
        if (bridge == null) {
          try {

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
            Bridge bridge = new Bridge(log, module, config, lifeCycle.getClasses(), null, new ClassLoaderResolver(lifeCycle.getClassLoader()));

            //
            this.module = module;
            this.bridge = bridge;
          }
          catch (Exception e) {
            System.out.println("Could not start");
            e.printStackTrace();
            return;
          }
        }

        //
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
        boolean served = false;
        HttpServerResponse response = req.response;
        Iterable<ResourceResolver> resolvers = bridge.application.resolveBeans(ResourceResolver.class);
        for (Iterator<ResourceResolver> i = resolvers.iterator();i.hasNext() && !served;) {
          ResourceResolver resolver = i.next();
          URL assetURL = resolver.resolve(req.path);
          if (assetURL != null) {
            served = true;
            if ("file".equals(assetURL.getProtocol())) {
              try {
                response.sendFile(new File(assetURL.toURI()).getAbsolutePath());
                break;
              }
              catch (URISyntaxException ignore) {
              }
            }
            else {
              // This is really not pretty code but for now it works
              try {
                InputStream in = assetURL.openStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                Tools.copy(in, baos);
                byte[] bytes = baos.toByteArray();
                String filename = assetURL.getPath();
                int li = filename.lastIndexOf('.');
                if (li != -1 && li != filename.length() - 1) {
                  String ext = filename.substring(li + 1, filename.length());
                  String contentType = MimeMapping.getMimeTypeForExtension(ext);
                  if (contentType != null) {
                    req.response.headers().put(HttpHeaders.Names.CONTENT_TYPE, contentType);
                  }
                }
                req.response.headers().put(HttpHeaders.Names.CONTENT_LENGTH, String.valueOf(bytes.length));
                req.response.writeBuffer(new Buffer(bytes));
                req.response.end();
              }
              catch (IOException e) {
                e.printStackTrace();
              }
              finally {
                req.response.close();
              }
            }
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

            //
            VertxWebBridge webBridge = new VertxWebBridge(bridge, Application.this, req, null, log);

            //
            webBridge.handle(h);
          }
        }
      }
    }

    ).

      listen(port);
    }

  public void stop() throws Exception {
    lifeCycle = null;
  }
}
