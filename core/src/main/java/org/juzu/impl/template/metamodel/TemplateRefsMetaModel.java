package org.juzu.impl.template.metamodel;

import org.juzu.impl.application.metamodel.ApplicationMetaModel;
import org.juzu.impl.application.metamodel.ApplicationsMetaModel;
import org.juzu.impl.compiler.CompilationException;
import org.juzu.impl.compiler.ElementHandle;
import org.juzu.impl.metamodel.Key;
import org.juzu.impl.metamodel.MetaModel;
import org.juzu.impl.metamodel.MetaModelObject;
import org.juzu.impl.utils.JSON;
import org.juzu.impl.utils.QN;

import javax.lang.model.element.PackageElement;
import javax.lang.model.element.VariableElement;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class TemplateRefsMetaModel extends MetaModelObject
{

   /** . */
   public final static Key<TemplateRefsMetaModel> KEY = Key.of(TemplateRefsMetaModel.class);

   /** . */
   MetaModel model;

   @Override
   public JSON toJSON()
   {
      JSON json = new JSON();
      json.add("values", getChildren(TemplateRefMetaModel.class));
      return json;
   }

   public TemplateRefMetaModel add(ElementHandle.Field handle, String path)
   {
      return addChild(Key.of(handle, TemplateRefMetaModel.class), new TemplateRefMetaModel(handle, path));
   }

   public void processDeclarationTemplate(
      VariableElement variableElt,
      String annotationFQN,
      Map<String, Object> annotationValues) throws CompilationException
   {
      String path = (String)annotationValues.get("value");
      ElementHandle.Field handle = ElementHandle.Field.create(variableElt);
      TemplateRefMetaModel ref = getChild(Key.of(handle, TemplateRefMetaModel.class));
      if (ref == null)
      {
         add(handle, path);
      }
      else if (ref.path.equals(path))
      {
         // OK
      }
      else
      {
         // We do have a template
         if (ref.getTemplate() != null)
         {
            // Remove the ref, the template will be garbaged in a later phase
            ref.setTemplate(null);
         }

         // Update the ref
         ref.path = path;
      }
   }

   public void postProcess(MetaModel model)
   {
      resolveTemplateRefs();
   }

   /**
    * Takes care of templates ref having a null template and try to assign each of them.
    */
   private void resolveTemplateRefs()
   {
      for (TemplateRefMetaModel ref : getChildren(TemplateRefMetaModel.class))
      {
         if (ref.getTemplate() == null)
         {
            VariableElement variableElt = model.env.get(ref.handle);
            PackageElement packageElt = model.env.getPackageOf(variableElt);
            QN packageQN = new QN(packageElt.getQualifiedName());
            for (ApplicationMetaModel application : model.getChild(ApplicationsMetaModel.KEY))
            {
               if (application.getFQN().getPackageName().isPrefix(packageQN))
               {
                  TemplateMetaModel template = application.getTemplates().get(ref.path);
                  if (template == null)
                  {
                     template = application.getTemplates().add(ref);
                  }
                  ref.setTemplate(template);
               }
            }
         }
      }
   }

   @Override
   protected void postAttach(MetaModelObject parent)
   {
      model = (MetaModel)parent;
   }
}
