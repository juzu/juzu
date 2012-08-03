/*
 * Copyright (C) 2012 eXo Platform SAS.
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

import juzu.impl.common.FQN;
import juzu.impl.compiler.BaseProcessor;
import juzu.impl.compiler.MessageCode;
import juzu.impl.compiler.ProcessingContext;
import juzu.impl.common.Logger;
import juzu.impl.common.Tools;

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
import java.util.LinkedHashMap;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class MetaModelProcessor extends BaseProcessor {

  /** . */
  public static final MessageCode ANNOTATION_UNSUPPORTED = new MessageCode("ANNOTATION_UNSUPPORTED", "The annotation of this element cannot be supported");

  /** . */
  private MetaModelState<?, ?> state;

  /** . */
  private int index;

  /** . */
  private final Logger log = BaseProcessor.getLogger(getClass());

  @Override
  protected void doInit(ProcessingContext context) {
    log.log("Using processing env " + context.getClass().getName());

    //
    this.index = 0;
  }

  protected abstract Class<? extends MetaModelPlugin<?, ?>> getPluginType();

  protected abstract MetaModel<?, ?> createMetaModel();

  @Override
  protected void doProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if (!roundEnv.errorRaised()) {
      if (roundEnv.processingOver()) {
        log.log("APT processing over");

        //
        log.log("Passivating model");
        state.metaModel.prePassivate();

        // Passivate model
        ObjectOutputStream out = null;
        try {
          FileObject file = getContext().createResource(StandardLocation.SOURCE_OUTPUT, "juzu", "metamodel.ser");
          out = new ObjectOutputStream(file.openOutputStream());
          out.writeObject(state);
          state = null;
        }
        catch (IOException e) {
          log.log("Could not passivate model ", e);
        }
        finally {
          Tools.safeClose(out);
        }
      }
      else {
        log.log("Starting APT round #" + index);

        //
        if (state == null) {
          InputStream in = null;
          try {
            FileObject file = getContext().getResource(StandardLocation.SOURCE_OUTPUT, "juzu", "metamodel.ser");
            in = file.openInputStream();
            ObjectInputStream ois = new ObjectInputStream(in);
            state = (MetaModelState<?, ?>)ois.readObject();
            log.log("Loaded model from " + file.toUri());
          }
          catch (Exception e) {
            log.log("Created new meta model");
            MetaModelState<?, ?> metaModel = new MetaModelState(getPluginType(), createMetaModel());

            //
            metaModel.init(getContext());

            //
            this.state = metaModel;
          }
          finally {
            Tools.safeClose(in);
          }

          // Activate
          log.log("Activating model");
          state.metaModel.postActivate(getContext());
        }

        //
        LinkedHashMap<AnnotationKey, AnnotationState> updates = new LinkedHashMap<AnnotationKey, AnnotationState>();
        Set<Class<? extends Annotation>> abc = state.metaModel.getSupportedAnnotations();
        for (Class annotationType : abc) {
          TypeElement annotationElt = getContext().getTypeElement(annotationType.getName());
          for (Element annotatedElt : roundEnv.getElementsAnnotatedWith(annotationElt)) {
            if (annotatedElt.getAnnotation(Generated.class) == null) {
              for (AnnotationMirror annotationMirror : annotatedElt.getAnnotationMirrors()) {
                if (annotationMirror.getAnnotationType().asElement().equals(annotationElt)) {
                  AnnotationKey key = new AnnotationKey(annotatedElt, new FQN(((TypeElement)annotationMirror.getAnnotationType().asElement()).getQualifiedName().toString()));
                  AnnotationState state = AnnotationState.create(annotationMirror);
                  updates.put(key, state);
                }
              }
            }
          }
        }

        //
        log.log("Process annotations");
        state.context.processAnnotations(updates.entrySet());

        //
        log.log("Post processing model");
        state.metaModel.postProcessAnnotations();

        //
        log.log("Process events");
        state.metaModel.processEvents();

        //
        log.log("Post process events");
        state.metaModel.postProcessEvents();

        //
        log.log("Ending APT round #" + index++);
      }
    }
  }
}
