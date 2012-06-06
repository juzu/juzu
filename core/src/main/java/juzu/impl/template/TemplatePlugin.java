package juzu.impl.template;

import juzu.Path;
import juzu.impl.application.metamodel.ApplicationMetaModelPlugin;
import juzu.impl.metadata.Descriptor;
import juzu.impl.plugin.Plugin;
import juzu.impl.template.metadata.TemplatesDescriptor;
import juzu.impl.template.metamodel.TemplateMetaModelPlugin;
import juzu.impl.utils.JSON;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class TemplatePlugin extends Plugin
{

   public TemplatePlugin()
   {
      super("template");
   }

   @Override
   public Set<Class<? extends Annotation>> getAnnotationTypes()
   {
      return Collections.<Class<? extends Annotation>>singleton(Path.class);
   }

   @Override
   public ApplicationMetaModelPlugin newApplicationMetaModelPlugin()
   {
      return new TemplateMetaModelPlugin();
   }

   @Override
   public Descriptor loadDescriptor(ClassLoader loader, JSON config) throws Exception
   {
      return new TemplatesDescriptor(loader, config);
   }
}
