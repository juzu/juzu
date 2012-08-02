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

package juzu.impl.plugin.controller.metamodel;

import juzu.Action;
import juzu.Application;
import juzu.Param;
import juzu.Resource;
import juzu.Response;
import juzu.URLBuilder;
import juzu.View;
import juzu.impl.plugin.application.ApplicationContext;
import juzu.impl.plugin.application.metamodel.ApplicationMetaModel;
import juzu.impl.plugin.application.metamodel.ApplicationMetaModelPlugin;
import juzu.impl.metamodel.AnnotationKey;
import juzu.impl.metamodel.AnnotationState;
import juzu.impl.compiler.ProcessingException;
import juzu.impl.compiler.ElementHandle;
import juzu.impl.compiler.ProcessingContext;
import juzu.impl.plugin.controller.descriptor.ControllerDescriptor;
import juzu.impl.plugin.controller.descriptor.MethodDescriptor;
import juzu.impl.plugin.controller.descriptor.ParameterDescriptor;
import juzu.impl.metamodel.MetaModelEvent;
import juzu.impl.metamodel.MetaModelObject;
import juzu.impl.request.Request;
import juzu.impl.common.Cardinality;
import juzu.impl.common.FQN;
import juzu.impl.common.JSON;
import juzu.impl.common.Tools;
import juzu.request.ActionContext;
import juzu.request.MimeContext;
import juzu.request.Phase;

import javax.annotation.Generated;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ControllerMetaModelPlugin extends ApplicationMetaModelPlugin {

  /** . */
  private static final String CONTROLLER_METHOD = MethodDescriptor.class.getSimpleName();

  /** . */
  private static final String CONTROLLER_DESCRIPTOR = ControllerDescriptor.class.getSimpleName();

  /** . */
  private static final String CONTROLLER_PARAMETER = ParameterDescriptor.class.getSimpleName();

  /** . */
  private static final String PHASE = Phase.class.getSimpleName();

  /** . */
  private static final String TOOLS = Tools.class.getSimpleName();

  /** . */
  private static final String RESPONSE = Response.Update.class.getSimpleName();

  /** . */
  public static final String CARDINALITY = Cardinality.class.getSimpleName();

  /** . */
  private static final Set<FQN> NAMES = Tools.set(new FQN(Action.class), new FQN(View.class), new FQN(Resource.class));

  /** . */
  private HashSet<ControllerMetaModel> written = new HashSet<ControllerMetaModel>();

  public ControllerMetaModelPlugin() {
    super("controller");
  }

  public Set<Class<? extends java.lang.annotation.Annotation>> init(ProcessingContext env) {
    return Tools.<Class<? extends java.lang.annotation.Annotation>>set(View.class, Action.class, Resource.class);
  }

  @Override
  public void init(ApplicationMetaModel application) {
    ControllersMetaModel controllers = new ControllersMetaModel();
    PackageElement pkg = application.model.env.get(application.getHandle());
    AnnotationMirror annotation = Tools.getAnnotation(pkg, Application.class.getName());
    AnnotationState values = AnnotationState.create(annotation);
    Boolean escapeXML = (Boolean)values.get("escapeXML");
    ElementHandle.Class defaultControllerElt = (ElementHandle.Class)values.get("defaultController");
    controllers.escapeXML = escapeXML;
    controllers.defaultController = defaultControllerElt != null ? defaultControllerElt.getFQN() : null;
    application.addChild(ControllersMetaModel.KEY, controllers);
  }

  @Override
  public void processAnnotationAdded(ApplicationMetaModel application, AnnotationKey key, AnnotationState added) {
    if (NAMES.contains(key.getType())) {
      ControllersMetaModel ac = application.getChild(ControllersMetaModel.KEY);
      ElementHandle.Method m = (ElementHandle.Method)key.getElement();
      ElementHandle.Class handle = ElementHandle.Class.create(m.getFQN());
      ControllerMetaModel controller = ac.get(handle);
      if (controller == null) {
        ac.add(controller = new ControllerMetaModel(handle));
      }
      controller.addMethod(application.model, key, added);
    }
  }

  @Override
  public void processAnnotationRemoved(ApplicationMetaModel metaModel, AnnotationKey key, AnnotationState removed) {
    if (NAMES.contains(key.getType())) {
      ElementHandle.Method methodHandle = (ElementHandle.Method)key.getElement();
      ElementHandle.Class controllerHandle = ElementHandle.Class.create(methodHandle.getFQN());
      ControllersMetaModel controllers = metaModel.getChild(ControllersMetaModel.KEY);
      ControllerMetaModel controller = controllers.get(controllerHandle);
      if (controller != null) {
        controller.remove(methodHandle);
        if (controller.getMethods().isEmpty()) {
          controller.remove();
        }
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
      controllers.add(controller.getHandle().getFQN().getName() + "_");
    }

    //
    JSON config = new JSON();
    config.set("default", ac.defaultController != null ? ac.defaultController.getName() : null);
    config.set("escapeXML", ac.escapeXML);
    config.map("controllers", controllers);

    //
    return config;
  }

  @Override
  public void postProcessEvents(ApplicationMetaModel application) {
    // Check everything is OK here
    for (ControllerMetaModel controller : application.getChild(ControllersMetaModel.KEY)) {
      for (MethodMetaModel method : controller.getMethods()) {
        ExecutableElement executableElt = application.model.env.get(method.handle);
        Iterator<? extends VariableElement> i = executableElt.getParameters().iterator();
        for (ParameterMetaModel parameter : method.parameters) {
          VariableElement ve = i.next();
          TypeElement te = application.model.env.get(parameter.getType());
          if (!te.toString().equals("java.lang.String") && te.getAnnotation(Param.class) == null) {
            throw ControllerMetaModel.CONTROLLER_METHOD_PARAMETER_NOT_RESOLVED.failure(ve, ve.getSimpleName());
          }
        }
      }
    }

    // Emit controllers
    for (Iterator<ControllerMetaModel> i = written.iterator();i.hasNext();) {
      ControllerMetaModel controller = i.next();
      i.remove();
      emitController(application.model.env, controller);
    }
  }

  private void emitController(ProcessingContext env, ControllerMetaModel controller) throws ProcessingException {
    FQN fqn = controller.getHandle().getFQN();
    Element origin = env.get(controller.getHandle());
    Collection<MethodMetaModel> methods = controller.getMethods();
    Writer writer = null;
    try {
      JavaFileObject file = env.createSourceFile(fqn.getName() + "_", origin);
      writer = file.openWriter();

      //
      writer.append("package ").append(fqn.getPackageName()).append(";\n");

      // Imports
      writer.append("import ").append(Tools.getImport(MethodDescriptor.class)).append(";\n");
      writer.append("import ").append(Tools.getImport(ParameterDescriptor.class)).append(";\n");
      writer.append("import ").append(Tools.getImport(Tools.class)).append(";\n");
      writer.append("import ").append(Tools.getImport(Arrays.class)).append(";\n");
      writer.append("import ").append(Tools.getImport(Phase.class)).append(";\n");
      writer.append("import ").append(Tools.getImport(URLBuilder.class)).append(";\n");
      writer.append("import ").append(Tools.getImport(ApplicationContext.class)).append(";\n");
      writer.append("import ").append(Tools.getImport(MimeContext.class)).append(";\n");
      writer.append("import ").append(Tools.getImport(ActionContext.class)).append(";\n");
      writer.append("import ").append(Tools.getImport(Response.Update.class)).append(";\n");
      writer.append("import ").append(Tools.getImport(ControllerDescriptor.class)).append(";\n");
      writer.append("import ").append(Tools.getImport(Generated.class)).append(";\n");
      writer.append("import ").append(Tools.getImport(Cardinality.class)).append(";\n");
      writer.append("import ").append(Tools.getImport(Request.class)).append(";\n");

      // Open class
      writer.append("@Generated(value={})\n");
      writer.append("public class ").append(fqn.getSimpleName()).append("_ {\n");

      //
      int index = 0;
      for (MethodMetaModel method : methods) {
        String methodRef = "method_" + index++;

        // Method constant
        writer.append("private static final ").append(CONTROLLER_METHOD).append(" ").append(methodRef).append(" = ");
        writer.append("new ").append(CONTROLLER_METHOD).append("(");
        if (method.getId() != null) {
          writer.append("\"").append(method.getId()).append("\",");
        }
        else {
          writer.append("null,");
        }
        writer.append(PHASE).append(".").append(method.getPhase().name()).append(",");
        writer.append(fqn.getName()).append(".class").append(",");
        writer.append(TOOLS).append(".safeGetMethod(").append(fqn.getName()).append(".class,\"").append(method.getName()).append("\"");
        for (ParameterMetaModel param : method.getParameters()) {
          writer.append(",").append(param.declaredType).append(".class");
        }
        writer.append(")");
        writer.append(", Arrays.<").append(CONTROLLER_PARAMETER).append(">asList(");
        for (int i = 0;i < method.getParameters().size();i++) {
          if (i > 0) {
            writer.append(",");
          }
          ParameterMetaModel param = method.getParameter(i);
          writer.append("new ").
            append(CONTROLLER_PARAMETER).append('(').
            append('"').append(param.getName()).append('"').append(',').
            append(CARDINALITY).append('.').append(param.getCardinality().name()).append(',').
            append("null,").
            append(param.declaredType).append(".class").
            append(')');
        }
        writer.append(")");
        writer.append(");\n");

        // Render builder literal
        if (method.getPhase() == Phase.VIEW) {
          writer.append("public static ").append(RESPONSE).append(" ").append(method.getName()).append("(");
          for (int j = 0;j < method.getParameters().size();j++) {
            if (j > 0) {
              writer.append(',');
            }
            ParameterMetaModel param = method.getParameter(j);
            writer.append(param.declaredType).append(" ").append(param.getName());
          }
          writer.append(") { return ((ActionContext)Request.getCurrent().getContext()).createResponse(").append(methodRef);
          switch (method.getParameters().size()) {
            case 0:
              break;
            case 1:
              writer.append(",(Object)").append(method.getParameter(0).getName());
              break;
            default:
              writer.append(",new Object[]{");
              for (int j = 0;j < method.getParameters().size();j++) {
                if (j > 0) {
                  writer.append(",");
                }
                ParameterMetaModel param = method.getParameter(j);
                writer.append(param.getName());
              }
              writer.append("}");
              break;
          }
          writer.append("); }\n");
        }

        // URL builder literal
        writer.append("public static URLBuilder ").append(method.getName()).append("URL").append("(");
        for (int j = 0;j < method.getParameters().size();j++) {
          if (j > 0) {
            writer.append(',');
          }
          ParameterMetaModel param = method.getParameter(j);
          writer.append(param.declaredType).append(" ").append(param.getName());
        }
        writer.append(") { return ((MimeContext)Request.getCurrent().getContext()).createURLBuilder(").append(methodRef);
        switch (method.getParameters().size()) {
          case 0:
            break;
          case 1:
            writer.append(",(Object)").append(method.getParameter(0).getName());
            break;
          default:
            writer.append(",new Object[]{");
            for (int j = 0;j < method.getParameters().size();j++) {
              if (j > 0) {
                writer.append(",");
              }
              writer.append(method.getParameter(j).getName());
            }
            writer.append("}");
            break;
        }
        writer.append("); }\n");
      }

      //
      writer.append("public static final ").append(CONTROLLER_DESCRIPTOR).append(" DESCRIPTOR = new ").append(CONTROLLER_DESCRIPTOR).append("(");
      writer.append(fqn.getSimpleName()).append(".class,Arrays.<").append(CONTROLLER_METHOD).append(">asList(");
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
      env.log("Generated controller companion " + fqn.getName() + "_" + " as " + file.toUri());
    }
    catch (IOException e) {
      throw ControllerMetaModel.CANNOT_WRITE_CONTROLLER_COMPANION.failure(e, origin, controller.getHandle().getFQN());
    }
    finally {
      Tools.safeClose(writer);
    }
  }
}
