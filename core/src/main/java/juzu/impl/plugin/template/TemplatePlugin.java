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

package juzu.impl.plugin.template;

import juzu.impl.plugin.PluginDescriptor;
import juzu.impl.plugin.PluginContext;
import juzu.impl.plugin.application.Application;
import juzu.impl.plugin.application.ApplicationPlugin;
import juzu.impl.plugin.template.metadata.TemplateDescriptor;
import juzu.impl.template.spi.TemplateStub;
import juzu.impl.plugin.template.metadata.TemplatesDescriptor;
import juzu.impl.common.Path;

import javax.inject.Inject;
import java.lang.reflect.Constructor;
import java.util.concurrent.ConcurrentHashMap;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class TemplatePlugin extends ApplicationPlugin {

  /** . */
  private TemplatesDescriptor descriptor;

  /** . */
  private final ConcurrentHashMap<Path, TemplateStub> stubs;

  @Inject
  Application application;

  public TemplatePlugin() {
    super("template");

    //
    this.stubs = new ConcurrentHashMap<Path, TemplateStub>();
  }

  public TemplatesDescriptor getDescriptor() {
    return descriptor;
  }

  @Override
  public PluginDescriptor init(PluginContext context) throws Exception {
    return descriptor = new TemplatesDescriptor(context.getClassLoader(), context.getConfig());
  }

  public TemplateStub resolveTemplateStub(String path) {
    return resolveTemplateStub(juzu.impl.common.Path.parse(path));
  }

  public TemplateStub resolveTemplateStub(juzu.impl.common.Path path) {
    TemplateStub stub = stubs.get(path);
    if (stub == null) {

      //
      TemplateDescriptor desc = descriptor.getTemplate(path.getCanonical());
      //
      try {
        Constructor ctor = desc.getStubType().getConstructor(String.class);
        stub = (TemplateStub)ctor.newInstance(desc.getType().getName());
      }
      catch (Exception e) {
        throw new UnsupportedOperationException("Handle me gracefully", e);
      }

      //
      TemplateStub phantom = stubs.putIfAbsent(path, stub);
      if (phantom != null) {
        stub = phantom;
      } else {
        stub.init(application.getClassLoader());
      }
    }

    //
    return stub;
  }
}
