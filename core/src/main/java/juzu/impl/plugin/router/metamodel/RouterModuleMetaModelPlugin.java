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

package juzu.impl.plugin.router.metamodel;

import juzu.Route;
import juzu.impl.common.JSON;
import juzu.impl.compiler.ElementHandle;
import juzu.impl.compiler.ProcessingContext;
import juzu.impl.metamodel.AnnotationKey;
import juzu.impl.metamodel.AnnotationState;
import juzu.impl.plugin.application.metamodel.ApplicationMetaModel;
import juzu.impl.plugin.module.metamodel.ModuleMetaModel;
import juzu.impl.plugin.module.metamodel.ModuleMetaModelPlugin;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RouterModuleMetaModelPlugin extends ModuleMetaModelPlugin {

  public RouterModuleMetaModelPlugin() {
    super("router");
  }

  @Override
  public Set<Class<? extends Annotation>> init(ProcessingContext env) {
    return Collections.<Class<? extends Annotation>>singleton(Route.class);
  }

  private RouterMetaModel getRoutes(ModuleMetaModel metaModel, boolean create) {
    RouterMetaModel routes = metaModel.getChild(RouterMetaModel.KEY);
    if (routes == null && create) {
      metaModel.addChild(RouterMetaModel.KEY, routes = new RouterMetaModel());
    }
    return routes;
  }

  @Override
  public void processAnnotationAdded(ModuleMetaModel metaModel, AnnotationKey key, AnnotationState added) {
    if (key.getType().equals(RouteMetaModel.FQN) && key.getElement() instanceof ElementHandle.Package) {
      getRoutes(metaModel, true).annotations.put(key.getElement(), added);
    }
  }

  @Override
  public void processAnnotationRemoved(ModuleMetaModel metaModel, AnnotationKey key, AnnotationState removed) {
    if (key.getType().equals(RouteMetaModel.FQN) && key.getElement() instanceof ElementHandle.Package) {
      getRoutes(metaModel, true).annotations.remove(key.getElement());
    }
  }

  @Override
  public void postProcessEvents(ModuleMetaModel metaModel) {
    RouterMetaModel router = getRoutes(metaModel, false);
    if (router != null) {
      RouteMetaModel root = new RouteMetaModel();
      for (ApplicationMetaModel application : metaModel.getChildren(ApplicationMetaModel.class)) {
        AnnotationState annotation = router.annotations.get(application.getHandle());
        if (annotation != null) {
          String path = (String)annotation.get("value");
          Integer priority = (Integer)annotation.get("priority");
          RouteMetaModel route = root.addChild(priority != null ? priority : 0, path);
          if (route.getTarget("application") != null) {
            throw RouterMetaModel.ROUTER_DUPLICATE_ROUTE.failure(metaModel.processingContext.get(application.getHandle()), path);
          } else {
            route.setTarget("application", application.getName().toString());
          }
        }
      }
      router.root = root;
      }
    }

  @Override
  public JSON getDescriptor(ModuleMetaModel metaModel) {
    RouterMetaModel router = getRoutes(metaModel, false);
    return router != null && router.root != null ? router.root.toJSON() : null;
  }
}
