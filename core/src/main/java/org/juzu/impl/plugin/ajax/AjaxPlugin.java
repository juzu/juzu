package org.juzu.impl.plugin.ajax;

import org.juzu.impl.application.Scope;
import org.juzu.impl.metamodel.MetaModelPlugin;
import org.juzu.impl.plugin.Plugin;
import org.juzu.impl.request.RequestLifeCycle;
import org.juzu.plugin.ajax.Ajax;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class AjaxPlugin extends Plugin
{
   public AjaxPlugin()
   {
      super("ajax");
   }

   @Override
   public Map<Class<? extends Annotation>, Scope> getAnnotationTypes()
   {
      return Collections.<Class<? extends Annotation>, Scope>singletonMap(Ajax.class, Scope.APPLICATION);
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
