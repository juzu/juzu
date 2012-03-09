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

package org.juzu.impl.metamodel;

import org.juzu.Action;
import org.juzu.Application;
import org.juzu.Path;
import org.juzu.Resource;
import org.juzu.View;
import org.juzu.impl.compiler.BaseProcessor;
import org.juzu.impl.utils.ErrorCode;
import org.juzu.impl.utils.Logger;
import org.juzu.impl.utils.Tools;

import javax.annotation.Generated;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class ModelProcessor extends BaseProcessor
{

   /** . */
   private static final Class<?>[] annotationTypes = {
      View.class,
      Action.class,
      Resource.class,
      Path.class,
      Application.class};

   /** . */
   private MetaModel model;

   /** . */
   private Set<TypeElement> annotations;

   /** . */
   Filer filer;

   /** . */
   private int index;
   
   /** . */
   private final Logger log = BaseProcessor.getLogger(getClass());

   /** . */
   private ProcessingContext context;

   @Override
   protected void doInit(ProcessingEnvironment processingEnv)
   {

      //
      HashSet<TypeElement> annotations = new HashSet<TypeElement>();
      for (Class c : annotationTypes)
      {
         annotations.add(processingEnv.getElementUtils().getTypeElement(c.getName()));
      }

      //
      this.filer = processingEnv.getFiler();
      this.annotations = annotations;
      this.index = 0;
      this.context = new ProcessingContext(processingEnv);

      //
      log.log("Using processing nev " + processingEnv.getClass().getName());
   }

   @Override
   protected ErrorCode decode(String key)
   {
      for (MetaModelErrorCode c : MetaModelErrorCode.values())
      {
         if (c.getKey().equals(key))
         {
            return c;
         }
      }
      return null;
   }

   @Override
   protected void doProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
   {
      if (!roundEnv.errorRaised())
      {
         if (roundEnv.processingOver())
         {
            log.log("APT processing over");

            //
            log.log("Passivating model");
            model.prePassivate();

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
               log.log("Could not passivate model ", e);
            }
            finally
            {
               Tools.safeClose(out);
            }
         }
         else
         {
            log.log("Starting APT round #" + index);

            //
            if (model == null)
            {
               InputStream in = null;
               try
               {
                  FileObject file = filer.getResource(StandardLocation.SOURCE_OUTPUT, "org.juzu", "model2.ser");
                  in = file.openInputStream();
                  ObjectInputStream ois = new ObjectInputStream(in);
                  model = (MetaModel)ois.readObject();
                  log.log("Loaded model from " + file.toUri());
               }
               catch (Exception e)
               {
                  log.log("Created new model");
                  model = new MetaModel();
               }
               finally
               {
                  Tools.safeClose(in);
               }

               // Activate
               log.log("Activating model");
               model.postActivate(context);
            }

            //
            for (TypeElement annotationElt : this.annotations)
            {
               for (Element annotatedElt : roundEnv.getElementsAnnotatedWith(annotationElt))
               {
                  if (annotatedElt.getAnnotation(Generated.class) == null)
                  {
                     for (AnnotationMirror annotationMirror : annotatedElt.getAnnotationMirrors())
                     {
                        if (annotationMirror.getAnnotationType().asElement().equals(annotationElt))
                        {
                           Map<String, Object> annotationValues = new HashMap<String, Object>();
                           for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotationMirror.getElementValues().entrySet())
                           {
                              String m = entry.getKey().getSimpleName().toString();
                              Object value = entry.getValue().getValue();
                              annotationValues.put(m, value);
                           }
                           String annotationFQN = annotationElt.getQualifiedName().toString();
                           model.processAnnotation(annotatedElt, annotationFQN, annotationValues);
                        }
                     }
                  }
               }
            }

            //
            log.log("Post processing model");
            model.postProcess();

            //
            log.log("Ending APT round #" + index++);
         }
      }
   }
}
