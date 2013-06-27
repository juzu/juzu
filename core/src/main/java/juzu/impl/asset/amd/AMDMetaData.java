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
package juzu.impl.asset.amd;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import juzu.asset.AssetLocation;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 *
 */
public class AMDMetaData {
  
  /** The amd name. */
  final String name;

  /** The amd location. */
  final AssetLocation location;

  /** The amd source path. */
  final String path;

  /** The asset dependencies. */
  final Map<String, AMDDependency> dependencies;
  
  /** The adapter to adapt script. */
  final String adapter;
  
  final boolean isRequire;
  
  public AMDMetaData(String name, AssetLocation location, String path) {
    this(name, location, path, null, null, false);
  }
  
  public AMDMetaData(String name, AssetLocation location, String path, String adapter, boolean isRequire) {
    this(name, location, path, adapter, null, isRequire);
  }
  
  public AMDMetaData(String name, AssetLocation location, String path, String adapter, Map<String, AMDDependency> dependencies, boolean isRequire) {
    this.name = name;
    this.location = location;
    this.path = path;
    this.adapter = adapter;
    this.dependencies = dependencies == null ? new HashMap<String, AMDDependency>() : dependencies;
    this.isRequire = isRequire;
  }
  
  public String getName() {
    return name;
  }
  
  public AssetLocation getLocation() {
    return location;
  }
  
  public String getPath() {
    return path;
  }
  
  public String getAdapter() {
    return adapter;
  }
  
  public boolean isRequire() {
    return isRequire;
  }
  
  public void addDependency(AMDDependency dependency) {
    dependencies.put(dependency.name, dependency);
  }
  
  public Map<String, AMDDependency> getDependencies() {
    return Collections.unmodifiableMap(dependencies);
  }
}
