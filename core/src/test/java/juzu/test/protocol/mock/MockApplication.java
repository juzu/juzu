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

package juzu.test.protocol.mock;

import juzu.impl.common.Name;
import juzu.impl.common.Tools;
import juzu.impl.fs.spi.ReadWriteFileSystem;
import juzu.impl.inject.spi.InjectorProvider;
import juzu.impl.plugin.application.Application;
import juzu.impl.runtime.ApplicationRuntime;
import juzu.impl.fs.spi.ReadFileSystem;
import juzu.impl.bridge.spi.RequestBridge;
import juzu.impl.common.Logger;
import juzu.impl.plugin.controller.ControllerPlugin;
import juzu.impl.runtime.ModuleRuntime;
import juzu.impl.resource.ResourceResolver;
import juzu.request.ApplicationContext;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class MockApplication<P> implements Closeable, ApplicationContext {

  /** . */
  private static final ResourceBundle.Control control = new ResourceBundle.Control() {};

  /** . */
  private final HashMap<Locale, ResourceBundleImpl> bundles = new HashMap<Locale, ResourceBundleImpl>();

  /** . */
  final ClassLoader classLoader;

  /** . */
  private final ApplicationRuntime<P, ?> lifeCycle;

  /** . */
  private final ReadWriteFileSystem<P> classes;

  public <L> MockApplication(
      ReadWriteFileSystem<P> classes,
      ClassLoader classLoader,
      InjectorProvider implementation,
      Name name) throws Exception {

    //
    ModuleRuntime<P> module = new ModuleRuntime.Static<P>(Logger.SYSTEM, classLoader, classes);

    //
    ApplicationRuntime<P, P> lifeCycle = new ApplicationRuntime<P, P>(
        Logger.SYSTEM,
        module,
        implementation.get(),
        name,
        null,
        new ResourceResolver() {
          public URL resolve(String uri) {
            return null;
          }
        });

    //
    this.classes = classes;
    this.classLoader = classLoader;
    this.lifeCycle = lifeCycle;
  }

  public ReadFileSystem<P> getClasses() {
    return classes;
  }

  public MockApplication<P> init() throws Exception {
    lifeCycle.refresh();
    return this;
  }

  public ApplicationRuntime<P, ?> getLifeCycle() {
    return lifeCycle;
  }

  public Application getContext() {
    return lifeCycle.getApplication();
  }

  void invoke(RequestBridge bridge) {
    lifeCycle.resolveBean(ControllerPlugin.class).invoke(bridge);
  }

  public MockClient client() {
    return new MockClient(this);
  }

  public void close() throws IOException {
    Tools.safeClose(lifeCycle);
  }

  public ResourceBundle resolveBundle(Locale locale) {
    ResourceBundle bundle = null;
    for (Locale current = locale;current != null;current = control.getFallbackLocale("whatever", current)) {
      bundle = bundles.get(current);
      if (bundle != null) {
        break;
      }
    }
    return bundle;
  }

  public void addMessage(Locale locale, String key, String value) {
    ResourceBundleImpl bundle = bundles.get(locale);
    if (bundle == null) {
      bundles.put(locale, bundle = new ResourceBundleImpl());
    }
    bundle.messages.put(key, value);
  }

  private static class ResourceBundleImpl extends ResourceBundle {

    /** . */
    private final HashMap<String, String> messages = new HashMap<String, String>();

    @Override
    protected Object handleGetObject(String key) {
      return messages.get(key);
    }

    @Override
    public Enumeration<String> getKeys() {
      return Collections.enumeration(messages.keySet());
    }
  }
}
