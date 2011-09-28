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

import org.juzu.impl.template.groovy.GroovyTemplate;
import org.juzu.impl.template.groovy.GroovyTemplateBuilder;
import org.juzu.template.TemplateRef;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
@javax.annotation.processing.SupportedAnnotationTypes({"org.juzu.template.TemplateRef"})
@javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion.RELEASE_6)
public class TemplateProcessor extends AbstractProcessor
{

   @Override
   public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
   {

      Filer filer = processingEnv.getFiler();

      for (Element elt : roundEnv.getElementsAnnotatedWith(TemplateRef.class))
      {
         PackageElement pkgElt = processingEnv.getElementUtils().getPackageOf(elt);
         CharSequence pkgName = pkgElt.getQualifiedName().toString();
         TemplateRef ref = elt.getAnnotation(TemplateRef.class);
         String value = ref.value();
         try
         {
            FileObject file = filer.getResource(StandardLocation.SOURCE_PATH, pkgName, value);
            CharSequence content = file.getCharContent(false).toString();

            // For now handle only groovy templates
            String id = pkgName.length() == 0 ? value : (pkgName + "." + value);
            GroovyTemplate template = new TemplateParser().parse(content).build(new GroovyTemplateBuilder(id));

            // Now we create the template
            FileObject fo = filer.createResource(StandardLocation.CLASS_OUTPUT, pkgName, value, elt);
            Writer writer = fo.openWriter();
            writer.write(template.getScript());
            writer.close();

         }
         catch (IOException e)
         {
            e.printStackTrace();
         }
      }

      return false;
   }
}
