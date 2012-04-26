package org.juzu.impl.template.metamodel;

import org.juzu.impl.application.metamodel.ApplicationMetaModel;
import org.juzu.impl.application.metamodel.ApplicationsMetaModel;
import org.juzu.impl.compiler.CompilationException;
import org.juzu.impl.compiler.ElementHandle;
import org.juzu.impl.metamodel.MetaModelError;
import org.juzu.impl.metamodel.MetaModel;
import org.juzu.impl.metamodel.MetaModelPlugin;
import org.juzu.impl.spi.template.TemplateProvider;
import org.juzu.impl.template.compiler.Template;
import org.juzu.impl.utils.JSON;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class TemplateMetaModelPlugin extends MetaModelPlugin
{

   /** . */
   public static final Pattern TEMPLATE_PATH_PATTERN = Pattern.compile("([^/].*/|)([^./]+)\\.([a-zA-Z]+)");

   /** . */
   private static final Pattern PROVIDER_PKG_PATTERN = Pattern.compile(
      "org\\.juzu\\.impl\\.spi\\.template\\.([^.]+)(?:\\..+)?"
   );

   /** . */
   Map<String, TemplateProvider> providers;

   @Override
   public void init(ApplicationsMetaModel applications)
   {
   }

   @Override
   public void postActivateApplicationsMetaModel(ApplicationsMetaModel applications)
   {
      // Discover the template providers
      ServiceLoader<TemplateProvider> loader = ServiceLoader.load(TemplateProvider.class, TemplateProvider.class.getClassLoader());
      Map<String, TemplateProvider> providers = new HashMap<String, TemplateProvider>();
      for (TemplateProvider provider : loader)
      {
         // Get extension
         String pkgName = provider.getClass().getPackage().getName();

         //
         Matcher matcher = PROVIDER_PKG_PATTERN.matcher(pkgName);
         if (matcher.matches())
         {
            String extension = matcher.group(1);
            providers.put(extension, provider);
         }
      }
      
      //
      this.providers = providers;
   }

   @Override
   public void postConstruct(ApplicationMetaModel application)
   {
      application.addChild(TemplatesMetaModel.KEY, new TemplatesMetaModel());
   }

   @Override
   public void processAnnotation(ApplicationMetaModel application, Element element, String fqn, Map<String, Object> values) throws CompilationException
   {
      if (fqn.equals("org.juzu.Path"))
      {
         if (element instanceof VariableElement)
         {
            VariableElement variableElt = (VariableElement)element;
            MetaModel.log.log("Processing template declaration " + variableElt.getEnclosingElement() + "#"  + variableElt);

            //
            TemplatesMetaModel at = application.getChild(TemplatesMetaModel.KEY);

            //
            String path = (String)values.get("value");
            ElementHandle.Field handle = ElementHandle.Field.create(variableElt);
            at.add(handle, path);
         }
         else if (element instanceof TypeElement)
         {
            // We ignore it on purpose
         }
         else
         {
            throw new CompilationException(element, MetaModelError.ANNOTATION_UNSUPPORTED);
         }
      }
   }

   @Override
   public void prePassivate(ApplicationMetaModel model)
   {
      MetaModel.log.log("Passivating template resolver for " + model.getHandle());
      model.getTemplates().resolver.prePassivate();
   }

   @Override
   public void prePassivate(ApplicationsMetaModel applications)
   {
      MetaModel.log.log("Passivating templates");
      this.providers = null;
   }

   @Override
   public void postProcessEvents(ApplicationMetaModel application)
   {
      MetaModel.log.log("Processing templates of " + application.getHandle());
      application.getTemplates().resolver.process(this, application.model.env);
   }

   @Override
   public JSON getDescriptor(ApplicationMetaModel application)
   {
      JSON config = new JSON();
      ArrayList<String> templates = new ArrayList<String>();
      for (Template template : application.getTemplates().resolver.getTemplates())
      {
         templates.add(template.getFQN().getFullName());
      }
      config.map("templates", templates);
      config.set("package", application.getTemplates().getQN().toString());
      return config;
   }
}
