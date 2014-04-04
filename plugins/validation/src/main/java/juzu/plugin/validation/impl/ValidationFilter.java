/*
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
 */package juzu.plugin.validation.impl;

import juzu.Response;
import juzu.impl.plugin.controller.ControllerPlugin;
import juzu.impl.request.ControlParameter;
import juzu.impl.request.ControllerHandler;
import juzu.impl.request.RequestFilter;
import juzu.impl.request.Stage;
import juzu.plugin.validation.ValidationError;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.ParameterNameProvider;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;

/**
 * @author Julien Viet
 */
public class ValidationFilter implements RequestFilter<Stage.Invoke>, ParameterNameProvider {

  /** . */
  private Validator validator;

  /** . */
  private final ControllerPlugin controllerPlugin;

  @Inject
  public ValidationFilter(ControllerPlugin controllerPlugin) {
    this.controllerPlugin = controllerPlugin;
  }

  @Override
  public Class<Stage.Invoke> getStageType() {
    return Stage.Invoke.class;
  }

  @PostConstruct
  public void start() {
    ValidatorFactory factory = Validation.byDefaultProvider().configure().parameterNameProvider(this).buildValidatorFactory();
    validator = factory.getValidator();
  }

  @Override
  public Response handle(Stage.Invoke argument) {
    if (validator != null) {
      Set<ConstraintViolation<Object>> violations = validator.forExecutables().validateParameters(argument.getController(), argument.getMethod(), argument.getArguments());
      if (violations.size() > 0) {
        return new ValidationError(violations);
      }
    }
    return argument.invoke();
  }

  @Override
  public List<String> getParameterNames(Constructor<?> constructor) {
    return getParameterNames( constructor.getParameterTypes().length );
  }

  @Override
  public List<String> getParameterNames(Method method) {
    ControllerHandler<?> handler = controllerPlugin.getDescriptor().getHandler(method);
    if (handler != null) {
      List<ControlParameter> parameters = handler.getParameters();
      ArrayList<String> parameterNames = new ArrayList<String>(parameters.size());
      for (ControlParameter parameter : parameters) {
        parameterNames.add(parameter.getName());
      }
      return parameterNames;
    } else {
      return getParameterNames( method.getParameterTypes().length );
    }
  }

  private List<String> getParameterNames(int parameterCount) {
    List<String> parameterNames = newArrayList();

    for ( int i = 0; i < parameterCount; i++ ) {
      parameterNames.add( getPrefix() + i );
    }

    return parameterNames;
  }

  /**
   * Returns the prefix to be used for parameter names. Defaults to {@code arg} as per
   * the spec. Can be overridden to create customized name providers.
   *
   * @return The prefix to be used for parameter names.
   */
  protected String getPrefix() {
    return "arg";
  }
}
