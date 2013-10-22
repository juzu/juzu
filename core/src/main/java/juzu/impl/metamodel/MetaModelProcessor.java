/*
 * Copyright 2013 eXo Platform SAS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package juzu.impl.metamodel;

import juzu.impl.common.Name;
import juzu.impl.compiler.BaseProcessor;
import juzu.impl.compiler.MessageCode;
import juzu.impl.compiler.ProcessingContext;
import juzu.impl.common.Logger;
import juzu.impl.common.Tools;

import javax.annotation.Generated;
import javax.annotation.processing.Completion;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class MetaModelProcessor<P extends MetaModelPlugin<M, P>, M extends MetaModel<P, M>> extends BaseProcessor {

  /** . */
  public static final MessageCode ANNOTATION_UNSUPPORTED = new MessageCode("ANNOTATION_UNSUPPORTED", "The annotation of this element cannot be supported");

  /** . */
  private MetaModelState<P, M> state;

  /** . */
  private int index;

  /** . */
  private final Logger log = BaseProcessor.getLogger(getClass());

  /** . */
  private HashSet<String> supportedAnnotations;

  @Override
  protected void doInit(ProcessingContext context) {
    log.info("Using processing env " + context.getClass().getName());

    // Try to get state or create new one
    if (state == null) {
      InputStream in = null;
      try {
        FileObject file = getContext().getResource(StandardLocation.SOURCE_OUTPUT, "juzu", "metamodel.ser");
        in = file.openInputStream();
        ObjectInputStream ois = new ObjectInputStream(in);
        state = (MetaModelState<P, M>)ois.readObject();
        log.info("Loaded model from " + file.toUri());
      }
      catch (Exception e) {
        log.info("Created new meta model");
        MetaModelState<P, M> metaModel = new MetaModelState<P, M>(getPluginType(), createMetaModel());
        metaModel.init(getContext());
        state = metaModel;
      }
      finally {
        Tools.safeClose(in);
      }
    }

    //
    HashSet<String> supportedAnnotations = new HashSet<String>();
    for (Class<?> supportedAnnotation : state.context.getSupportedAnnotations()) {
      supportedAnnotations.add(supportedAnnotation.getName());
    }

    //
    this.supportedAnnotations = supportedAnnotations;
    this.index = 0;
  }

  protected abstract Class<P> getPluginType();

  protected abstract M createMetaModel();

  @Override
  protected void doProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if (!roundEnv.errorRaised()) {
      if (roundEnv.processingOver()) {
        log.info("APT processing over");

        //
        log.info("Passivating model");
        state.metaModel.prePassivate();

        // Passivate model
        ObjectOutputStream out = null;
        try {
          FileObject file = getContext().createResource(StandardLocation.SOURCE_OUTPUT, "juzu", "metamodel.ser");
          out = new ObjectOutputStream(file.openOutputStream());
          out.writeObject(state);
          state = null;
        }
        catch (Exception e) {
          e.printStackTrace();
          log.info("Could not passivate model ", e);
        }
        finally {
          Tools.safeClose(out);
        }
      }
      else {
        log.info("Starting APT round #" + index);

        //
        if (index == 0) {
          log.info("Activating model");
          state.metaModel.postActivate(getContext());
        }

        //
        LinkedHashMap<AnnotationKey, AnnotationState> updates = new LinkedHashMap<AnnotationKey, AnnotationState>();
        for (TypeElement annotationElt : annotations) {
          if (supportedAnnotations.contains(annotationElt.getQualifiedName().toString())) {
            log.info("Processing elements for annotation for " + annotationElt.getQualifiedName());
            for (Element annotatedElt : roundEnv.getElementsAnnotatedWith(annotationElt)) {
              if (annotatedElt.getAnnotation(Generated.class) == null) {
                log.info("Processing element " + annotatedElt);
                for (AnnotationMirror annotationMirror : annotatedElt.getAnnotationMirrors()) {
                  if (annotationMirror.getAnnotationType().asElement().equals(annotationElt)) {
                    AnnotationKey key = new AnnotationKey(annotatedElt, annotationMirror);
                    AnnotationState state = AnnotationState.create(annotationMirror);
                    updates.put(key, state);
                  }
                }
              }
            }
          }
        }

        //
        log.info("Process annotations");
        state.context.processAnnotations(updates.entrySet());

        //
        log.info("Post processing model");
        state.metaModel.postProcessAnnotations();

        //
        log.info("Process events");
        state.metaModel.processEvents();

        //
        log.info("Post process events");
        state.metaModel.postProcessEvents();

        //
        log.info("Ending APT round #" + index++);
      }
    }
  }

  @Override
  public Iterable<? extends Completion> getCompletions(Element element, AnnotationMirror annotation, ExecutableElement member, String userText) {
    Iterable<? extends Completion> completions;
    // For now we don't provide completion when element is absent
    if (element != null) {
      // Get state (but we won't save it)
      log.info("Activating model");
      state.metaModel.postActivate(getContext());
      AnnotationKey annotationKey = new AnnotationKey(element, Name.parse(((TypeElement)annotation.getAnnotationType().asElement()).getQualifiedName().toString()));
      AnnotationState annotationState = AnnotationState.create(annotation);
      completions = state.context.getCompletions(annotationKey, annotationState, member.getSimpleName().toString(), userText);
    } else {
      completions = Collections.emptyList();
    }
    return completions != null ? completions : Collections.<Completion>emptyList();
  }
}
