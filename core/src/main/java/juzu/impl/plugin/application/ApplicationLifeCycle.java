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

package juzu.impl.plugin.application;

import juzu.impl.asset.AssetManager;
import juzu.impl.asset.AssetServer;
import juzu.impl.common.FQN;
import juzu.impl.common.NameLiteral;
import juzu.impl.common.QN;
import juzu.impl.fs.spi.SimpleFileSystem;
import juzu.impl.fs.spi.disk.DiskFileSystem;
import juzu.impl.fs.spi.ReadFileSystem;
import juzu.impl.fs.spi.jar.JarFileSystem;
import juzu.impl.inject.spi.InjectBuilder;
import juzu.impl.inject.spi.InjectImplementation;
import juzu.impl.inject.spi.spring.SpringBuilder;
import juzu.impl.common.Logger;
import juzu.impl.plugin.application.descriptor.ApplicationDescriptor;
import juzu.impl.plugin.asset.AssetPlugin;
import juzu.impl.plugin.module.ModuleLifeCycle;
import juzu.impl.resource.ClassLoaderResolver;
import juzu.impl.resource.ResourceResolver;

import javax.portlet.PortletException;
import java.io.File;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.Collections;
import java.util.jar.JarFile;

/**
 * The application life cycle.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class ApplicationLifeCycle<P, R> {

  /** . */
  protected final Logger logger;

  /** . */
  protected QN name;

  /** . */
  protected InjectImplementation injectImplementation;

  /** . */
  protected SimpleFileSystem<R> resources;

  /** . */
  protected ApplicationContext context;

  /** . */
  protected AssetServer assetServer;

  /** . */
  protected AssetManager stylesheetManager;

  /** . */
  protected AssetManager scriptManager;

  /** . */
  protected ApplicationDescriptor descriptor;

  /** . */
  protected ApplicationBootstrap bootstrap;

  /** . */
  protected ResourceResolver resolver;

  /** . */
  private final ModuleLifeCycle<R, P> module;

  public ApplicationLifeCycle(Logger logger, ModuleLifeCycle<R, P> module) {
    this.logger = logger;
    this.module = module;
  }

  public QN getName() {
    return name;
  }

  public void setName(QN name) {
    this.name = name;
  }

  public InjectImplementation getInjectImplementation() {
    return injectImplementation;
  }

  public void setInjectImplementation(InjectImplementation injectImplementation) {
    this.injectImplementation = injectImplementation;
  }

  public SimpleFileSystem<R> getResources() {
    return resources;
  }

  public void setResources(SimpleFileSystem<R> resources) {
    this.resources = resources;
  }

  public ResourceResolver getResolver() {
    return resolver;
  }

  public void setResolver(ResourceResolver resolver) {
    this.resolver = resolver;
  }

  public ApplicationContext getContext() {
    return context;
  }

  public AssetServer getAssetServer() {
    return assetServer;
  }

  public AssetManager getScriptManager() {
    return scriptManager;
  }

  public AssetManager getStylesheetManager() {
    return stylesheetManager;
  }

  public void setAssetServer(AssetServer assetServer) {
    if (assetServer != null) {
      assetServer.register(this);
    }
    if (this.assetServer != null) {
      this.assetServer.unregister(this);
    }
    this.assetServer = assetServer;
  }

  public ApplicationDescriptor getDescriptor() {
    return descriptor;
  }

  public ModuleLifeCycle<R, P> getModule() {
    return module;
  }

  public boolean refresh() throws Exception {
    boolean changed = getModule().refresh();

    //
    if (context != null) {
      if (changed) {
        context = null;
      }
    }

    //
    if (context == null) {
      logger.log("Building application");
      doBoot();
      return true;
    } else {
      return false;
    }
  }

  protected final void doBoot() throws Exception {
    ReadFileSystem<P> classes = getModule().getClasses();

    //
    FQN fqn = new FQN(name, "Application");

    //
    Class<?> clazz = getModule().getClassLoader().loadClass(fqn.toString());
    ApplicationDescriptor descriptor = ApplicationDescriptor.create(clazz);

    // Find the juzu jar
    URL mainURL = ApplicationBootstrap.class.getProtectionDomain().getCodeSource().getLocation();
    if (mainURL == null) {
      throw new PortletException("Cannot find juzu jar");
    }
    if (!mainURL.getProtocol().equals("file")) {
      throw new PortletException("Cannot handle " + mainURL);
    }
    File file = new File(mainURL.toURI());
    ReadFileSystem<?> libs;
    if (file.isDirectory()) {
      libs = new DiskFileSystem(file);
    } else {
      libs = new JarFileSystem(new JarFile(file));
    }

    //
    InjectBuilder injectBuilder = injectImplementation.builder();
    injectBuilder.addFileSystem(classes);
    injectBuilder.addFileSystem(libs);
    injectBuilder.setClassLoader(getModule().getClassLoader());

    //
    if (injectBuilder instanceof SpringBuilder) {
      R springName = resources.getPath("spring.xml");
      if (springName != null) {
        URL configurationURL = resources.getURL(springName);
        ((SpringBuilder)injectBuilder).setConfigurationURL(configurationURL);
      }
    }

    // Bind the resolver
    ClassLoaderResolver resolver = new ClassLoaderResolver(getModule().getClassLoader());
    injectBuilder.bindBean(ResourceResolver.class, Collections.<Annotation>singletonList(new NameLiteral("juzu.resource_resolver.classpath")), resolver);
    injectBuilder.bindBean(ResourceResolver.class, Collections.<Annotation>singletonList(new NameLiteral("juzu.resource_resolver.server")), this.resolver);

    //
    ApplicationBootstrap bootstrap = new ApplicationBootstrap(
      injectBuilder,
      descriptor
    );

    //
    logger.log("Starting " + descriptor.getName());
    bootstrap.start();

    //
    AssetPlugin assetPlugin = bootstrap.getContext().getInjectionContext().get(AssetPlugin.class).get();

    //
    this.context = bootstrap.getContext();
    this.scriptManager = assetPlugin.getScriptManager();
    this.stylesheetManager = assetPlugin.getStylesheetManager();
    this.descriptor = descriptor;
    this.bootstrap = bootstrap;
  }

  public void shutdown() {
    if (bootstrap != null) {
      bootstrap.stop();
    }
  }
}
