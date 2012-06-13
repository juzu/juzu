package juzu.impl.plugin.asset;

import juzu.impl.asset.AssetMetaData;
import juzu.impl.metadata.Descriptor;

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

  public String getPackageName() {
    return packageName;
  }

  public List<AssetMetaData> getScripts() {
    return scripts;
  }

  public List<AssetMetaData> getStylesheets() {
    return stylesheets;
  }

/*
  @Override
  public Iterable<BeanDescriptor> getBeans() {
    return Tools.list(
      new BeanDescriptor(AssetManager.class, Scope.SINGLETON, Collections.<Annotation>singletonList(new ManagerQualifier(AssetType.SCRIPT)), null),
      new BeanDescriptor(AssetManager.class, Scope.SINGLETON, Collections.<Annotation>singletonList(new ManagerQualifier(AssetType.STYLESHEET)), null));
  }
*/
}
