package org.juzu.impl.controller;

import org.juzu.Action;
import org.juzu.Resource;
import org.juzu.View;
import org.juzu.impl.application.Scope;
import org.juzu.impl.controller.descriptor.ControllerDescriptor;
import org.juzu.impl.controller.metamodel.ControllerMetaModelPlugin;
import org.juzu.impl.metadata.Descriptor;
import org.juzu.impl.metamodel.MetaModelPlugin;
import org.juzu.impl.plugin.Plugin;
import org.juzu.impl.utils.Builder;
import org.juzu.impl.utils.JSON;

import java.lang.annotation.Annotation;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ControllerPlugin extends Plugin
{

   public ControllerPlugin()
   {
      super("controller");
   }

   @Override
   public Map<Class<? extends Annotation>, Scope> getAnnotationTypes()
   {
      return Builder.<Class<? extends Annotation>, Scope>
         map(View.class, Scope.APPLICATION).
         put(Action.class, Scope.APPLICATION).
         put(Resource.class, Scope.APPLICATION).
         build();
   }

   @Override
   public MetaModelPlugin newMetaModelPlugin()
   {
      return new ControllerMetaModelPlugin();
   }

   @Override
   public Descriptor loadDescriptor(ClassLoader loader, JSON config) throws Exception
   {
      return new ControllerDescriptor(loader, config);
   }
}
