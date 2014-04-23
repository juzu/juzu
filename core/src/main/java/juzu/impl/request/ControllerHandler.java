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

package juzu.impl.request;

import juzu.impl.common.AbstractAnnotatedElement;
import juzu.impl.common.MethodHandle;
import juzu.impl.common.Tools;
import juzu.request.Phase;

import java.beans.Introspector;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A controller handler.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public final class ControllerHandler<P extends Phase> {

  /** An optional id. */
  private final String id;

  /** The phase this handler targets. */
  private final P phase;

  /** The type of the controller owning this handler. */
  private final Class<?> type;

  /** The corresponding java method. */
  private final Method method;

  /** The handler parameters as a list. */
  private final List<ControlParameter> parameterList;

  /** The handler parameters as a map. */
  private final Map<String, ControlParameter> parameterMap;

  /** . */
  final boolean requiresPrefix;

  /** The handle corresponding to the {@link #method} field. */
  private final MethodHandle handle;

  public ControllerHandler(
      String id,
      P phase,
      Class<?> type,
      Method method,
      List<ControlParameter> parameterList) {

    // Enhance parameter list
    Class<?>[] parameterTypes = method.getParameterTypes();
    Type[] genericParameterTypes = method.getGenericParameterTypes();
    final Annotation[][] parameterAnnotations = method.getParameterAnnotations();
    parameterList = new ArrayList<ControlParameter>(parameterList);
    for (int i = 0;i < parameterList.size();i++) {
      ControlParameter parameter = parameterList.get(i);
      AnnotatedElement annotations = AbstractAnnotatedElement.wrap(parameterAnnotations[i]);
      if (parameter instanceof ContextualParameter) {
        Type genericParameterType = genericParameterTypes[i];
        parameterList.set(i, new ContextualParameter(parameter.getName(), annotations, parameterTypes[i], genericParameterType));
      } else if (parameter instanceof PhaseParameter) {
        PhaseParameter phaseParameter = (PhaseParameter)parameter;
        parameterList.set(i, new PhaseParameter(
            parameter.getName(),
            annotations,
            parameterTypes[i],
            phaseParameter.getValueType(),
            phaseParameter.getCardinality(),
            phaseParameter.getAlias()));
      } else {
        parameterList.set(i , new BeanParameter(parameter.getName(), annotations, parameter.getType()));
      }
    }

    //
    LinkedHashMap<String, ControlParameter> argumentMap = new LinkedHashMap<String, ControlParameter>();
    for (ControlParameter argument : parameterList) {
      argumentMap.put(argument.getName(), argument);
    }

    //
    boolean requiresPrefix = false;
    HashSet<String> set = new HashSet<String>();
    for (ControlParameter parameter : parameterList) {
      if (parameter instanceof PhaseParameter) {
        if (!set.add(parameter.getName())) {
          requiresPrefix = true;
          break;
        }
      } else if (parameter instanceof BeanParameter) {
        BeanParameter beanParameter = (BeanParameter)parameter;
        for (Field field : beanParameter.getType().getFields()) {
          if (!set.add(field.getName())) {
            requiresPrefix = true;
            break;
          }
        }
        for (java.lang.reflect.Method beanMethod : beanParameter.getType().getMethods()) {
          String methodName = beanMethod.getName();
          if (methodName.length() > 3 &&
              methodName.startsWith("get") &&
              beanMethod.getParameterTypes().length == 0 &&
              beanMethod.getReturnType() != Void.class) {
            String name = Introspector.decapitalize(methodName.substring("get".length()));
            if (!set.add(name)) {
              requiresPrefix = true;
              break;
            }
          }
        }
      }
    }

    //
    this.id = id;
    this.phase = phase;
    this.type = type;
    this.method = method;
    this.parameterList = Tools.safeUnmodifiableList(parameterList);
    this.parameterMap = Collections.unmodifiableMap(argumentMap);
    this.requiresPrefix = requiresPrefix;
    this.handle = new MethodHandle(method);
  }

  public MethodHandle getHandle() {
    return handle;
  }

  public String getId() {
    return id;
  }

  public P getPhase() {
    return phase;
  }

  public Class<?> getType() {
    return type;
  }

  public Method getMethod() {
    return method;
  }

  public String getName() {
    return method.getName();
  }

  public ControlParameter getParameter(String name) {
    return parameterMap.get(name);
  }

  public List<ControlParameter> getParameters() {
    return parameterList;
  }

  public Set<String> getParameterNames() {
    return parameterMap.keySet();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(getClass().getSimpleName());
    sb.append("[type=").append(type.getName()).append(",method=");
    sb.append(method.getName()).append("(");
    Class<?>[] types = method.getParameterTypes();
    for (int i = 0;i < types.length;i++) {
      if (i > 0) {
        sb.append(',');
      }
      sb.append(parameterList.get(i).getName()).append("=").append(types[i].getName());
    }
    sb.append(")]");
    return sb.toString();
  }
}
