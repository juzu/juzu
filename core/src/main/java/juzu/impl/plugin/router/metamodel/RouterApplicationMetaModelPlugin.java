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

package juzu.impl.plugin.router.metamodel;

import juzu.Param;
import juzu.Route;
import juzu.impl.plugin.application.metamodel.ApplicationMetaModel;
import juzu.impl.plugin.application.metamodel.ApplicationMetaModelPlugin;
import juzu.impl.common.JSON;
import juzu.impl.compiler.ElementHandle;
import juzu.impl.compiler.ProcessingContext;
import juzu.impl.plugin.controller.metamodel.PhaseParameterMetaModel;
import juzu.impl.plugin.controller.metamodel.ControllerMetaModel;
import juzu.impl.plugin.controller.metamodel.ControllersMetaModel;
import juzu.impl.plugin.controller.metamodel.MethodMetaModel;
import juzu.impl.metamodel.AnnotationKey;
import juzu.impl.metamodel.AnnotationState;
import juzu.impl.plugin.controller.metamodel.ParameterMetaModel;
import juzu.impl.plugin.router.ParamDescriptor;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
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
    if (key.getElement() instanceof ElementHandle.Method) {
      getRoutes(metaModel, true).annotations.put(key.getElement(), added);
    } else if (key.getElement().equals(metaModel.getHandle())) {
      getRoutes(metaModel, true).packageRoute = (String)added.get("value");
      getRoutes(metaModel, true).packagePriority = (Integer)added.get("priority");
    }
  }

  @Override
  public void processAnnotationRemoved(ApplicationMetaModel metaModel, AnnotationKey key, AnnotationState removed) {
    if (key.getElement() instanceof ElementHandle.Method) {
      getRoutes(metaModel, true).annotations.remove(key.getElement());
    } else if (key.getElement().equals(metaModel.getHandle())) {
      getRoutes(metaModel, true).packageRoute = null;
      getRoutes(metaModel, true).packagePriority = null;
    }
  }

  @Override
  public void postProcessEvents(ApplicationMetaModel metaModel) {
    RouterMetaModel router = getRoutes(metaModel, false);
    if (router != null) {
      ControllersMetaModel controllers = metaModel.getChild(ControllersMetaModel.KEY);
      if (controllers != null) {
        RouteMetaModel root = new RouteMetaModel(
            router.packageRoute != null ? router.packageRoute : null,
            router.packagePriority != null ? router.packagePriority : 0);
        for (ControllerMetaModel controller : controllers) {
          for (MethodMetaModel method : controller) {
            AnnotationState annotation = router.annotations.get(method.getHandle());
            if (annotation != null) {
              String path = (String)annotation.get("value");
              Integer priority = (Integer)annotation.get("priority");
              HashMap<String, ParamDescriptor> parameters = null;
              ExecutableElement exe = metaModel.processingContext.get(method.getHandle());
              for (VariableElement ve : exe.getParameters()) {
                Param param = ve.getAnnotation(Param.class);
                if (param != null) {
                  if (parameters == null) {
                    parameters = new HashMap<String, ParamDescriptor>();
                  }
                  String name = ve.getSimpleName().toString();
                  ParameterMetaModel a = method.parameterBy(name);
                  if (a instanceof PhaseParameterMetaModel) {
                    parameters.put(name, new ParamDescriptor(param.pattern(),  param.preservePath(),  param.captureGroup()));
                  } else {
                    throw new UnsupportedOperationException("Handle me gracefully");
                  }
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
