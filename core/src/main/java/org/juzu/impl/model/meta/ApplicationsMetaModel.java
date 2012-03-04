package org.juzu.impl.model.meta;

import org.juzu.impl.compiler.CompilationException;
import org.juzu.impl.compiler.ElementHandle;
import org.juzu.impl.utils.FQN;
import org.juzu.impl.utils.JSON;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.PackageElement;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ApplicationsMetaModel extends MetaModelObject implements Iterable<ApplicationMetaModel>
{

   /** . */
   public final static Key<ApplicationsMetaModel> KEY = Key.of(ApplicationsMetaModel.class);

   /** . */
   MetaModel model;

   @Override
   public JSON toJSON()
   {
      JSON json = new JSON();
      json.add("values", getChildren(ApplicationMetaModel.class));
      return json;
   }

   public Iterator<ApplicationMetaModel> iterator()
   {
      return getChildren(ApplicationMetaModel.class).iterator();
   }

   public ApplicationMetaModel get(ElementHandle.Package handle)
   {
      return getChild(Key.of(handle, ApplicationMetaModel.class));
   }

   public void processApplication(
      PackageElement packageElt,
      String annotationName,
      Map<String, Object> annotationValues) throws CompilationException
   {
      TypeMirror defaultControllerElt = (TypeMirror)annotationValues.get("defaultController");
      String defaultController = defaultControllerElt != null ? defaultControllerElt.toString() : null;
      Boolean escapeXML = (Boolean)annotationValues.get("escapeXML");
      String name = (String)annotationValues.get("name");
      if (name == null)
      {
         String s = packageElt.getSimpleName().toString();
         name = Character.toUpperCase(s.charAt(0)) + s.substring(1) + "Application";
      }
      List<AnnotationValue> pluginsAnnotation = (List<AnnotationValue>)annotationValues.get("plugins");
      List<FQN> plugins;
      if (pluginsAnnotation != null)
      {
         plugins = new ArrayList<FQN>(pluginsAnnotation.size());
         for (AnnotationValue pluginAnnotation : pluginsAnnotation)
         {
            TypeMirror plugin = (TypeMirror)pluginAnnotation.getValue();
            plugins.add(new FQN(plugin.toString()));
         }
      }
      else
      {
         plugins = Collections.emptyList();
      }
      ElementHandle.Package handle = ElementHandle.Package.create(packageElt);
      ApplicationMetaModel application = get(handle);
      if (application == null)
      {
         add(handle, name, defaultController, escapeXML, plugins);
      }
      else
      {
         application.modified = true;
      }
   }

   public ApplicationMetaModel add(
      ElementHandle.Package handle,
      String applicationName,
      String defaultController,
      Boolean escapeXML,
      List<FQN> plugins)
   {
      ApplicationMetaModel application = new ApplicationMetaModel(
         handle,
         applicationName,
         defaultController,
         escapeXML,
         plugins);
      addChild(Key.of(handle, ApplicationMetaModel.class), application);
      MetaModel.queue(MetaModelEvent.createAdded(application));
      return application;
   }

   public void postProcess(MetaModel model) throws CompilationException
   {
      resolveApplications();
   }

   private void resolveApplications()
   {
      for (ApplicationMetaModel application : getChildren(ApplicationMetaModel.class))
      {
         if (application.modified)
         {
            MetaModel.queue(MetaModelEvent.createUpdated(application));
            application.modified = false;
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
