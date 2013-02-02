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

package juzu.impl.bridge.spi.servlet;

import juzu.impl.asset.AssetServer;
import juzu.impl.bridge.BridgeConfig;
import juzu.impl.common.DevClassLoader;
import juzu.impl.common.JSON;
import juzu.impl.common.Logger;
import juzu.impl.common.SimpleMap;
import juzu.impl.common.Tools;
import juzu.impl.fs.spi.ReadFileSystem;
import juzu.impl.fs.spi.disk.DiskFileSystem;
import juzu.impl.fs.spi.war.WarFileSystem;
import juzu.impl.plugin.module.Module;
import juzu.impl.plugin.module.ModuleLifeCycle;

import javax.servlet.ServletContext;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ServletModule extends Module {

  /** . */
  private static final HashMap<String, ServletModule> modules = new HashMap<String, ServletModule>();

  static synchronized ServletModule leaseModule(ServletContext context, ClassLoader classLoader) throws Exception {
    ServletModule module = modules.get(context.getContextPath());
    if (module == null) {
      modules.put(context.getContextPath(), module = new ServletModule(context, classLoader));
    }
    module.leases.incrementAndGet();
    return module;
  }

  /** . */
  final AtomicInteger leases;

  /** . */
  final ModuleLifeCycle lifeCycle;

  /** . */
  final ServletContext context;

  /** . */
  final AssetServer server;

  /** . */
  final WarFileSystem resources;

  /** . */
  final Logger log = new Logger() {
    public void log(CharSequence msg) {
      System.out.println(msg);
    }

    public void log(CharSequence msg, Throwable t) {
      System.out.println(msg);
      t.printStackTrace();
    }
  };

  private static JSON getConfig(ClassLoader loader) throws Exception {
    URL cfg = loader.getResource("juzu/config.json");
    String s = Tools.read(cfg);
    return (JSON)JSON.parse(s);
  }

  private ServletModule(final ServletContext context, ClassLoader classLoader) throws Exception {
    super(classLoader, getConfig(classLoader));

    //
    String srcPath = context.getInitParameter("juzu.src_path");
    ReadFileSystem<?> sourcePath = srcPath != null ? new DiskFileSystem(new File(srcPath)) : WarFileSystem.create(context, "/WEB-INF/src/");
    int runMode = BridgeConfig.getRunMode(new SimpleMap<String, String>() {
      @Override
      protected Iterator<String> keys() {
        return Tools.iterator(BridgeConfig.RUN_MODE);
      }

      @Override
      public String get(Object key) {
        return key.equals(BridgeConfig.RUN_MODE) ? context.getInitParameter(BridgeConfig.RUN_MODE) : null;
      }
    });

    // Build module
    ModuleLifeCycle lifeCycle;
    switch (runMode) {
      case BridgeConfig.DYNAMIC_MODE:
        lifeCycle = new ModuleLifeCycle.Dynamic(log, new DevClassLoader(Thread.currentThread().getContextClassLoader()), sourcePath);
        break;
      default:
        lifeCycle = new ModuleLifeCycle.Static(log, Thread.currentThread().getContextClassLoader(), WarFileSystem.create(context, "/WEB-INF/classes/"));
    }

    //
    AssetServer server = (AssetServer)context.getAttribute("asset.server");
    if (server == null) {
      server = new AssetServer();
      context.setAttribute("asset.server", server);
    }

    //
    this.context = context;
    this.server = server;
    this.resources = WarFileSystem.create(context, "/WEB-INF/");
    this.lifeCycle = lifeCycle;
    this.leases = new AtomicInteger();
  }

  synchronized void release() {
    if (leases.decrementAndGet() == 0) {
      modules.remove(context.getContextPath());
    }
  }
}
