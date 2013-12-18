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

import juzu.impl.asset.AssetManager;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedHashMap;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 * 
 */
public class ModuleManager {

  /** . */
  protected final LinkedHashMap<String, Module> modules = new LinkedHashMap<String, Module>();

  /** . */
  private final AssetManager assetManager;

  @Inject
  public ModuleManager(AssetManager assetManager) {
    this.assetManager = assetManager;
  }

  public Module addAMD(ModuleMetaData data, URL url) throws NullPointerException, IllegalArgumentException, IOException {
    String name = data.getId();

    // Use value hashcode if no id is provided
    if (name == null) {
      name = "" + data.getPath().hashCode();
    }

    //
    Module module = modules.get(name);
    if (module == null) {
      modules.put(name, module = new Module(name, data.getLocation(), data.getPath()));

      //
      switch (data.getLocation()) {
        case APPLICATION :
          assetManager.resources.put(data.getPath(), url);
          break;
        case SERVER :
        case URL:
          if (data instanceof ModuleMetaData.Require) {
            assetManager.resources.put(data.getPath(), url);
          }
          break;
        default :
          // Nothing to do
          break;
      }
    }

    //
    return module;
  }

}
