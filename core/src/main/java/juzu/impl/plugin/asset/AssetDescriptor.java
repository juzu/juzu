package juzu.impl.plugin.asset;

import juzu.Scope;
import juzu.asset.AssetType;
import juzu.impl.asset.AssetManager;
import juzu.impl.asset.AssetMetaData;
import juzu.impl.asset.ManagerQualifier;
import juzu.impl.inject.BeanDescriptor;
import juzu.impl.metadata.Descriptor;
import juzu.impl.utils.Tools;

import java.lang.annotation.Annotation;
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
