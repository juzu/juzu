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
import juzu.impl.common.QN;
import juzu.impl.plugin.application.metamodel.ApplicationMetaModel;
import juzu.impl.plugin.application.metamodel.ApplicationMetaModelPlugin;
import juzu.impl.common.JSON;
import juzu.impl.compiler.ElementHandle;
import juzu.impl.compiler.ProcessingContext;
import juzu.impl.plugin.controller.metamodel.ControllerMetaModel;
import juzu.impl.plugin.controller.metamodel.ControllersMetaModel;
import juzu.impl.plugin.controller.metamodel.MethodMetaModel;
import juzu.impl.metamodel.AnnotationKey;
import juzu.impl.metamodel.AnnotationState;
import juzu.impl.plugin.controller.metamodel.ParameterMetaModel;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RouterApplicationMetaModelPlugin extends ApplicationMetaModelPlugin {

  public RouterApplicationMetaModelPlugin() {
    super("router");
  }

  @Override
  public Set<Class<? extends Annotation>> init(ProcessingContext env) {
    return Collections.<Class<? extends Annotation>>singleton(Route.class);
  }

  private RouterMetaModel getRoutes(ApplicationMetaModel metaModel, boolean create) {
    RouterMetaModel routes = metaModel.getChild(RouterMetaModel.KEY);
    if (routes == null && create) {
      metaModel.addChild(RouterMetaModel.KEY, routes = new RouterMetaModel());
    }
    return routes;
  }

  @Override
  public void processAnnotationAdded(ApplicationMetaModel metaModel, AnnotationKey key, AnnotationState added) {
    if (key.getType().equals(RouteMetaModel.FQN)) {
      if (key.getElement() instanceof ElementHandle.Method) {
        getRoutes(metaModel, true).annotations.put(key.getElement(), added);
      } else if (key.getElement().equals(metaModel.getHandle())) {
        getRoutes(metaModel, true).packageRoute = (String)added.get("value");
        getRoutes(metaModel, true).packagePriority = (Integer)added.get("priority");
      }
    }
  }

  @Override
  public void processAnnotationRemoved(ApplicationMetaModel metaModel, AnnotationKey key, AnnotationState removed) {
    if (key.getType().equals(RouteMetaModel.FQN)) {
      if (key.getElement() instanceof ElementHandle.Method) {
        getRoutes(metaModel, true).annotations.remove(key.getElement());
      } else if (key.getElement().equals(metaModel.getHandle())) {
        getRoutes(metaModel, true).packageRoute = null;
        getRoutes(metaModel, true).packagePriority = null;
      }
    }
  }

  @Override
  public void postProcessEvents(ApplicationMetaModel metaModel) {
    RouterMetaModel router = getRoutes(metaModel, false);
    if (router != null) {
      ControllersMetaModel controllers = metaModel.getChild(ControllersMetaModel.KEY);
      if (controllers != null) {
        QN abc = metaModel.getName();
        RouteMetaModel root = new RouteMetaModel(
            router.packageRoute != null ? router.packageRoute : "/" + abc.get(abc.size() - 1),
            router.packagePriority != null ? router.packagePriority : 0);
        for (ControllerMetaModel controller : controllers) {
          for (MethodMetaModel method : controller) {
            AnnotationState annotation = router.annotations.get(method.getHandle());
            if (annotation != null) {
              String path = (String)annotation.get("value");
              Integer priority = (Integer)annotation.get("priority");
              HashMap<String, String> parameters = null;
              for (ParameterMetaModel parameter : method.getParameters()) {
                if (parameter.getPattern() != null) {
                  if (parameters == null) {
                    parameters = new HashMap<String, String>();
                  }
                  parameters.put(parameter.getName(), parameter.getPattern());
                }
              }
              RouteMetaModel route = root.addChild(priority != null ? priority : 0, path, parameters);
              String key = method.getPhase().name();
              if (route.getTarget(key) != null) {
                throw RouterMetaModel.ROUTER_DUPLICATE_ROUTE.failure(metaModel.processingContext.get(method.getHandle()), path);
              } else {
                route.setTarget(key, method.getHandle().getMethodHandle().toString());
              }
            }
          }
        }
        router.root = root;
      }
    }
  }

  @Override
  public JSON getDescriptor(ApplicationMetaModel application) {
    RouterMetaModel router = getRoutes(application, false);
    return router != null && router.root != null ? router.root.toJSON() : null;
  }
}
