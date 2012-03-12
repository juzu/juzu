package org.juzu.impl.plugin.asset;

import org.juzu.impl.metamodel.MetaModelPlugin;
import org.juzu.impl.plugin.Plugin;
import org.juzu.impl.request.RequestLifeCycle;

import javax.annotation.processing.SupportedAnnotationTypes;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
@SupportedAnnotationTypes("org.juzu.plugin.asset.Assets")
public class AssetPlugin extends Plugin
{

   public AssetPlugin()
   {
      super("asset");
   }


   @Override
   public MetaModelPlugin newMetaModelPlugin()
   {
      return new AssetMetaModelPlugin();
   }

   @Override
   public Class<? extends RequestLifeCycle> getLifeCycleClass()
   {
      return AssetLifeCycle.class;
   }
}
