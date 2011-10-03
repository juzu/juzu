/*
 * Copyright (C) 2011 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.juzu.impl.template;

import org.juzu.impl.spi.template.TemplateGenerator;
import org.juzu.impl.spi.template.TemplateProvider;
import org.juzu.template.Template;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
@javax.annotation.processing.SupportedAnnotationTypes({"org.juzu.template.Template"})
@javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion.RELEASE_6)
public class TemplateProcessor extends AbstractProcessor
{

   /** . */
   private static final Pattern PROVIDER_PKG_PATTERN = Pattern.compile(
      "org\\.juzu\\.impl\\.spi\\.template\\.([^.]+)(?:\\..+)?"
   );

   /** . */
   private static final Pattern NAME_PATTERN = Pattern.compile("([^.]+)\\.([a-zA-Z]+)");

   /** . */
   private Map<String, TemplateProvider> providers;

   @Override
   public void init(ProcessingEnvironment processingEnv)
   {
      super.init(processingEnv);

      // Discover the template provider
      ServiceLoader<TemplateProvider> loader = ServiceLoader.load(TemplateProvider.class, TemplateProvider.class.getClassLoader());

      //
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
   public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
   {

      TemplateParser parser = new TemplateParser();
      Filer filer = processingEnv.getFiler();

      for (Element elt : roundEnv.getElementsAnnotatedWith(Template.class))
      {
         PackageElement pkgElt = processingEnv.getElementUtils().getPackageOf(elt);
         CharSequence pkgName = pkgElt.getQualifiedName().toString();
         Template ref = elt.getAnnotation(Template.class);

         String value = ref.value();
         Matcher matcher = NAME_PATTERN.matcher(value);
         if (!matcher.matches())
         {
            throw new UnsupportedOperationException("handle me gracefully for name " + value);
         }

         //
         try
         {
            FileObject file = filer.getResource(StandardLocation.SOURCE_PATH, pkgName, value);
            CharSequence content = file.getCharContent(false).toString();

            //
            String extension = matcher.group(2);
            TemplateProvider provider = providers.get(extension);
            if (provider != null)
            {
               TemplateGenerator generator = provider.newGenerator();
               parser.parse(content).generate(generator);
               generator.generate(filer, pkgName.toString(), matcher.group(1));
            }
            else
            {
               throw new UnsupportedOperationException("handle me gracefully");
            }
         }
         catch (IOException e)
         {
            e.printStackTrace();
         }
      }

      //
      return false;
   }
}
