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

package juzu.impl.metamodel;

import juzu.impl.compiler.AnnotationData;
import juzu.impl.compiler.BaseProcessor;
import juzu.impl.compiler.MessageCode;
import juzu.impl.compiler.ProcessingContext;
import juzu.impl.utils.Logger;
import juzu.impl.utils.Tools;

import javax.annotation.Generated;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class MetaModelProcessor extends BaseProcessor
{

   /** . */
   public static final MessageCode ANNOTATION_UNSUPPORTED = new MessageCode("ANNOTATION_UNSUPPORTED", "The annotation of this element cannot be supported");

   /** . */
   private MetaModel metaModel;

   /** . */
   private Set<TypeElement> annotations;

   /** . */
   private int index;
   
   /** . */
   private final Logger log = BaseProcessor.getLogger(getClass());

   /** . */
   private List<MetaModelPlugin> plugins;

   @Override
   protected void doInit(ProcessingContext context)
   {
      log.log("Using processing env " + context.getClass().getName());

      //
      ArrayList<MetaModelPlugin> metaModelPlugins = Tools.list(ServiceLoader.load(MetaModelPlugin.class, MetaModelPlugin.class.getClassLoader()));
      StringBuilder msg = new StringBuilder("Using plugins:");
      for (MetaModelPlugin plugin : metaModelPlugins)
      {
         msg.append(" ").append(plugin.getName());
      }
      log.log(msg);


      //
      HashSet<TypeElement> supportedAnnotationTypes = new HashSet<TypeElement>();
      for (MetaModelPlugin plugin : metaModelPlugins)
      {
         for (Class<? extends Annotation> type : plugin.getAnnotationTypes())
         {
            TypeElement supportedAnnotationType = context.getTypeElement(type.getName());
            supportedAnnotationTypes.add(supportedAnnotationType);
         }
      }

      //
      this.annotations = supportedAnnotationTypes;
      this.index = 0;
      this.plugins = metaModelPlugins;
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
               FileObject file = getContext().createResource(StandardLocation.SOURCE_OUTPUT, "juzu", "metamodel.ser");
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
                  FileObject file = getContext().getResource(StandardLocation.SOURCE_OUTPUT, "juzu", "metamodel.ser");
                  in = file.openInputStream();
                  ObjectInputStream ois = new ObjectInputStream(in);
                  metaModel = (MetaModel)ois.readObject();
                  log.log("Loaded model from " + file.toUri());
               }
               catch (Exception e)
               {
                  log.log("Created new meta model");
                  MetaModel metaModel = new MetaModel();

                  //
                  log.log("Adding meta model plugins");
                  for (MetaModelPlugin plugin : plugins)
                  {
                     log.log("Adding meta model plugin: " + plugin.getName());
                     metaModel.addPlugin(plugin.getName(), plugin);
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
               metaModel.postActivate(getContext());
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
                           AnnotationData annotationData = AnnotationData.create(annotationMirror);
                           String annotationFQN = annotationElt.getQualifiedName().toString();
                           metaModel.processAnnotation(annotatedElt, annotationFQN, annotationData);
                        }
                     }
                  }
               }
            }

            //
            log.log("Post processing model");
            metaModel.postProcessAnnotations();

            //
            log.log("Ending APT round #" + index++);
         }
      }
   }
}
