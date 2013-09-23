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

import juzu.Response;
import juzu.impl.bridge.Bridge;
import juzu.impl.bridge.BridgeConfig;
import juzu.impl.bridge.BridgeContext;
import juzu.impl.bridge.module.ApplicationBridge;
import juzu.impl.common.Logger;
import juzu.impl.common.Name;
import juzu.impl.common.Tools;
import juzu.impl.compiler.CompilationException;
import juzu.impl.fs.spi.ReadFileSystem;
import juzu.impl.fs.spi.disk.DiskFileSystem;
import juzu.impl.inject.spi.Injector;
import juzu.impl.inject.spi.InjectorProvider;
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
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
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
    HttpServer server = vertx.createHttpServer().requestHandler(new Handler<HttpServerRequest>() {
      juzu.impl.bridge.spi.web.Handler h;

      /** . */
      Bridge bridge = null;

      public void handle(final HttpServerRequest req) {
        String contentType = req.headers().get("Content-Type");
        if (contentType != null && contentType.startsWith("application/x-www-form-urlencoded")) {
          req.bodyHandler(new Handler<Buffer>() {
            public void handle(Buffer buffer) {
              VertxRequestContext ctx = new VertxRequestContext(req, buffer, log);
              handle2(ctx);
            }
          });
        }
        else {
          VertxRequestContext ctx = new VertxRequestContext(req, null, log);
          handle2(ctx);
        }
      }

      private void handle2(VertxRequestContext ctx) {
        if (bridge == null) {
          try {

            //
            final ResourceResolver r = new ClassLoaderResolver(loader);
/*
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

              public ModuleRuntime<?> getLifeCycle() {
                return lifeCycle;
              }

              public RunMode getRunMode() {
                return RunMode.DEV;
              }
            });
*/

            //
            Map<String, String> cfg = new HashMap<String, String>();
            cfg.put(BridgeConfig.INJECT, InjectorProvider.INJECT_GUICE.getValue());
            cfg.put(BridgeConfig.APP_NAME, main.toString());
            BridgeConfig config = new BridgeConfig(cfg);

            //
            BridgeContext context = new BridgeContext() {

              /** . */
              final ResourceResolver resolver = new ClassLoaderResolver(loader);

              /** . */
              final HashMap<String, Object> attributes = new HashMap<String, Object>();

              public ClassLoader getClassLoader() {
                return loader;
              }

              public String getInitParameter(String name) {
                if ("juzu.run_mode".equals(name)) {
                  return "live";
                }
                else {
                  return null;
                }
              }

              public ResourceResolver getResolver() {
                return resolver;
              }

              public Object getAttribute(String key) {
                return attributes.get(key);
              }

              public void setAttribute(String key, Object value) {
                if (value != null) {
                  attributes.put(key, value);
                }
                else {
                  attributes.remove(key);
                }
              }

              public ReadFileSystem<?> getClassPath() {
                throw new UnsupportedOperationException("Not supported");
              }

              public ReadFileSystem<?> getSourcePath() {
                return sourcePath;
              }

              public ReadFileSystem<?> getResourcePath() {
                return sourcePath;
              }
            };

            //
            Injector injector = config.injectorProvider.get();
            injector.bindBean(Vertx.class, null, vertx);

            //
            this.bridge = new ApplicationBridge(
                context,
                log,
                config,
                null,
                r,
                injector);
          }
          catch (CompilationException e) {
            try {
              ctx.send(e);
            }
            catch (IOException ignore) {
            }
            return;
          }
          catch (Exception e) {
            try {
              ctx.send(Response.error(e).result(), true);
            }
            catch (IOException ignore) {
            }
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
        catch (CompilationException e) {
          try {
            ctx.send(e);
          }
          catch (IOException ignore) {
          }
          return;
        }
        catch (Exception e) {
          e.printStackTrace();
          try {
            ctx.send(Response.error(e).result(), true);
          }
          catch (IOException ignore) {
          }
          return;
        }

        //
        boolean served = false;
        HttpServerResponse response = ctx.req.response;
        Iterable<ResourceResolver> resolvers = bridge.getApplication().resolveBeans(ResourceResolver.class);
        for (Iterator<ResourceResolver> i = resolvers.iterator();i.hasNext() && !served;) {
          ResourceResolver resolver = i.next();
          URL assetURL = resolver.resolve(ctx.req.path);
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
                    ctx.req.response.headers().put(HttpHeaders.Names.CONTENT_TYPE, contentType);
                  }
                }
                ctx.req.response.headers().put(HttpHeaders.Names.CONTENT_LENGTH, String.valueOf(bytes.length));
                ctx.req.response.writeBuffer(new Buffer(bytes));
                ctx.req.response.end();
              }
              catch (IOException e) {
                e.printStackTrace();
              }
              finally {
                ctx.req.response.close();
              }
            }
          }
        }

        // Send 404 code
        if (!served) {
          VertxWebBridge webBridge = new VertxWebBridge(bridge, ctx, Application.this);
          webBridge.handle(h);
        }
      }
    }).listen(port);
  }

  public void stop() throws Exception {
//    lifeCycle = null;
  }
}
