package org.juzu.impl.application.metamodel;

import org.juzu.Application;
import org.juzu.impl.application.metadata.ApplicationDescriptor;
import org.juzu.impl.compiler.CompilationException;
import org.juzu.impl.compiler.ElementHandle;
import org.juzu.impl.compiler.ProcessingContext;
import org.juzu.impl.metamodel.EventQueue;
import org.juzu.impl.metamodel.Key;
import org.juzu.impl.metamodel.MetaModel;
import org.juzu.impl.metamodel.MetaModelError;
import org.juzu.impl.metamodel.MetaModelEvent;
import org.juzu.impl.metamodel.MetaModelObject;
import org.juzu.impl.metamodel.MetaModelPlugin;
import org.juzu.impl.utils.FQN;
import org.juzu.impl.utils.JSON;
import org.juzu.impl.utils.QN;
import org.juzu.impl.utils.Tools;

import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ApplicationsMetaModel extends MetaModelObject implements Iterable<ApplicationMetaModel>
{

   /** . */
   private static final String APPLICATION_DESCRIPTOR = ApplicationDescriptor.class.getSimpleName();

   /** . */
   private Map<String, String> moduleConfig;

   /** . */
   public final static Key<ApplicationsMetaModel> KEY = Key.of(ApplicationsMetaModel.class);

   /** . */
   MetaModel model;

   /** . */
   final Map<BufKey, Map<String, Object>> toProcess = new HashMap<BufKey, Map<String, Object>>();

   /** The meta model plugins. */
   final LinkedHashMap<String, MetaModelPlugin> plugins = new LinkedHashMap<String, MetaModelPlugin>();

   public ApplicationsMetaModel()
   {
      this.moduleConfig = new HashMap<String, String>();
   }

   @Override
   public JSON toJSON()
   {
      JSON json = new JSON();
      json.map("values", getChildren(ApplicationMetaModel.class));
      return json;
   }

   public Iterator<ApplicationMetaModel> iterator()
   {
      return getChildren(ApplicationMetaModel.class).iterator();
   }

   public ProcessingContext getContext()
   {
      return model.env;
   }

   public ApplicationMetaModel get(ElementHandle.Package handle)
   {
      return getChild(Key.of(handle, ApplicationMetaModel.class));
   }

   public void addPlugin(String name, MetaModelPlugin plugin)
   {
      plugins.put(name, plugin);

      //
      plugin.init(this);
   }

   public void postActivate(MetaModel model)
   {
      for (MetaModelPlugin plugin : plugins.values())
      {
         plugin.postActivateApplicationsMetaModel(this);
      }

      //
      for (ApplicationMetaModel application : this)
      {
         for (MetaModelPlugin plugin : plugins.values())
         {
            plugin.postActivate(application);
         }
      }
   }

   public void processAnnotation(MetaModel model, Element element, String annotationFQN, Map<String, Object> annotationValues) throws CompilationException
   {
      PackageElement pkg = model.env.getPackageOf(element);
      QN pkgQN = new QN(pkg.getQualifiedName());

      //
      ApplicationMetaModel found = null;

      //
      if (annotationFQN.equals(Application.class.getName()))
      {
         found = processApplication((PackageElement)element, annotationValues);

         // Process this annotation manually
         for (MetaModelPlugin plugin : plugins.values())
         {
            plugin.processAnnotation(found, element, annotationFQN, annotationValues);
         }
      }
      else
      {
         for (ApplicationMetaModel application : this)
         {
            if (application.fqn.getPackageName().isPrefix(pkgQN))
            {
               found = application;
               break;
            }
         }

         //
         BufKey key = new BufKey(model.env, element, annotationFQN);
         if (found == null)
         {
            toProcess.put(key, annotationValues);
            MetaModel.log.log("Buffering " + key + " = " + annotationValues);
         }
         else
         {
            found.toProcess.put(key, annotationValues);
         }
      }

      // Broadcast annotations
      if (found != null)
      {
         for (Iterator<Map.Entry<BufKey, Map<String, Object>>> i = found.toProcess.entrySet().iterator();i.hasNext();)
         {
            Map.Entry<BufKey, Map<String, Object>> entry = i.next();
            BufKey key = entry.getKey();
            Map<String, Object> annV = entry.getValue();
            Element e = model.env.get(key.element);
            i.remove();
            MetaModel.log.log("Broadcasting annotation " + key + " = " +  annV);
            for (MetaModelPlugin plugin : plugins.values())
            {
               plugin.processAnnotation(found, e, key.annotationFQN, annV);
            }
            found.processed.put(key, annV);
         }
      }
   }

   public void postProcessAnnotations(MetaModel model)
   {
      resolveApplications();

      //
      for (ApplicationMetaModel application : this)
      {
         for (MetaModelPlugin plugin : plugins.values())
         {
            plugin.postProcessAnnotations(application);
         }
      }
   }

   public void processEvents(MetaModel model, EventQueue queue)
   {
      while (queue.hasEvents())
      {
         MetaModelEvent event = queue.popEvent();

         //
         processEvent(event);

         //
         MetaModel.log.log("Processing meta model event " + event.getType() + " " + event.getObject());
         for (MetaModelPlugin plugin : plugins.values())
         {
            plugin.processEvent(this, event);
         }
      }
   }

   private void processEvent(MetaModelEvent event)
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
            moduleConfig.remove(application.getFQN().getSimpleName());
         }
      }
   }

   public void postProcessEvents(MetaModel model) throws CompilationException
   {
      for (ApplicationMetaModel application : this)
      {
         for (MetaModelPlugin plugin : plugins.values())
         {
            plugin.postProcessEvents(application);
         }
      }
   }

   public void prePassivate(MetaModel model)
   {
      for (ApplicationMetaModel application : this)
      {
         for (MetaModelPlugin plugin : plugins.values())
         {
            plugin.prePassivate(application);
         }
      }

      //
      for (MetaModelPlugin plugin : plugins.values())
      {
         plugin.prePassivate(this);
      }

      //
      MetaModel.log.log("Emitting config");
      emitConfig(model);
   }

   // ****

   private ApplicationMetaModel processApplication(PackageElement packageElt, Map<String, Object> annotationValues) throws CompilationException
   {
      String name = (String)annotationValues.get("name");
      ElementHandle.Package handle = ElementHandle.Package.create(packageElt);
      ApplicationMetaModel application = get(handle);

      //
      if (application == null)
      {
         application = add(handle, name);
      }
      else
      {
         application.modified = true;
      }
      
      //
      return application;
   }

   public ApplicationMetaModel add(ElementHandle.Package handle, String applicationName)
   {
      ApplicationMetaModel application = new ApplicationMetaModel(handle, applicationName);

      // Let's find buffered annotations
      for (Iterator<Map.Entry<BufKey, Map<String, Object>>> i = toProcess.entrySet().iterator();i.hasNext();)
      {
         Map.Entry<BufKey, Map<String, Object>> entry = i.next();
         BufKey key = entry.getKey();
         if (handle.getQN().isPrefix(key.pkg))
         {
            Map<String, Object> annV = entry.getValue();
            i.remove();
            MetaModel.log.log("Moving " + key + " = " +  annV);
            application.toProcess.put(key, annV);
         }
      }

      // Add child
      addChild(Key.of(handle, ApplicationMetaModel.class), application);

      //
      return application;
   }

   private void resolveApplications()
   {
      for (ApplicationMetaModel application : getChildren(ApplicationMetaModel.class))
      {
         if (application.modified)
         {
            queue(MetaModelEvent.createUpdated(application));
            application.modified = false;
         }
      }
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
      JSON descriptor = new JSON();
      descriptor.merge(moduleConfig);

      // Module config
      Writer writer = null;
      try
      {
         FileObject fo = model.env.createResource(StandardLocation.CLASS_OUTPUT, "org.juzu", "config.json");
         writer = fo.openWriter();
         descriptor.toString(writer, 2);
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
         descriptor.clear();

         // Emit config
         for (Map.Entry<String, MetaModelPlugin> entry : plugins.entrySet())
         {
            JSON pluginDescriptor = entry.getValue().getDescriptor(application);
            if (pluginDescriptor != null)
            {
               descriptor.set(entry.getKey(), pluginDescriptor);
            }
         }

         //
         writer = null;
         try
         {
            FileObject fo = model.env.createResource(StandardLocation.CLASS_OUTPUT, application.getFQN().getPackageName(), "config.json");
            writer = fo.openWriter();
            descriptor.toString(writer, 2);
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

   @Override
   protected void postAttach(MetaModelObject parent)
   {
      model = (MetaModel)parent;
   }

   @Override
   protected void preDetach(MetaModelObject parent)
   {
      model = null;
   }
}
