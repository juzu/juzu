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

package org.juzu.impl.processor;

import org.juzu.impl.compiler.BaseProcessor;
import org.juzu.impl.compiler.CompilationException;
import org.juzu.impl.metamodel.MetaModel;
import org.juzu.impl.spi.template.TemplateProvider;
import org.juzu.impl.utils.Tools;

import javax.annotation.Generated;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
@javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion.RELEASE_6)
@javax.annotation.processing.SupportedAnnotationTypes({
   "*"
/*
   "org.juzu.View","org.juzu.Action","org.juzu.Resource",

   "org.juzu.Application",

   "org.juzu.Path"
*/

})
public class ModelProcessor extends BaseProcessor
{

   /** . */
   private static final Pattern PROVIDER_PKG_PATTERN = Pattern.compile(
      "org\\.juzu\\.impl\\.spi\\.template\\.([^.]+)(?:\\..+)?"
   );

   /** . */
   private static final ThreadLocal<ProcessingEnvironment> env = new ThreadLocal<ProcessingEnvironment>();

   /** . */
   private MetaModel model;

   /** . */
   Filer filer;

   /** . */
   private Map<String, TemplateProvider> providers;

   static Element get(ElementHandle handle)
   {
      return handle.get(env.get());
   }

   @Override
   protected void doInit(ProcessingEnvironment processingEnv)
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
      this.filer = processingEnv.getFiler();

      //
      log("Using processing nev " + processingEnv.getClass().getName());
   }

   @Override
   protected void doProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
   {
      try
      {
         env.set(processingEnv);

         //
         if (!roundEnv.errorRaised())
         {
            if (roundEnv.processingOver())
            {
               model.prePassivate();

               //
               model.setEnv(null);

               // Passivate model
               ObjectOutputStream out = null;
               try
               {
                  FileObject file = filer.createResource(StandardLocation.SOURCE_OUTPUT, "org.juzu", "model2.ser");
                  out = new ObjectOutputStream(file.openOutputStream());
                  out.writeObject(model);
                  model = null;
               }
               catch (IOException e)
               {
                  e.printStackTrace();
               }
               finally
               {
                  Tools.safeClose(out);
               }
            }
            else
            {
               if (model == null)
               {
                  InputStream in = null;
                  try
                  {
                     FileObject file = filer.getResource(StandardLocation.SOURCE_OUTPUT, "org.juzu", "model2.ser");
                     in = file.openInputStream();
                     ObjectInputStream ois = new ObjectInputStream(in);
                     model = (MetaModel)ois.readObject();
                  }
                  catch (Exception e)
                  {
                     model = new MetaModel();
                  }
                  finally
                  {
                     Tools.safeClose(in);
                  }

                  // Set env
                  model.setEnv(processingEnv);
               }

               //
               HashSet<TypeElement> explicit = new HashSet<TypeElement>();
               explicit.add(processingEnv.getElementUtils().getTypeElement("org.juzu.View"));
               explicit.add(processingEnv.getElementUtils().getTypeElement("org.juzu.Action"));
               explicit.add(processingEnv.getElementUtils().getTypeElement("org.juzu.Resource"));
               explicit.add(processingEnv.getElementUtils().getTypeElement("org.juzu.Application"));
               explicit.add(processingEnv.getElementUtils().getTypeElement("org.juzu.Path"));
               annotations = explicit;

               //
               for (TypeElement annotationElt : annotations)
               {
                  for (Element annotatedElt : roundEnv.getElementsAnnotatedWith(annotationElt))
                  {
                     if (annotatedElt.getAnnotation(Generated.class) == null)
                     {
                        for (AnnotationMirror annotationMirror : annotatedElt.getAnnotationMirrors())
                        {
                           if (annotationMirror.getAnnotationType().asElement().equals(annotationElt))
                           {
                              String annotationName = annotationElt.getSimpleName().toString();
                              Map<String, Object> annotationValues = new HashMap<String, Object>();
                              for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotationMirror.getElementValues().entrySet())
                              {
                                 String m = entry.getKey().getSimpleName().toString();
                                 Object value = entry.getValue().getValue();
                                 annotationValues.put(m, value);
                              }
                              String annotationFQN = annotationElt.getQualifiedName().toString();
                              if (annotationFQN.equals("org.juzu.View") || annotationFQN.equals("org.juzu.Action") || annotationFQN.equals("org.juzu.Resource"))
                              {
                                 model.processControllerMethod(
                                    (ExecutableElement)annotatedElt,
                                    annotationName,
                                    annotationValues);
                              }
                              else if (annotationFQN.equals("org.juzu.Path"))
                              {
                                 if (annotatedElt instanceof VariableElement)
                                 {
                                    model.processDeclarationTemplate(
                                       (VariableElement)annotatedElt,
                                       annotationName,
                                       annotationValues);
                                 }
                                 else if (annotatedElt instanceof TypeElement)
                                 {
                                    // We ignore it on purpose
                                 }
                                 else
                                 {
                                    throw new CompilationException(annotatedElt, ErrorCode.UNSUPPORTED, "Annotation of this element is not yet supported");
                                 }
                              }
                              else if (annotationFQN.equals("org.juzu.Application"))
                              {
                                 model.processApplication(
                                    (PackageElement)annotatedElt,
                                    annotationName,
                                    annotationValues);
                              }
                              break;
                           }
                        }
                     }
                  }
               }

               //
               model.postProcess();
            }
         }
      }
      finally
      {
         env.set(null);
      }
   }
}
