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

package juzu.impl.plugin.application.metamodel;

import juzu.impl.metamodel.AnnotationChange;
import juzu.impl.metamodel.AnnotationKey;
import juzu.impl.metamodel.AnnotationState;
import juzu.impl.metamodel.EventQueue;
import juzu.impl.metamodel.MetaModelEvent;
import juzu.impl.metamodel.MetaModelPlugin;
import juzu.impl.plugin.module.metamodel.ModuleMetaModel;

import javax.annotation.processing.Completion;
import java.io.Serializable;

/**
 * A plugin for meta model processing.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public abstract class ApplicationMetaModelPlugin extends MetaModelPlugin<ApplicationMetaModel, ApplicationMetaModelPlugin> implements Serializable {

  protected ApplicationMetaModelPlugin(String name) {
    super(name);
  }

  public void postActivate(ModuleMetaModel applications) {
  }

  @Override
  public void processAnnotationChange(ApplicationMetaModel metaModel, AnnotationChange change) {
    if (metaModel.getHandle().getPackage().isPrefix(change.getKey().getElement().getPackage())) {
      super.processAnnotationChange(metaModel, change);
    }
  }

  @Override
  public Iterable<? extends Completion> getCompletions(
      ApplicationMetaModel metaModel,
      AnnotationKey annotationKey,
      AnnotationState annotationState,
      String member,
      String userText) {
    if (metaModel.getHandle().getPackage().isPrefix(annotationKey.getElement().getPackage())) {
      return super.getCompletions(metaModel, annotationKey, annotationState, member, userText);
    } else {
      return null;
    }
  }

  @Override
  public final void processEvents(ApplicationMetaModel metaModel, EventQueue queue) {
    for (MetaModelEvent event : queue.clear()) {
      processEvent(metaModel, event);
    }
  }

  public void processEvent(ApplicationMetaModel application, MetaModelEvent event) {
  }

  public void prePassivate(ModuleMetaModel applications) {
  }
}
