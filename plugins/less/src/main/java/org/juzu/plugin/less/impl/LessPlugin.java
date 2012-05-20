package org.juzu.plugin.less.impl;

import org.juzu.impl.metamodel.MetaModelPlugin;
import org.juzu.impl.plugin.Plugin;

import javax.annotation.processing.SupportedAnnotationTypes;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
@SupportedAnnotationTypes("org.juzu.plugin.less.Less")
public class LessPlugin extends Plugin
{
   public LessPlugin()
   {
      super("compiler");
   }

   @Override
   public MetaModelPlugin newMetaModelPlugin()
   {
      return new LessMetaModelPlugin();
   }
}
