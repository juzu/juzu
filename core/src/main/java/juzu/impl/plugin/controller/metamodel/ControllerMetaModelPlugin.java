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

package juzu.impl.plugin.controller.metamodel;

import juzu.Action;
import juzu.Application;
import juzu.Consumes;
import juzu.Resource;
import juzu.View;
import juzu.impl.common.Name;
import juzu.impl.plugin.application.metamodel.ApplicationMetaModel;
import juzu.impl.plugin.application.metamodel.ApplicationMetaModelPlugin;
import juzu.impl.metamodel.AnnotationKey;
import juzu.impl.metamodel.AnnotationState;
import juzu.impl.compiler.ProcessingException;
import juzu.impl.compiler.ElementHandle;
import juzu.impl.compiler.ProcessingContext;
import juzu.impl.plugin.module.metamodel.ModuleMetaModel;
import juzu.impl.request.BeanParameter;
import juzu.impl.request.ContextualParameter;
import juzu.impl.request.ControlParameter;
import juzu.impl.request.Handler;
import juzu.impl.request.PhaseParameter;
import juzu.impl.plugin.controller.descriptor.ControllerDescriptor;
import juzu.impl.metamodel.MetaModelEvent;
import juzu.impl.metamodel.MetaModelObject;
import juzu.impl.request.Request;
import juzu.impl.common.Cardinality;
import juzu.impl.common.JSON;
import juzu.impl.common.Tools;
import juzu.impl.value.ValueType;
import juzu.processor.MainProcessor;
import juzu.request.Phase;

import javax.annotation.Generated;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ControllerMetaModelPlugin extends ApplicationMetaModelPlugin {

  /** . */
  private static final String METHOD_DESCRIPTOR = Handler.class.getSimpleName();

  /** . */
  private static final String CONTROLLER_DESCRIPTOR = ControllerDescriptor.class.getSimpleName();

  /** . */
  private static final String PARAMETER = ControlParameter.class.getSimpleName();

  /** . */
  private static final String PHASE_PARAMETER = PhaseParameter.class.getSimpleName();

  /** . */
  private static final String CONTEXTUAL_PARAMETER = ContextualParameter.class.getSimpleName();

  /** . */
  private static final String BEAN_PARAMETER = BeanParameter.class.getSimpleName();

  /** . */
  private static final String PHASE = Phase.class.getSimpleName();

  /** . */
  private static final String TOOLS = Tools.class.getSimpleName();

  /** . */
  public static final String CARDINALITY = Cardinality.class.getSimpleName();

  /** . */
  private HashSet<ControllerMetaModel> written = new HashSet<ControllerMetaModel>();

  /** . */
  private static final String SERVICES = "META-INF/services/" + ValueType.class.getName();

  /** . */
  final HashSet<ElementHandle.Type> valueTypes = new HashSet<ElementHandle.Type>();

  public ControllerMetaModelPlugin() {
    super("controller");
  }

  public Set<Class<? extends java.lang.annotation.Annotation>> init(ProcessingContext env) {
    return Tools.<Class<? extends java.lang.annotation.Annotation>>set(View.class, Action.class, Consumes.class, Resource.class);
  }

  @Override
  public void postActivate(ModuleMetaModel applications) {
    valueTypes.clear();
    for (ValueType<?> valueType : ValueType.DEFAULT) {
      for (Class<?> type : valueType.getTypes()) {
        valueTypes.add(ElementHandle.Type.create(Name.create(type)));
      }
    }
    Enumeration<URL> services;
    try {
      services = MainProcessor.class.getClassLoader().getResources(SERVICES);
    }
    catch (IOException e) {
      services = null;
    }
    if (services != null) {
      while (services.hasMoreElements()) {
        URL url = services.nextElement();
        try {
          InputStream in = url.openStream();
          BufferedReader reader = new BufferedReader(new InputStreamReader(in, Tools.UTF_8));
          while (true) {
            String line = reader.readLine();
            if (line != null) {
              String providerName = line.trim();
              TypeElement provider = applications.getProcessingContext().getTypeElement(providerName);
              if (provider.getKind() == ElementKind.CLASS) {
                List<? extends TypeMirror> superTypes = applications.getProcessingContext().directSupertypes((TypeMirror)provider.asType());
                if (superTypes.size() > 0) {
                  TypeMirror superType = superTypes.get(0);
                  if (superType.getKind() == TypeKind.DECLARED) {
                    DeclaredType declaredTypeSuper = (DeclaredType)superType;
                    TypeElement declaredSuperElement = (TypeElement)declaredTypeSuper.asElement();
                    if (ValueType.class.getName().equals(declaredSuperElement.getQualifiedName().toString())) {
                      TypeMirror argument = declaredTypeSuper.getTypeArguments().get(0);
                      if (argument instanceof DeclaredType) {
                        DeclaredType declaredArgumentType = (DeclaredType)argument;
                        Element declaredArgumentElement = declaredArgumentType.asElement();
                        if (declaredArgumentElement instanceof TypeElement) {
                          // Here we are
                          ElementHandle.Type valueType = ElementHandle.Type.create((TypeElement) declaredArgumentElement);
                          valueTypes.add(valueType);
                        }
                      }
                    } else {
                      // ?
                    }
                  } else {
                    // ?
                  }
                } else {
                  // This is likely an interface
                }
              }
            } else {
              break;
            }
          }
        }
        catch (IOException e) {
          // ?
        }
      }
    }
  }

  @Override
  public void init(ApplicationMetaModel application) {
    ControllersMetaModel controllers = new ControllersMetaModel(this);
    PackageElement pkg = application.model.processingContext.get(application.getHandle());
    AnnotationMirror annotation = Tools.getAnnotation(pkg, Application.class.getName());
    AnnotationState values = AnnotationState.create(annotation);
    Boolean escapeXML = (Boolean)values.get("escapeXML");
    ElementHandle.Type defaultControllerElt = (ElementHandle.Type)values.get("defaultController");
    controllers.escapeXML = escapeXML;
    controllers.defaultController = defaultControllerElt != null ? defaultControllerElt.getName() : null;
    application.addChild(ControllersMetaModel.KEY, controllers);
  }

  @Override
  public void processAnnotationAdded(ApplicationMetaModel application, AnnotationKey key, AnnotationState added) {
    ElementHandle.Method methodHandle = (ElementHandle.Method)key.getElement();
    ElementHandle.Type controllerHandle = methodHandle.getType();
    ControllersMetaModel controllers = application.getChild(ControllersMetaModel.KEY);
    ControllerMetaModel controller = controllers.get(controllerHandle);
    if (controller == null) {
      controllers.add(controller = new ControllerMetaModel(controllerHandle));
    }
    controller.addMethod(application.model, key, added);
  }

  @Override
  public void processAnnotationRemoved(ApplicationMetaModel metaModel, AnnotationKey key, AnnotationState removed) {
    ElementHandle.Method methodHandle = (ElementHandle.Method)key.getElement();
    ElementHandle.Type controllerHandle = ElementHandle.Type.create(methodHandle.getTypeName());
    ControllersMetaModel controllers = metaModel.getChild(ControllersMetaModel.KEY);
    ControllerMetaModel controller = controllers.get(controllerHandle);
    if (controller != null) {
      controller.removeMethod(methodHandle);
      if (controller.getHandlers().isEmpty()) {
        controller.remove();
      }
    }
  }

  @Override
  public void postProcessAnnotations(ApplicationMetaModel application) {
    for (ControllerMetaModel controller : application.getChild(ControllersMetaModel.KEY)) {
      if (controller.modified) {
        controller.modified = false;
        controller.queue(MetaModelEvent.createUpdated(controller));
      }
    }
  }

  @Override
  public void processEvent(ApplicationMetaModel application, MetaModelEvent event) {
    MetaModelObject obj = event.getObject();
    if (obj instanceof ControllerMetaModel) {
      switch (event.getType()) {
        case MetaModelEvent.BEFORE_REMOVE:
          break;
        case MetaModelEvent.UPDATED:
        case MetaModelEvent.AFTER_ADD:
          ControllerMetaModel controller = (ControllerMetaModel)obj;
          written.add(controller);
          break;
      }
    }
  }


  @Override
  public JSON getDescriptor(ApplicationMetaModel application) {
    ControllersMetaModel ac = application.getChild(ControllersMetaModel.KEY);

    // Build routes configuration
    ArrayList<String> controllers = new ArrayList<String>();
    for (ControllerMetaModel controller : ac) {
      controllers.add(controller.getHandle().getName() + "_");
    }

    //
    JSON config = new JSON();
    config.set("default", ac.defaultController != null ? ac.defaultController.toString() : null);
    config.set("escapeXML", ac.escapeXML);
    config.map("controllers", controllers);

    //
    return config;
  }

  /** . */
  private static final HashMap<Phase, String> DISPATCH_TYPE = new HashMap<Phase, String>();

  static
  {
    DISPATCH_TYPE.put(Phase.ACTION, Tools.getName(Phase.Action.Dispatch.class));
    DISPATCH_TYPE.put(Phase.VIEW, Tools.getName(Phase.View.Dispatch.class));
    DISPATCH_TYPE.put(Phase.RESOURCE, Tools.getName(Phase.Resource.Dispatch.class));
  }

  @Override
  public void postProcessEvents(ApplicationMetaModel application) {

    // Validate abstract
    for (ControllerMetaModel controller : application.getChild(ControllersMetaModel.KEY)) {
      TypeElement controllerElt = application.getProcessingContext().get(controller.getHandle());
      if (controllerElt.getModifiers().contains(Modifier.ABSTRACT)) {
        throw ControllerMetaModel.CONTROLLER_IS_ABSTRACT.failure(controllerElt, controller.handle.getName());
      }
    }

    // Check everything is OK here
//    for (ControllerMetaModel controller : application.getChild(ControllersMetaModel.KEY)) {
//      for (MethodMetaModel method : controller.getMethods()) {
//        ExecutableElement executableElt = application.model.processingContext.get(method.handle);
//        Iterator<? extends VariableElement> i = executableElt.getParameters().iterator();
//        for (ParameterMetaModel parameter : method.parameters) {
//          VariableElement ve = i.next();
//          if (parameter instanceof InvocationParameterMetaModel) {
//            InvocationParameterMetaModel invocationParameter = (InvocationParameterMetaModel)parameter;
//            TypeElement te = application.model.processingContext.get(invocationParameter.getType());
//            if (!te.toString().equals("java.lang.String") && te.getAnnotation(Mapped.class) == null) {
//              // We should find out who was compiled the bean or the type containing a ref to the class
//              throw ControllerMetaModel.CONTROLLER_METHOD_PARAMETER_NOT_RESOLVED.failure(ve, ve.getSimpleName());
//            }
//          }
//        }
//      }
//    }

    // Emit controllers
    for (Iterator<ControllerMetaModel> i = written.iterator();i.hasNext();) {
      ControllerMetaModel controller = i.next();
      i.remove();
      emitController(application.model.processingContext, controller);
    }
  }

  private void emitController(ProcessingContext env, ControllerMetaModel controller) throws ProcessingException {
    Name fqn = controller.getHandle().getName();
    Element origin = env.get(controller.getHandle());
    Collection<HandlerMetaModel> methods = controller.getHandlers();
    Writer writer = null;
    try {
      JavaFileObject file = env.createSourceFile(fqn + "_", origin);
      writer = file.openWriter();

      //
      writer.append("package ").append(fqn.getParent()).append(";\n");

      // Imports
      writer.append("import ").append(Handler.class.getCanonicalName()).append(";\n");
      writer.append("import ").append(ControlParameter.class.getCanonicalName()).append(";\n");
      writer.append("import ").append(PhaseParameter.class.getCanonicalName()).append(";\n");
      writer.append("import ").append(ContextualParameter.class.getCanonicalName()).append(";\n");
      writer.append("import ").append(BeanParameter.class.getCanonicalName()).append(";\n");
      writer.append("import ").append(Tools.class.getCanonicalName()).append(";\n");
      writer.append("import ").append(Arrays.class.getCanonicalName()).append(";\n");
      writer.append("import ").append(Phase.class.getCanonicalName()).append(";\n");
      writer.append("import ").append(ControllerDescriptor.class.getCanonicalName()).append(";\n");
      writer.append("import ").append(Generated.class.getCanonicalName()).append(";\n");
      writer.append("import ").append(Cardinality.class.getCanonicalName()).append(";\n");
      writer.append("import ").append(Request.class.getCanonicalName()).append(";\n");

      // Open class
      writer.append("@Generated(value={})\n");
      writer.append("public class ").append(fqn.getIdentifier()).append("_ {\n");

      // Class literal
      writer.append("private static final Class<").append(fqn).append("> TYPE = ").append(fqn).append(".class;\n");

      //
      int index = 0;
      for (HandlerMetaModel method : methods) {

        //
        String methodRef = "method_" + index++;

        // Method constant
        writer.append("private static final ").append(METHOD_DESCRIPTOR).append("<");
        Tools.nameOf(method.getPhase().getClass(), writer);
        writer.append("> ").append(methodRef).append(" = ");
        writer.append("new ").append(METHOD_DESCRIPTOR).append("<");
        Tools.nameOf(method.getPhase().getClass(), writer);
        writer.append(">(");
        if (method.getId() != null) {
          writer.append("\"").append(method.getId()).append("\",");
        }
        else {
          writer.append("null,");
        }
        writer.append(PHASE).append(".").append(method.getPhase().name()).append(",");
        writer.append("TYPE,");
        writer.append(TOOLS).append(".safeGetMethod(TYPE,\"").append(method.getName()).append("\"");
        for (ParameterMetaModel parameter : method.getParameters()) {
          writer.append(",").append(parameter.type).append(".class");
        }
        writer.append(')');
        writer.append(", Arrays.<").append(PARAMETER).append(">asList(");
        for (int i = 0;i < method.getParameters().size();i++) {
          ParameterMetaModel parameter = method.getParameters().get(i);
          if (i > 0) {
            writer.append(',');
          }
          if (parameter instanceof BeanParameterMetaModel) {
            writer.append("new ").
                append(BEAN_PARAMETER).append('(').
                append('"').append(parameter.getName()).append('"').append(',').
                append(parameter.type).append(".class").
                append(')');
          } else if (parameter instanceof PhaseParameterMetaModel) {
            PhaseParameterMetaModel phaseParameter = (PhaseParameterMetaModel)parameter;
            writer.append("new ").
                append(PHASE_PARAMETER).append('(').
                append('"').append(parameter.getName()).append('"').append(',').
                append(parameter.type).append(".class").append(',').
                append(phaseParameter.valueType).append(".class").append(',').
                append(CARDINALITY).append('.').append(phaseParameter.getCardinality().name()).append(',');
            if (phaseParameter.getAlias() != null) {
              writer.append('"').append(phaseParameter.getAlias()).append('"');
            } else {
              writer.append("null");
            }
            writer.append(')');
          } else {
            writer.append("new ").
                append(CONTEXTUAL_PARAMETER).append('(').
                append('"').append(parameter.getName()).append('"').append(',').
                append(parameter.type).append(".class").
                append(')');
          }
        }
        writer.append(')');
        writer.append(");\n");

        //
        String dispatchType = DISPATCH_TYPE.get(method.getPhase());

        // Build list of invocation parameters, i.e phase+bean parameters
        ArrayList<ParameterMetaModel> parameters = new ArrayList<ParameterMetaModel>(method.getParameters().size());
        for (ParameterMetaModel parameter : method.getParameters()) {
          if (parameter instanceof PhaseParameterMetaModel || parameter instanceof BeanParameterMetaModel) {
            parameters.add(parameter);
          }
        }

        // We don't generate dispatch for event phase
        if (method.getPhase() != Phase.EVENT) {
          // Dispatch literal
          writer.append("public static ").append(dispatchType).append(" ").append(method.getName()).append("(");
          for (int i = 0;i < parameters.size();i++) {
            ParameterMetaModel parameter = parameters.get(i);
            if (i > 0) {
              writer.append(',');
            }
            writer.append(parameter.type).append(" ").append(parameter.getName());
          }
          writer.append(") { return Request.create").append(method.getPhase().getClass().getSimpleName()).append("Dispatch(").append(methodRef);
          switch (parameters.size()) {
            case 0:
              break;
            case 1:
              writer.append(",(Object)").append(parameters.get(0).getName());
              break;
            default:
              writer.append(",new Object[]{");
              for (int j = 0;j < parameters.size();j++) {
                if (j > 0) {
                  writer.append(",");
                }
                writer.append(parameters.get(j).getName());
              }
              writer.append('}');
              break;
          }
          writer.append("); }\n");
        }
      }

      //
      writer.append("public static final ").append(CONTROLLER_DESCRIPTOR).append(" DESCRIPTOR = new ").append(CONTROLLER_DESCRIPTOR).append("(");
      writer.append("TYPE,Arrays.<").append(METHOD_DESCRIPTOR).append("<?>>asList(");
      for (int j = 0;j < methods.size();j++) {
        if (j > 0) {
          writer.append(',');
        }
        writer.append("method_").append(Integer.toString(j));
      }
      writer.append(")");
      writer.append(");\n");

      // Close class
      writer.append("}\n");

      //
      env.info("Generated controller companion " + fqn + "_" + " as " + file.toUri());
    }
    catch (IOException e) {
      throw ControllerMetaModel.CANNOT_WRITE_CONTROLLER_COMPANION.failure(e, origin, controller.getHandle().getName());
    }
    finally {
      Tools.safeClose(writer);
    }
  }
}
