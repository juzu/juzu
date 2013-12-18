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
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import juzu.PropertyType;
import juzu.asset.AssetLocation;
import juzu.impl.asset.Asset;
import juzu.impl.asset.AssetManager;
import juzu.impl.plugin.PluginContext;
import juzu.impl.plugin.PluginDescriptor;
import juzu.impl.plugin.application.ApplicationPlugin;
import juzu.impl.plugin.asset.AssetPlugin;
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
public class AmdPlugin extends ApplicationPlugin implements RequestFilter {

  /** . */
  private Module[] modules;

  /** . */
  private PluginContext context;

  /** . */
  @Inject
  AssetManager assetManager;

  /** . */
  @Inject
  ModuleManager manager;

  /** Force load of assets to avoid lazy load of the asset plugin (and thus population of the asset manager). */
  @Inject
  AssetPlugin assetPlugin;

  public AmdPlugin() {
    super("amd");
  }

  public ModuleManager getModuleManager() {
    return manager;
  }

  @Override
  public PluginDescriptor init(PluginContext context) throws Exception {
    this.context = context;
    return new AmdDescriptor();
  }

  @PostConstruct
  public void start() throws Exception {
    URL requirejsURL = AmdPlugin.class.getClassLoader().getResource("juzu/impl/plugin/amd/require.js");
    if (requirejsURL == null) {
      throw new Exception("Not found require.js");
    }

    //
    assetManager.addAsset("juzu.amd", "asset", AssetLocation.APPLICATION, "/juzu/impl/plugin/amd/require.js", requirejsURL);

    //
    this.modules = process("module", manager);
  }

  private Module[] process(String type, ModuleManager manager) throws Exception {
    ArrayList<Module> assets = new ArrayList<Module>();
    for (Map.Entry<String, Asset> m : assetManager.getAssets(type).entrySet()) {
      Asset asset = m.getValue();
      // Validate assets
      AssetLocation location = asset.getLocation();
      URL url;
      if (location == AssetLocation.APPLICATION) {
        String path = asset.getURI();
        url = context.getApplicationResolver().resolve(path);
        if (url == null) {
          throw new Exception("Could not resolve application asset " + asset.getURI());
        }
      } else if (location == AssetLocation.SERVER) {
        if (!asset.getURI().startsWith("/")) {
          url = context.getServerResolver().resolve("/" + asset.getURI());
          if (url == null) {
            throw new Exception("Could not resolve server asset " +asset.getURI());
          }
        } else {
          url = null;
        }
      } else {
        url = null;
      }

      //
      Module id = manager.addAMD(m.getKey(), asset, url);
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
        if (status.decorated && (modules.length > 0)) {
          status = new Result.Status(status.code, true, new StreamableDecorator(status.streamable) {
            @Override
            protected void sendHeader(Stream consumer) {
              consumer.provide(new Chunk.Property<String>("juzu.amd", PropertyType.ASSET));
              for (Module module : modules) {
                consumer.provide(new Chunk.Property<Module>(module, Module.TYPE));
              }
            }
          });
        }
        request.setResult(status);
      }
    }
  }
}
