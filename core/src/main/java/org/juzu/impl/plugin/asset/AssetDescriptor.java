package org.juzu.impl.plugin.asset;

import org.juzu.impl.asset.AssetMetaData;
import org.juzu.impl.metadata.BeanDescriptor;
import org.juzu.impl.metadata.Descriptor;

import java.util.Collections;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class AssetDescriptor extends Descriptor
{

   /** . */
   private List<AssetMetaData> scripts;

   /** . */
   private List<AssetMetaData> stylesheets;

   public AssetDescriptor(List<AssetMetaData> scripts, List<AssetMetaData> stylesheets)
   {
      this.scripts = scripts;
      this.stylesheets = stylesheets;
   }

   @Override
   public Iterable<BeanDescriptor> getBeans()
   {
      return Collections.singletonList(new BeanDescriptor(AssetLifeCycle.class, null, null, null));
   }

   public List<AssetMetaData> getScripts()
   {
      return scripts;
   }

   public List<AssetMetaData> getStylesheets()
   {
      return stylesheets;
   }
}
