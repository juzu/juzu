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

package juzu.impl.plugin.application.metamodel;

import juzu.impl.metamodel.AnnotationKey;
import juzu.impl.metamodel.AnnotationState;
import juzu.impl.metamodel.EventQueue;
import juzu.impl.metamodel.MetaModelEvent;
import juzu.impl.metamodel.MetaModelPlugin;
import juzu.impl.plugin.module.metamodel.ModuleMetaModel;

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
  public final void processAnnotationChange(ApplicationMetaModel metaModel, AnnotationKey key, AnnotationState removed, AnnotationState added) {
    if (metaModel.getHandle().getQN().isPrefix(key.getElement().getPackage())) {
      super.processAnnotationChange(metaModel, key, removed, added);
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
