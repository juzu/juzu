package org.juzu.impl.application.metamodel;

import org.juzu.impl.application.metadata.ApplicationDescriptor;
import org.juzu.impl.compiler.CompilationException;
import org.juzu.impl.metamodel.MetaModelError;
import org.juzu.impl.metamodel.MetaModel;
import org.juzu.impl.metamodel.MetaModelEvent;
import org.juzu.impl.metamodel.MetaModelObject;
import org.juzu.impl.metamodel.MetaModelPlugin;
import org.juzu.impl.metamodel.ProcessingContext;
import org.juzu.impl.request.LifeCyclePlugin;
import org.juzu.impl.utils.FQN;
import org.juzu.impl.utils.JSON;
import org.juzu.impl.utils.Tools;

import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ApplicationMetaModelPlugin extends MetaModelPlugin
{

   /** . */
   private static final String APPLICATION_DESCRIPTOR = ApplicationDescriptor.class.getSimpleName();

   /** . */
   private Map<String, String> moduleConfig;

   @Override
   public void init(MetaModel model)
   {
      model.addChild(ApplicationsMetaModel.KEY, new ApplicationsMetaModel());
      moduleConfig = new HashMap<String, String>();
   }

   @Override
   public void processAnnotation(MetaModel model, Element element, String annotationFQN, Map<String, Object> annotationValues) throws CompilationException
   {
      if (annotationFQN.equals("org.juzu.Application"))
      {
         MetaModel.log.log("Processing application " + element);
         model.getChild(ApplicationsMetaModel.KEY).processApplication((PackageElement)element, annotationFQN, annotationValues);
      }
   }

   @Override
   public void processEvent(MetaModel model, MetaModelEvent event)
   {
      MetaModelObject obj = event.getObject();
      if (obj instanceof ApplicationMetaModel)
      {
         ApplicationMetaModel application = (ApplicationMetaModel)obj;
         if (event.getType() == MetaModelEvent.AFTER_ADD)
         {
            moduleConfig.put(application.getFQN().getSimpleName(), application.getFQN().getFullName());
            emitApplication(model.env, application);
         }
         else if (event.getType() == MetaModelEvent.BEFORE_REMOVE)
         {
            // Should we do something
         }
      }
   }

   @Override
   public void prePassivate(MetaModel model)
   {
      MetaModel.log.log("Emitting config");
      emitConfig(model);
   }

   private void emitApplication(ProcessingContext env, ApplicationMetaModel application) throws CompilationException
   {
      PackageElement elt = env.get(application.getHandle());
      FQN fqn = application.getFQN();

      //
      Writer writer = null;
      try
      {
         JavaFileObject applicationFile = env.createSourceFile(fqn.getFullName(), elt);
         writer = applicationFile.openWriter();

         writer.append("package ").append(fqn.getPackageName()).append(";\n");

         // Imports
         writer.append("import ").append(Tools.getImport(ApplicationDescriptor.class)).append(";\n");

         // Open class
         writer.append("public class ").append(fqn.getSimpleName()).append(" {\n");

         // Singleton
         writer.append("public static final ").append(APPLICATION_DESCRIPTOR).append(" DESCRIPTOR = new ").append(APPLICATION_DESCRIPTOR).append("(");
         writer.append(fqn.getSimpleName()).append(".class");
         writer.append(",\n");
         writer.append("java.util.Arrays.<Class<? extends ");
         writer.append(LifeCyclePlugin.class.getName());
         writer.append(">>asList(");
         List<FQN> plugins = application.getPlugins();
         for (int i = 0;i < plugins.size();i++)
         {
            if (i > 0)
            {
               writer.append(',');
            }
            FQN plugin = plugins.get(i);
            writer.append(plugin.getFullName()).append(".class");
         }
         writer.append(")");
         writer.append(");\n");

         // Close class
         writer.append("}\n");

         //
         MetaModel.log.log("Generated application " + fqn.getFullName() + " as " + applicationFile.toUri());
      }
      catch (IOException e)
      {
         throw new CompilationException(e, elt, MetaModelError.CANNOT_WRITE_APPLICATION, application.getFQN());
      }
      finally
      {
         Tools.safeClose(writer);
      }
   }

   private void emitConfig(MetaModel model)
   {
      JSON config = new JSON();
      config.merge(moduleConfig);

      // Module config
      Writer writer = null;
      try
      {
         //
         FileObject fo = model.env.createResource(StandardLocation.CLASS_OUTPUT, "org.juzu", "config.json");
         writer = fo.openWriter();
         config.toString(writer, 2);
      }
      catch (IOException e)
      {
         throw new CompilationException(e, MetaModelError.CANNOT_WRITE_CONFIG);
      }
      finally
      {
         Tools.safeClose(writer);
      }

      // Application configs
      for (ApplicationMetaModel application : model.getChild(ApplicationsMetaModel.KEY))
      {
         config.clear();

         // Emit config
         for (Map.Entry<String, MetaModelPlugin> entry : model.getPlugins().entrySet())
         {
            JSON pluginConfig = entry.getValue().emitConfig(application);
            if (pluginConfig != null)
            {
               config.set(entry.getKey(), pluginConfig);
            }
         }

         //
         writer = null;
         try
         {
            FileObject fo = model.env.createResource(StandardLocation.CLASS_OUTPUT, application.getFQN().getPackageName(), "config.json");
            writer = fo.openWriter();
            config.toString(writer, 2);
         }
         catch (IOException e)
         {
            throw new CompilationException(e, model.env.get(application.getHandle()), MetaModelError.CANNOT_WRITE_APPLICATION_CONFIG, application.getFQN());
         }
         finally
         {
            Tools.safeClose(writer);
         }
      }
   }
}
