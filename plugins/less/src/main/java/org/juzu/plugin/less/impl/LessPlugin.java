package org.juzu.plugin.less.impl;

import org.juzu.impl.application.Scope;
import org.juzu.impl.metamodel.MetaModelPlugin;
import org.juzu.impl.plugin.Plugin;
import org.juzu.plugin.less.Less;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class LessPlugin extends Plugin
{
   public LessPlugin()
   {
      super("compiler");
   }

   @Override
   public Map<Class<? extends Annotation>, Scope> getAnnotationTypes()
   {
      return Collections.<Class<? extends Annotation>, Scope>singletonMap(Less.class, Scope.APPLICATION);
   }

   @Override
   public MetaModelPlugin newMetaModelPlugin()
   {
      return new LessMetaModelPlugin();
   }
}
