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

package juzu.impl.plugin.application.descriptor;

import juzu.impl.plugin.ServiceDescriptor;
import juzu.impl.inject.BeanDescriptor;
import juzu.impl.common.JSON;
import juzu.impl.common.Tools;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ApplicationDescriptor extends ServiceDescriptor {

  /**
   * Encapsulate application descriptor loading from an application name.
   *
   * @param loader the application loader
   * @param applicationName the application name
   * @return the descriptor
   * @throws Exception any exception that would prevent the exception to be loaded
   */
  public static ApplicationDescriptor create(ClassLoader loader, String applicationName) throws Exception {
    if (loader == null) {
      throw new NullPointerException("No null loader accepted");
    }
    // Application class
    Class<?> applicationClass = loader.loadClass(applicationName + ".Application");
    return new ApplicationDescriptor(loader, applicationClass);
  }

  /**
   * Encapsulate application descriptor loading from an application name.
   *
   * @param applicationClass the application class
   * @return the descriptor
   * @throws Exception any exception that would prevent the exception to be loaded
   */
  public static ApplicationDescriptor create(Class<?> applicationClass) throws Exception {
    return new ApplicationDescriptor(applicationClass.getClassLoader(), applicationClass);
  }

  /** . */
  private final Class<?> applicationClass;

  /** . */
  private final String packageName;

  /** . */
  private final String name;

  /** . */
  private final Class<?> packageClass;

  /** . */
  private final JSON config;

  /** . */
  private final ClassLoader loader;

  private ApplicationDescriptor(ClassLoader loader, Class<?> applicationClass) throws Exception {


    // Load config
    JSON config;
    InputStream in = null;
    try {
      String configPath = applicationClass.getPackage().getName().replace('.', '/') + "/config.json";
      in = loader.getResourceAsStream(configPath);
      String s = Tools.read(in);
      config = (JSON)JSON.parse(s);
    }
    catch (IOException e) {
      throw new AssertionError(e);
    }
    finally {
      Tools.safeClose(in);
    }

    // Init this first before initing plugin so they can use it
    this.applicationClass = applicationClass;
    this.name = applicationClass.getSimpleName();
    this.packageName = applicationClass.getPackage().getName();
    this.packageClass = Tools.getPackageClass(applicationClass.getClassLoader(), applicationClass.getPackage().getName());
    this.loader = loader;
    this.config = config;
  }

  public JSON getConfig() {
    return config;
  }

  @Override
  public Iterable<BeanDescriptor> getBeans() {
    return Collections.emptyList();
  }

  public Class<?> getPackageClass() {
    return packageClass;
  }

  public Class<?> getApplicationClass() {
    return applicationClass;
  }

  public ClassLoader getApplicationLoader() {
    return loader;
  }

  public String getPackageName() {
    return packageName;
  }

  public String getName() {
    return name;
  }
}
