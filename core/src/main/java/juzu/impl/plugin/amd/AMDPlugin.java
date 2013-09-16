/*
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
package juzu.impl.plugin.amd;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import juzu.PropertyType;
import juzu.asset.AssetLocation;
import juzu.impl.asset.AssetManager;
import juzu.impl.asset.AssetMetaData;
import juzu.impl.common.JSON;
import juzu.impl.plugin.PluginContext;
import juzu.impl.plugin.PluginDescriptor;
import juzu.impl.plugin.application.ApplicationPlugin;
import juzu.impl.request.Request;
import juzu.impl.request.RequestFilter;
import juzu.io.Chunk;
import juzu.io.Stream;
import juzu.io.StreamableDecorator;
import juzu.request.Phase;
import juzu.request.Result;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 * 
 */
public class AMDPlugin extends ApplicationPlugin implements RequestFilter {

  /** . */
  private Module[] defines;

  /** . */
  private Module[] requires;

  /** . */
  private AMDDescriptor descriptor;

  /** . */
  private PluginContext context;

  /** . */
  @Inject
  @Named("juzu.asset_manager.script")
  AssetManager scriptManager;

  /** . */
  @Inject
  ModuleManager manager;

  public AMDPlugin() {
    super("amd");
  }

  public ModuleManager getModuleManager() {
    return manager;
  }

  @Override
  public PluginDescriptor init(PluginContext context) throws Exception {
    JSON config = context.getConfig();
    List<ModuleMetaData.Define> defines = Collections.emptyList();
    List<ModuleMetaData.Require> requires = Collections.emptyList();

    if (config != null) {
      String packageName = config.getString("package");
      JSON definesJSON = config.getJSON("defines");
      JSON requiresJSON = config.getJSON("requires");

      if (definesJSON != null) {
        defines = loadDefines(packageName, definesJSON.getList("value", JSON.class));
      }

      if (requiresJSON != null) {
        AssetLocation defaultLocation = AssetLocation.safeValueOf(requiresJSON.getString("location"));
        if (defaultLocation == null) {
          defaultLocation = AssetLocation.APPLICATION;
        }
        requires = loadRequires(packageName, defaultLocation, requiresJSON.getList("value", JSON.class));
      }
    }

    this.descriptor = new AMDDescriptor(defines, requires);
    this.context = context;
    return descriptor;
  }

  private List<ModuleMetaData.Define> loadDefines(String packageName, List<? extends JSON> modules) throws Exception {
    List<ModuleMetaData.Define> defines = Collections.emptyList();
    if (modules != null && modules.size() > 0) {
      defines = new ArrayList<ModuleMetaData.Define>();
      for (JSON module : modules) {
        String name = module.getString("id");
        List<JSON> dependencies = (List<JSON>)module.getList("dependencies");

        //
        String value = module.getString("path");
        if (!value.startsWith("/")) {
          value = "/" + application.getPackageName().replace('.', '/') + "/" + packageName.replace('.', '/') + "/" + value;
        }

        //
        String adapter = module.getString("adapter");

        //
        ModuleMetaData.Define descriptor = new ModuleMetaData.Define(name, value, adapter);
        if (dependencies != null && !dependencies.isEmpty()) {
          for (JSON dependency : dependencies) {
            String depName = dependency.getString("id");
            String depAlias = dependency.getString("alias");
            descriptor.addDependency(new AMDDependency(depName, depAlias));
          }
        }

        defines.add(descriptor);
      }
    }
    return defines;
  }

  private List<ModuleMetaData.Require> loadRequires(String packageName, AssetLocation defaultLocation, List<? extends JSON> modules) throws Exception {
    List<ModuleMetaData.Require> defines = Collections.emptyList();
    if (modules != null && modules.size() > 0) {
      defines = new ArrayList<ModuleMetaData.Require>();
      for (JSON module : modules) {
        String name = module.getString("id");
        AssetLocation location = AssetLocation.safeValueOf(module.getString("location"));

        // We handle here location / perhaps we could handle it at compile time
        // instead?
        if (location == null) {
          location = defaultLocation;
        }

        //
        String value = module.getString("path");
        if (!value.startsWith("/") && location == AssetLocation.APPLICATION) {
          value = "/" + application.getPackageName().replace('.', '/') + "/" + packageName.replace('.', '/') + "/" + value;
        }

        //
        defines.add(new ModuleMetaData.Require(name, value, location));
      }
    }
    return defines;
  }

  @PostConstruct
  public void start() throws Exception {
    URL requirejsURL = AMDPlugin.class.getClassLoader().getResource("juzu/impl/plugin/amd/require.js");
    if (requirejsURL == null) {
      throw new Exception("Not found require.js");
    }

    //
    URL wrapperjsURL = AMDPlugin.class.getClassLoader().getResource("juzu/impl/plugin/amd/wrapper.js");
    if (wrapperjsURL == null) {
      throw new Exception("Not found wrapper.js");
    }

    //
    scriptManager.addAsset(new AssetMetaData("juzu.amd", AssetLocation.APPLICATION, "/juzu/impl/plugin/amd/require.js"),
        requirejsURL);
    scriptManager.addAsset(new AssetMetaData("juzu.amd.wrapper", AssetLocation.APPLICATION, "/juzu/impl/plugin/amd/wrapper.js"),
        wrapperjsURL);

    //
    this.defines = process(descriptor.getDefines(), manager);
    this.requires = process(descriptor.getRequires(), manager);
  }

  private Module[] process(List<? extends ModuleMetaData> modules, ModuleManager manager) throws Exception {
    ArrayList<Module> assets = new ArrayList<Module>();
    for (ModuleMetaData module : modules) {

      // Validate assets
      AssetLocation location = module.getLocation();
      URL url;
      if (location == AssetLocation.APPLICATION) {
        String path = module.getPath();
        url = context.getApplicationResolver().resolve(path);
        if (url == null) {
          throw new Exception("Could not resolve application asset " + module.getPath());
        }
      } else if (location == AssetLocation.SERVER) {
        if (!module.getPath().startsWith("/")) {
          url = context.getServerResolver().resolve("/" + module.getPath());
          if (url == null) {
            throw new Exception("Could not resolve server asset " + module.getPath());
          }
        } else {
          url = null;
        }
      } else {
        url = null;
      }

      //
      Module id = manager.addAMD(module, url);
      assets.add(id);
    }

    //
    return assets.toArray(new Module[assets.size()]);
  }

  public void invoke(Request request) {
    request.invoke();

    //
    if (request.getPhase() == Phase.VIEW) {
      Result result = request.getResult();
      if (result instanceof Result.Status) {
        Result.Status status = (Result.Status)result;
        if (status.decorated && (defines.length > 0 || requires.length > 0)) {
          status = new Result.Status(status.code, true, new StreamableDecorator(status.streamable) {
            @Override
            protected void sendHeader(Stream consumer) {
              consumer.provide(new Chunk.Property<String>("juzu.amd", PropertyType.SCRIPT));
              consumer.provide(new Chunk.Property<String>("juzu.amd.wrapper", PropertyType.SCRIPT));
              for (Module define : defines) {
                consumer.provide(new Chunk.Property<Module>(define, Module.TYPE));
              }
              for (Module require : requires) {
                consumer.provide(new Chunk.Property<Module>(require, Module.TYPE));
              }
            }
          });
        }
        request.setResult(status);
      }
    }
  }
}
