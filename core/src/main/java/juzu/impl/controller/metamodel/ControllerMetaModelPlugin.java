package juzu.impl.controller.metamodel;

import juzu.Application;
import juzu.Param;
import juzu.Response;
import juzu.URLBuilder;
import juzu.impl.application.ApplicationContext;
import juzu.impl.application.metamodel.ApplicationMetaModel;
import juzu.impl.application.metamodel.ApplicationMetaModelPlugin;
import juzu.impl.application.metamodel.ApplicationsMetaModel;
import juzu.impl.compiler.AnnotationData;
import juzu.impl.compiler.CompilationException;
import juzu.impl.compiler.ElementHandle;
import juzu.impl.compiler.ProcessingContext;
import juzu.impl.controller.descriptor.ControllerBean;
import juzu.impl.controller.descriptor.ControllerMethod;
import juzu.impl.controller.descriptor.ControllerParameter;
import juzu.impl.metamodel.MetaModel;
import juzu.impl.metamodel.MetaModelEvent;
import juzu.impl.metamodel.MetaModelObject;
import juzu.impl.request.Request;
import juzu.impl.utils.Cardinality;
import juzu.impl.utils.FQN;
import juzu.impl.utils.JSON;
import juzu.impl.utils.Tools;
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

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ControllerMetaModelPlugin extends ApplicationMetaModelPlugin {

  /** . */
  private static final String CONTROLLER_METHOD = ControllerMethod.class.getSimpleName();

  /** . */
  private static final String CONTROLLER_DESCRIPTOR = ControllerBean.class.getSimpleName();

  /** . */
  private static final String CONTROLLER_PARAMETER = ControllerParameter.class.getSimpleName();

  /** . */
  private static final String PHASE = Phase.class.getSimpleName();

  /** . */
  private static final String TOOLS = Tools.class.getSimpleName();

  /** . */
  private static final String RESPONSE = Response.Update.class.getSimpleName();

  /** . */
  public static final String CARDINALITY = Cardinality.class.getSimpleName();

  /** . */
  private HashSet<ControllerMetaModel> written = new HashSet<ControllerMetaModel>();

  @Override
  public void postConstruct(ApplicationMetaModel application) {
    ControllersMetaModel controllers = new ControllersMetaModel();
    PackageElement pkg = application.model.env.get(application.getHandle());
    AnnotationMirror annotation = Tools.getAnnotation(pkg, Application.class.getName());
    AnnotationData values = AnnotationData.create(annotation);
    Boolean escapeXML = (Boolean)values.get("escapeXML");
    ElementHandle.Class defaultControllerElt = (ElementHandle.Class)values.get("defaultController");
    controllers.escapeXML = escapeXML;
    controllers.defaultController = defaultControllerElt != null ? defaultControllerElt.getFQN() : null;
    application.addChild(ControllersMetaModel.KEY, controllers);
  }

  @Override
  public void processAnnotation(ApplicationMetaModel application, Element element, String fqn, AnnotationData data) throws CompilationException {
    ControllersMetaModel ac = application.getChild(ControllersMetaModel.KEY);
    if (fqn.equals("juzu.View") || fqn.equals("juzu.Action") || fqn.equals("juzu.Resource")) {
      ExecutableElement methodElt = (ExecutableElement)element;
      MetaModel.log.log("Processing controller method " + methodElt + " found on type " + methodElt.getEnclosingElement());
      TypeElement controllerElt = (TypeElement)methodElt.getEnclosingElement();
      ElementHandle.Class handle = ElementHandle.Class.create(controllerElt);
      ControllerMetaModel controller = ac.get(handle);
      if (controller == null) {
        ac.add(controller = new ControllerMetaModel(handle));
      }
      controller.addMethod(
        application.model,
        methodElt,
        fqn,
        data
      );
    }
  }

  @Override
  public void postProcessAnnotations(ApplicationMetaModel application) {
    for (ControllerMetaModel controller : application.getControllers()) {
      if (controller.modified) {
        controller.modified = false;
        controller.queue(MetaModelEvent.createUpdated(controller));
      }
    }
  }

  @Override
  public void processEvent(ApplicationsMetaModel applications, MetaModelEvent event) {
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

    //
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
    for (ControllerMetaModel controller : application.getControllers()) {
      for (ControllerMethodMetaModel method : controller.getMethods()) {
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

  private void emitController(ProcessingContext env, ControllerMetaModel controller) throws CompilationException {
    FQN fqn = controller.getHandle().getFQN();
    Element origin = env.get(controller.getHandle());
    Collection<ControllerMethodMetaModel> methods = controller.getMethods();
    Writer writer = null;
    try {
      JavaFileObject file = env.createSourceFile(fqn.getName() + "_", origin);
      writer = file.openWriter();

      //
      writer.append("package ").append(fqn.getPackageName()).append(";\n");

      // Imports
      writer.append("import ").append(Tools.getImport(ControllerMethod.class)).append(";\n");
      writer.append("import ").append(Tools.getImport(ControllerParameter.class)).append(";\n");
      writer.append("import ").append(Tools.getImport(Tools.class)).append(";\n");
      writer.append("import ").append(Tools.getImport(Arrays.class)).append(";\n");
      writer.append("import ").append(Tools.getImport(Phase.class)).append(";\n");
      writer.append("import ").append(Tools.getImport(URLBuilder.class)).append(";\n");
      writer.append("import ").append(Tools.getImport(ApplicationContext.class)).append(";\n");
      writer.append("import ").append(Tools.getImport(MimeContext.class)).append(";\n");
      writer.append("import ").append(Tools.getImport(ActionContext.class)).append(";\n");
      writer.append("import ").append(Tools.getImport(Response.Update.class)).append(";\n");
      writer.append("import ").append(Tools.getImport(ControllerBean.class)).append(";\n");
      writer.append("import ").append(Tools.getImport(Generated.class)).append(";\n");
      writer.append("import ").append(Tools.getImport(Cardinality.class)).append(";\n");
      writer.append("import ").append(Tools.getImport(Request.class)).append(";\n");

      // Open class
      writer.append("@Generated(value={})\n");
      writer.append("public class ").append(fqn.getSimpleName()).append("_ {\n");

      //
      int index = 0;
      for (ControllerMethodMetaModel method : methods) {
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
        if (method.getPhase() == Phase.RENDER) {
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
      MetaModel.log.log("Generated controller companion " + fqn.getName() + "_" + " as " + file.toUri());
    }
    catch (IOException e) {
      throw ControllerMetaModel.CANNOT_WRITE_CONTROLLER_COMPANION.failure(e, origin, controller.getHandle().getFQN());
    }
    finally {
      Tools.safeClose(writer);
    }
  }
}
