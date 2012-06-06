package juzu.impl.plugin.asset;

import juzu.impl.asset.AssetMetaData;
import juzu.impl.metadata.BeanDescriptor;
import juzu.impl.metadata.Descriptor;

import java.util.Collections;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class AssetDescriptor extends Descriptor {

  /** . */
  private final String packageName;

  /** . */
  private List<AssetMetaData> scripts;

  /** . */
  private List<AssetMetaData> stylesheets;

  public AssetDescriptor(String packageName, List<AssetMetaData> scripts, List<AssetMetaData> stylesheets) {
    this.packageName = packageName;
    this.scripts = scripts;
    this.stylesheets = stylesheets;
  }

  @Override
  public Iterable<BeanDescriptor> getBeans() {
    return Collections.singletonList(new BeanDescriptor(AssetLifeCycle.class, null, null, null));
  }

  public String getPackageName() {
    return packageName;
  }

  public List<AssetMetaData> getScripts() {
    return scripts;
  }

  public List<AssetMetaData> getStylesheets() {
    return stylesheets;
  }
}
