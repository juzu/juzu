package org.juzu.impl.plugin.ajax;

import org.juzu.impl.metamodel.MetaModelPlugin;
import org.juzu.impl.plugin.Plugin;
import org.juzu.impl.request.RequestLifeCycle;

import javax.annotation.processing.SupportedAnnotationTypes;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
@SupportedAnnotationTypes("org.juzu.plugin.ajax.Ajax")
public class AjaxPlugin extends Plugin
{
   public AjaxPlugin()
   {
      super("ajax");
   }

   @Override
   public Class<? extends RequestLifeCycle> getLifeCycleClass()
   {
      return AjaxLifeCycle.class;
   }

   @Override
   public MetaModelPlugin newMetaModelPlugin()
   {
      return new AjaxMetaModelPlugin();
   }
}
