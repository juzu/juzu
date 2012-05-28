package org.juzu.impl.plugin.portlet;

import org.juzu.impl.application.Scope;
import org.juzu.impl.metamodel.MetaModelPlugin;
import org.juzu.impl.plugin.Plugin;
import org.juzu.plugin.portlet.Portlet;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class PortletPlugin extends Plugin
{

   public PortletPlugin()
   {
      super("portlet");
   }

   @Override
   public Map<Class<? extends Annotation>, Scope> getAnnotationTypes()
   {
      return Collections.<Class<? extends Annotation>, Scope>singletonMap(Portlet.class, Scope.APPLICATION);
   }

   @Override
   public MetaModelPlugin newMetaModelPlugin()
   {
      return new PortletMetaModelPlugin();
   }
}
