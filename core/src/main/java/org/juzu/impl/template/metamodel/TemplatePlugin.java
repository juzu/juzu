package org.juzu.impl.template.metamodel;

import org.juzu.impl.application.metamodel.ApplicationMetaModel;
import org.juzu.impl.compiler.CompilationException;
import org.juzu.impl.compiler.ElementHandle;
import org.juzu.impl.model.CompilationErrorCode;
import org.juzu.impl.model.meta.MetaModel;
import org.juzu.impl.model.meta.MetaModelEvent;
import org.juzu.impl.model.meta.MetaModelObject;
import org.juzu.impl.model.meta.MetaModelPlugin;
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
public class TemplatePlugin extends MetaModelPlugin
{

   /** . */
   public static final Pattern TEMPLATE_PATH_PATTERN = Pattern.compile("([^/].*/|)([^./]+)\\.([a-zA-Z]+)");

   /** . */
   private static final Pattern PROVIDER_PKG_PATTERN = Pattern.compile(
      "org\\.juzu\\.impl\\.spi\\.template\\.([^.]+)(?:\\..+)?"
   );

   /** . */
   Map<String, TemplateProvider> providers;

   /** . */
   private Map<ElementHandle.Package, TemplateResolver> templateRepositoryMap;

   @Override
   public void init(MetaModel model)
   {
      model.addChild(TemplateRefsMetaModel.KEY, new TemplateRefsMetaModel());

      //
      this.templateRepositoryMap = new HashMap<ElementHandle.Package, TemplateResolver>();
   }

   @Override
   public void postActivate(MetaModel moel)
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
   public void processAnnotation(MetaModel model, Element element, String annotationFQN, Map<String, Object> annotationValues) throws CompilationException
   {
      if (annotationFQN.equals("org.juzu.Path"))
      {
         if (element instanceof VariableElement)
         {
            VariableElement variableElt = (VariableElement)element;
            MetaModel.log.log("Processing template declaration " + variableElt.getEnclosingElement() + "#"  + variableElt);
            model.getChild(TemplateRefsMetaModel.KEY).processDeclarationTemplate(variableElt, annotationFQN, annotationValues);
         }
         else if (element instanceof TypeElement)
         {
            // We ignore it on purpose
         }
         else
         {
            throw new CompilationException(element, CompilationErrorCode.ANNOTATION_UNSUPPORTED);
         }
      }
   }

   @Override
   public void processEvent(MetaModel model, MetaModelEvent event)
   {
      MetaModelObject obj = event.getObject();
      if (obj instanceof ApplicationMetaModel)
      {
         ApplicationMetaModel application = (ApplicationMetaModel)obj;
         if (event.getType() == MetaModelEvent.AFTER_ADD)
         {
            templateRepositoryMap.put(application.getHandle(), new TemplateResolver(application));
         }
         else if (event.getType() == MetaModelEvent.BEFORE_REMOVE)
         {
            templateRepositoryMap.remove(application.getHandle());
         }
      }
      else if (obj instanceof TemplateMetaModel)
      {
         TemplateMetaModel template = (TemplateMetaModel)obj;
         if (event.getType() == MetaModelEvent.AFTER_ADD)
         {
            // ?
         }
         else if (event.getType() == MetaModelEvent.BEFORE_REMOVE)
         {
            ElementHandle.Package handle = (ElementHandle.Package)event.getPayload();
            TemplateResolver resolver = templateRepositoryMap.get(handle);
            if (resolver != null)
            {
               resolver.removeTemplate(template.getPath());
            }
         }
      }
   }

   @Override
   public void prePassivate(MetaModel model)
   {
      MetaModel.log.log("Passivating templates");
      for (TemplateResolver repo : templateRepositoryMap.values())
      {
         repo.prePassivate();
      }

      //
      this.providers = null;
   }

   @Override
   public void postProcess(MetaModel model)
   {
      MetaModel.log.log("Processing templates");
      for (Map.Entry<ElementHandle.Package, TemplateResolver> entry : templateRepositoryMap.entrySet())
      {
         TemplateResolver repo = entry.getValue();
         repo.process(this, model.env);
      }
   }

   @Override
   public void emitConfig(ApplicationMetaModel application, JSON json)
   {
      TemplateResolver repo = templateRepositoryMap.get(application.getHandle());
      if (repo != null)
      {
         ArrayList<String> templates = new ArrayList<String>();
         for (Template template : repo.getTemplates())
         {
            templates.add(template.getFQN().getFullName());
         }
         json.add("templates", templates);
      }
   }
}
