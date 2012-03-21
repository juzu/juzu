package org.juzu.impl.controller.metamodel;

import org.juzu.Application;
import org.juzu.Response;
import org.juzu.URLBuilder;
import org.juzu.impl.application.ApplicationContext;
import org.juzu.impl.application.metamodel.ApplicationMetaModel;
import org.juzu.impl.application.metamodel.ApplicationsMetaModel;
import org.juzu.impl.compiler.CompilationException;
import org.juzu.impl.compiler.ElementHandle;
import org.juzu.impl.compiler.ProcessingContext;
import org.juzu.impl.controller.descriptor.ControllerBean;
import org.juzu.impl.controller.descriptor.ControllerMethod;
import org.juzu.impl.controller.descriptor.ControllerParameter;
import org.juzu.impl.metamodel.MetaModelError;
import org.juzu.impl.metamodel.MetaModel;
import org.juzu.impl.metamodel.MetaModelEvent;
import org.juzu.impl.metamodel.MetaModelObject;
import org.juzu.impl.metamodel.MetaModelPlugin;
import org.juzu.impl.request.Request;
import org.juzu.impl.utils.Cardinality;
import org.juzu.impl.utils.FQN;
import org.juzu.impl.utils.JSON;
import org.juzu.impl.utils.Tools;
import org.juzu.request.ActionContext;
import org.juzu.request.MimeContext;
import org.juzu.request.Phase;

import javax.annotation.Generated;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ControllerMetaModelPlugin extends MetaModelPlugin
{

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

   @Override
   public void processAnnotation(ApplicationMetaModel application, Element element, String fqn, Map<String, Object> values) throws CompilationException
   {
      ControllersMetaModel ac = application.getChild(ControllersMetaModel.KEY);
      if (fqn.equals("org.juzu.View") || fqn.equals("org.juzu.Action") || fqn.equals("org.juzu.Resource"))
      {
         ExecutableElement methodElt = (ExecutableElement)element;
         MetaModel.log.log("Processing controller method " + methodElt + " found on type " + methodElt.getEnclosingElement());
         TypeElement controllerElt = (TypeElement)methodElt.getEnclosingElement();
         ElementHandle.Class handle = ElementHandle.Class.create(controllerElt);
         ControllerMetaModel controller = ac.get(handle);
         if (controller == null)
         {
            ac.add(controller = new ControllerMetaModel(handle));
         }
         controller.addMethod(
            application.model,
            methodElt,
            fqn,
            values
         );
      }
   }

   @Override
   public void postConstruct(ApplicationMetaModel application)
   {
      ControllersMetaModel controllers = new ControllersMetaModel();
      PackageElement pkg = application.model.env.get(application.getHandle());
      AnnotationMirror annotation = Tools.getAnnotation(pkg, Application.class.getName());
      Map<String, Object> values = Tools.foo(annotation);
      Boolean escapeXML = (Boolean)values.get("escapeXML");
      ElementHandle.Class defaultControllerElt = (ElementHandle.Class)values.get("defaultController");
      controllers.escapeXML = escapeXML;
      controllers.defaultController = defaultControllerElt != null ? defaultControllerElt.getFQN() : null;
      application.addChild(ControllersMetaModel.KEY, controllers);
   }

   @Override
   public void processEvent(ApplicationsMetaModel applications, MetaModelEvent event)
   {
      MetaModelObject obj = event.getObject();
      if (obj instanceof ControllerMetaModel)
      {
         switch (event.getType())
         {
            case MetaModelEvent.BEFORE_REMOVE:
               break;
            case MetaModelEvent.UPDATED:
            case MetaModelEvent.AFTER_ADD:
               emitController(applications.getContext(), (ControllerMetaModel)obj);
               break;
         }
      }
   }

   @Override
   public JSON getDescriptor(ApplicationMetaModel application)
   {
      ControllersMetaModel ac = application.getChild(ControllersMetaModel.KEY);

      //
      ArrayList<String> controllers = new ArrayList<String>();
      for (ControllerMetaModel controller : ac)
      {
         controllers.add(controller.getHandle().getFQN().getFullName() + "_");
      }
      
      //
      JSON config = new JSON();
      config.set("default", ac.defaultController);
      config.set("escapeXML", ac.escapeXML);
      config.map("controllers", controllers);

      //
      return config;
   }

   private void emitController(ProcessingContext env, ControllerMetaModel controller) throws CompilationException
   {
      FQN fqn = controller.getHandle().getFQN();
      Element origin = env.get(controller.getHandle());
      Collection<ControllerMethodMetaModel> methods = controller.getMethods();
      Writer writer = null;
      try
      {
         JavaFileObject file = env.createSourceFile(fqn.getFullName() + "_", origin);
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
         for (ControllerMethodMetaModel method : methods)
         {
            String methodRef = "method_" + index++;

            // Method constant
            writer.append("private static final ").append(CONTROLLER_METHOD).append(" ").append(methodRef).append(" = ");
            writer.append("new ").append(CONTROLLER_METHOD).append("(");
            if (method.getId() != null)
            {
               writer.append("\"").append(method.getId()).append("\",");
            }
            else
            {
               writer.append("null,");
            }
            writer.append(PHASE).append(".").append(method.getPhase().name()).append(",");
            writer.append(fqn.getFullName()).append(".class").append(",");
            writer.append(TOOLS).append(".safeGetMethod(").append(fqn.getFullName()).append(".class,\"").append(method.getName()).append("\"");
            for (String parameterType : method.getParameterTypes())
            {
               writer.append(",").append(parameterType).append(".class");
            }
            writer.append(")");
            writer.append(", Arrays.<").append(CONTROLLER_PARAMETER).append(">asList(");
            for (int i = 0;i < method.getParameterNames().size();i++)
            {
               if (i > 0)
               {
                  writer.append(",");
               }
               String parameterName = method.getParameterNames().get(i);
               Cardinality parameterCardinality = method.getParameterCardinalities().get(i);
               writer.append("new ").
                  append(CONTROLLER_PARAMETER).append('(').
                  append('"').append(parameterName).append('"').append(',').
                  append(CARDINALITY).append('.').append(parameterCardinality.name()).append(',').
                  append("null,").
                  append(method.getParameterTypes().get(i)).append(".class").
                  append(')');
            }
            writer.append(")");
            writer.append(");\n");

            // Render builder literal
            if (method.getPhase() == Phase.RENDER)
            {
               writer.append("public static ").append(RESPONSE).append(" ").append(method.getName()).append("(");
               for (int j = 0; j < method.getParameterTypes().size(); j++)
               {
                  if (j > 0)
                  {
                     writer.append(',');
                  }
                  writer.append(method.getParameterTypes().get(j)).append(" ").append(method.getParameterNames().get(j));
               }
               writer.append(") { return ((ActionContext)Request.getCurrent().getContext()).createResponse(").append(methodRef);
               switch (method.getParameterTypes().size())
               {
                  case 0:
                     break;
                  case 1:
                     writer.append(",(Object)").append(method.getParameterNames().get(0));
                     break;
                  default:
                     writer.append(",new Object[]{");
                     for (int j = 0; j < method.getParameterNames().size();j++)
                     {
                        if (j > 0)
                        {
                           writer.append(",");
                        }
                        writer.append(method.getParameterNames().get(j));
                     }
                     writer.append("}");
                     break;
               }
               writer.append("); }\n");
            }

            // URL builder literal
            writer.append("public static URLBuilder ").append(method.getName()).append("URL").append("(");
            for (int j = 0; j < method.getParameterTypes().size(); j++)
            {
               if (j > 0)
               {
                  writer.append(',');
               }
               writer.append(method.getParameterTypes().get(j)).append(" ").append(method.getParameterNames().get(j));
            }
            writer.append(") { return ((MimeContext)Request.getCurrent().getContext()).createURLBuilder(").append(methodRef);
            switch (method.getParameterNames().size())
            {
               case 0:
                  break;
               case 1:
                  writer.append(",(Object)").append(method.getParameterNames().get(0));
                  break;
               default:
                  writer.append(",new Object[]{");
                  for (int j = 0;j < method.getParameterNames().size();j++)
                  {
                     if (j > 0)
                     {
                        writer.append(",");
                     }
                     writer.append(method.getParameterNames().get(j));
                  }
                  writer.append("}");
                  break;
            }
            writer.append("); }\n");
         }

         //
         writer.append("public static final ").append(CONTROLLER_DESCRIPTOR).append(" DESCRIPTOR = new ").append(CONTROLLER_DESCRIPTOR).append("(");
         writer.append(fqn.getSimpleName()).append(".class,Arrays.<").append(CONTROLLER_METHOD).append(">asList(");
         for (int j = 0;j < methods.size();j++)
         {
            if (j > 0)
            {
               writer.append(',');
            }
            writer.append("method_").append(Integer.toString(j));
         }
         writer.append(")");
         writer.append(");\n");

         // Close class
         writer.append("}\n");

         //
         MetaModel.log.log("Generated controller companion " + fqn.getFullName() + "_" + " as " + file.toUri());
      }
      catch (IOException e)
      {
         throw new CompilationException(e, origin, MetaModelError.CANNOT_WRITE_CONTROLLER_COMPANION, controller.getHandle().getFQN());
      }
      finally
      {
         Tools.safeClose(writer);
      }
   }
}
