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

package juzu.impl.plugin.ajax;

import juzu.impl.plugin.application.metamodel.ApplicationMetaModel;
import juzu.impl.plugin.application.metamodel.ApplicationMetaModelPlugin;
import juzu.impl.common.FQN;
import juzu.impl.metamodel.AnnotationKey;
import juzu.impl.metamodel.AnnotationState;
import juzu.impl.compiler.ElementHandle;
import juzu.impl.common.JSON;
import juzu.impl.compiler.ProcessingContext;
import juzu.plugin.ajax.Ajax;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class AjaxMetaModelPlugin extends ApplicationMetaModelPlugin {

  /** . */
  private static final FQN AJAX = new FQN(Ajax.class);

  /** . */
  private final HashMap<ElementHandle.Package, Boolean> enabledMap = new HashMap<ElementHandle.Package, Boolean>();

  public AjaxMetaModelPlugin() {
    super("ajax");
  }

  @Override
  public Set<Class<? extends java.lang.annotation.Annotation>> init(ProcessingContext env) {
    return Collections.<Class<? extends java.lang.annotation.Annotation>>singleton(Ajax.class);
  }

  @Override
  public void processAnnotationAdded(ApplicationMetaModel metaModel, AnnotationKey key, AnnotationState added) {
    if (key.getType().equals(AJAX)) {
      ElementHandle.Package handle = metaModel.getHandle();
      enabledMap.put(handle, true);
    }
  }

  @Override
  public void processAnnotationRemoved(ApplicationMetaModel metaModel, AnnotationKey key, AnnotationState removed) {
    if (key.getType().equals(AJAX)) {
      ElementHandle.Package handle = metaModel.getHandle();
      enabledMap.remove(handle);
    }
  }

  @Override
  public void init(ApplicationMetaModel application) {
  }

  @Override
  public void destroy(ApplicationMetaModel application) {
    enabledMap.remove(application.getHandle());
  }

  @Override
  public JSON getDescriptor(ApplicationMetaModel application) {
    ElementHandle.Package handle = application.getHandle();
    Boolean enabled = enabledMap.get(handle);
    return enabled != null && enabled ? new JSON() : null;
  }
}
