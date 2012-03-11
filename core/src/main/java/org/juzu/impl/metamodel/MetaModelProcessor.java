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

import org.juzu.Application;
import org.juzu.impl.compiler.BaseProcessor;
import org.juzu.impl.plugin.Plugin;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class MetaModelProcessor extends BaseProcessor
{

   /** . */
   private MetaModel metaModel;

   /** . */
   private Set<TypeElement> annotations;

   /** . */
   Filer filer;

   /** . */
   private int index;
   
   /** . */
   private final Logger log = BaseProcessor.getLogger(getClass());

   /** . */
   private List<Plugin> plugins;

   /** . */
   private ProcessingContext context;

   @Override
   protected void doInit(ProcessingEnvironment processingEnv)
   {
      log.log("Using processing nev " + processingEnv.getClass().getName());

      //
      ArrayList<Plugin> plugins = Tools.list(ServiceLoader.load(Plugin.class));
      StringBuilder msg = new StringBuilder("Using plugins:");
      for (Plugin plugin : plugins)
      {
         msg.append(" ").append(plugin.getName());
      }
      log.log(msg);

      // 
      HashSet<TypeElement> supportedAnnotationTypes = new HashSet<TypeElement>();
      supportedAnnotationTypes.add(processingEnv.getElementUtils().getTypeElement(Application.class.getName()));
      for (Plugin plugin : plugins)
      {
         for (String supportedAnnotationName : plugin.getSupportedAnnotationTypes())
         {
            TypeElement supportedAnnotationType = processingEnv.getElementUtils().getTypeElement(supportedAnnotationName);
            supportedAnnotationTypes.add(supportedAnnotationType);
         }
      }
      
      //
      this.filer = processingEnv.getFiler();
      this.annotations = supportedAnnotationTypes;
      this.index = 0;
      this.context = new ProcessingContext(processingEnv);
      this.plugins = plugins;
   }

   @Override
   protected ErrorCode decode(String key)
   {
      for (MetaModelError c : MetaModelError.values())
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
            metaModel.prePassivate();

            // Passivate model
            ObjectOutputStream out = null;
            try
            {
               FileObject file = filer.createResource(StandardLocation.SOURCE_OUTPUT, "org.juzu", "metamodel.ser");
               out = new ObjectOutputStream(file.openOutputStream());
               out.writeObject(metaModel);
               metaModel = null;
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
            if (metaModel == null)
            {
               InputStream in = null;
               try
               {
                  FileObject file = filer.getResource(StandardLocation.SOURCE_OUTPUT, "org.juzu", "metamodel.ser");
                  in = file.openInputStream();
                  ObjectInputStream ois = new ObjectInputStream(in);
                  metaModel = (MetaModel)ois.readObject();
                  log.log("Loaded model from " + file.toUri());
               }
               catch (Exception e)
               {
                  log.log("Created new meta model");
                  MetaModel metaModel = new MetaModel();
                  log.log("Adding meta model plugins");
                  for (Plugin plugin : plugins)
                  {
                     log.log("Adding meta model plugin: " + plugin.getName());
                     metaModel.addPlugin(plugin.getName(), plugin.newMetaModelPlugin());
                  }
                  
                  //
                  this.metaModel = metaModel;
               }
               finally
               {
                  Tools.safeClose(in);
               }

               // Activate
               log.log("Activating model");
               metaModel.postActivate(context);
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
                           metaModel.processAnnotation(annotatedElt, annotationFQN, annotationValues);
                        }
                     }
                  }
               }
            }

            //
            log.log("Post processing model");
            metaModel.postProcess();

            //
            log.log("Ending APT round #" + index++);
         }
      }
   }
}
