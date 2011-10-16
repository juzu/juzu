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

import org.juzu.Path;
import org.juzu.impl.application.ApplicationProcessor;
import org.juzu.impl.compiler.ProcessorPlugin;
import org.juzu.impl.spi.template.TemplateProvider;

import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
@javax.annotation.processing.SupportedAnnotationTypes({"org.juzu.Resource"})
@javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion.RELEASE_6)
public class TemplateProcessor extends ProcessorPlugin
{

   /** . */
   private static final Pattern PROVIDER_PKG_PATTERN = Pattern.compile(
      "org\\.juzu\\.impl\\.spi\\.template\\.([^.]+)(?:\\..+)?"
   );

   /** . */
   Map<String, TemplateProvider> providers;

   /** . */
   private ApplicationProcessor applicationPlugin;

   @Override
   public void init()
   {
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
      this.applicationPlugin = getPlugin(ApplicationProcessor.class);
   }

   @Override
   public void process()
   {
      for (final Element elt : getElementsAnnotatedWith(Path.class))
      {
         PackageElement packageElt = getPackageOf(elt);
         Path ref = elt.getAnnotation(Path.class);

         //
         ApplicationProcessor.ApplicationMetaData application = applicationPlugin.getApplication(packageElt);
         if (application == null)
         {
            throw new UnsupportedOperationException("handle me gracefully");
         }

         //
         TemplateCompiler compiler = new TemplateCompiler(this, application, getFiler());

         //
         try
         {
            compiler.compile(elt, ref.value());
         }
         catch (IOException e)
         {
            e.printStackTrace();
         }
      }
   }
}
