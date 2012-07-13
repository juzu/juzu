package juzu.impl.asset;

import juzu.asset.AssetLocation;
import juzu.impl.common.Tools;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

/**
 * Describes an asset.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class AssetMetaData {

  /** The asset id. */
  final String id;

  /** The asset location. */
  final AssetLocation location;

  /** The asset value. */
  final String value;

  /** The asset dependencies. */
  final Set<String> dependencies;

  public AssetMetaData(String id, AssetLocation location, String value, String... dependencies) {
    this.id = id;
    this.value = value;
    this.location = location;
    this.dependencies = Collections.unmodifiableSet(Tools.set(dependencies));
  }

  public String getId() {
    return id;
  }

  public AssetLocation getLocation() {
    return location;
  }

  public String getValue() {
    return value;
  }

  public Set<String> getDependencies() {
    return dependencies;
  }

  @Override
  public String toString() {
    return "AssetDescriptor[id=" + id + ",location=" + location + ",value=" + value + ",dependencies=" + Arrays.asList(dependencies) + "]";
  }
}
