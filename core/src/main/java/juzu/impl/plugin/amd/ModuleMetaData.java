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

import java.util.LinkedHashMap;
import java.util.Map;

import juzu.asset.AssetLocation;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 *
 */
public abstract class ModuleMetaData {
  
  /** The amd id. */
  private final String id;

  /** The amd source path. */
  private final String path;

  public ModuleMetaData(String id, String path) {
    this.id = id;
    this.path = path;
  }

  public String getId() {
    return id;
  }
  
  public String getPath() {
    return path;
  }

  public abstract AssetLocation getLocation();

  public static class Define extends ModuleMetaData {

    /** The asset dependencies. */
    private final Map<String, AMDDependency> dependencies;

    /** The adapter to adapt script. */
    private final String adapter;

    public Define(String name, String path, String adapter) {
      super(name, path);
      this.dependencies = new LinkedHashMap<String, AMDDependency>();
      this.adapter = adapter;
    }

    public String getAdapter() {
      return adapter;
    }

    public void addDependency(AMDDependency dependency) {
      dependencies.put(dependency.name, dependency);
    }

    public Iterable<AMDDependency> getDependencies() {
      return dependencies.values();
    }

    @Override
    public AssetLocation getLocation() {
      return AssetLocation.APPLICATION;
    }
  }

  public static class Require extends ModuleMetaData {

    /** The amd location. */
    final AssetLocation location;

    public Require(String name, String path, AssetLocation location) {
      super(name, path);
      this.location = location;
    }

    public AssetLocation getLocation() {
      return location;
    }
  }
}
