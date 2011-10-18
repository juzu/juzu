package org.juzu.impl.template;

import org.juzu.AmbiguousResolutionException;
import org.juzu.impl.application.ApplicationProcessor;
import org.juzu.impl.compiler.CompilationException;
import org.juzu.impl.utils.MethodInvocation;
import org.juzu.impl.spi.template.TemplateGenerator;
import org.juzu.impl.spi.template.TemplateProvider;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class TemplateCompiler 
{

   /** . */
   private static final Pattern NAME_PATTERN = Pattern.compile("([^.]+)\\.([a-zA-Z]+)");

   /** . */
   private TemplateProcessor processor;

   /** . */
   private final ApplicationProcessor.ApplicationMetaData application;

   /** . */
   private final Filer filer;

   /** . */
   private final String templatesPkgFQN;

   /** . */
   private final Map<String, String> cache;

   /** The templates built in progress. */
   private final Set<String> building;

   TemplateCompiler(
      TemplateProcessor processor,
      ApplicationProcessor.ApplicationMetaData application,
      Filer filer)
   {
      StringBuilder templatesPkgSB = new StringBuilder(application.getPackageName());
      if (templatesPkgSB.length() > 0)
      {
         templatesPkgSB.append(".");
      }
      templatesPkgSB.append("templates");

      //
      this.templatesPkgFQN = templatesPkgSB.toString();
      this.application = application;
      this.filer = filer;
      this.processor = processor;
      this.cache = new HashMap<String, String>();
      this.building = new HashSet<String>();
   }
   
   String compile(final Element element, String path) throws IOException
   {
      String v = cache.get(path);
      if (v != null)
      {
         return v;
      }
      if (building.contains(path))
      {
         throw new UnsupportedOperationException("circulariry detected (not handled for now)");
      }

      //
      Matcher matcher = NAME_PATTERN.matcher(path);
      if (!matcher.matches())
      {
         throw new UnsupportedOperationException("handle me gracefully for name " + path);
      }

      //
      FileObject file = filer.getResource(StandardLocation.SOURCE_PATH, templatesPkgFQN, path);
      CharSequence content = file.getCharContent(false).toString();

      //
      String extension = matcher.group(2);
      TemplateProvider provider = processor.providers.get(extension);

      //
      TemplateCompilationContext tgc = new TemplateCompilationContext()
      {
         @Override
         public String resolveTemplate(String path) throws IOException
         {
            return compile(element, path);
         }
         @Override
         public MethodInvocation resolveMethodInvocation(String typeName, String methodName, Map<String, String> parameterMap)
         {
            ApplicationProcessor.MethodMetaData methodMD;
            try
            {
               methodMD = application.resolve(typeName, methodName, parameterMap.keySet());
            }
            catch (AmbiguousResolutionException e)
            {
               throw new CompilationException(element, "Could not resolve method arguments " + methodName + parameterMap);
            }
            if (methodMD != null)
            {
               List<String> args = new ArrayList<String>();
               for (VariableElement ve : methodMD.getElement().getParameters())
               {
                  String value = parameterMap.get(ve.getSimpleName().toString());
                  args.add(value);
               }
               return new MethodInvocation(methodMD.getController().getClassName() + "_", methodMD.getName() + "URL", args);
            }
            else
            {
               throw new CompilationException(element, "Could not resolve method name " + methodName + parameterMap);
            }
         }
      };

      //
      if (provider != null)
      {
         TemplateGenerator generator = provider.newGenerator();

         try
         {
            ASTNode.Template.parse(content).generate(generator, tgc);
            building.add(path);
            String ret = generator.generate(filer, templatesPkgFQN, matcher.group(1));
            cache.put(path, ret);
            return ret;
         }
         catch (ParseException e)
         {
            throw new CompilationException(element, "Could not compile template " + path, e);
         }
         finally
         {
            building.remove(path);
         }
      }
      else
      {
         throw new UnsupportedOperationException("handle me gracefully");
      }
   }
}
