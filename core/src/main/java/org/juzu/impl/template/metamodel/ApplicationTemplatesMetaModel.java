package org.juzu.impl.template.metamodel;

import org.juzu.impl.application.metamodel.ApplicationMetaModel;
import org.juzu.impl.model.meta.Key;
import org.juzu.impl.model.meta.MetaModel;
import org.juzu.impl.model.meta.MetaModelEvent;
import org.juzu.impl.model.meta.MetaModelObject;
import org.juzu.impl.utils.JSON;
import org.juzu.impl.utils.QN;

import java.util.Iterator;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ApplicationTemplatesMetaModel extends MetaModelObject implements Iterable<TemplateMetaModel>
{

   /** . */
   public final static Key<ApplicationTemplatesMetaModel> KEY = Key.of(ApplicationTemplatesMetaModel.class);

   /** . */
   ApplicationMetaModel application;

   /** . */
   final QN qn;

   public ApplicationTemplatesMetaModel(QN qn)
   {
      this.qn = qn;
   }

   @Override
   public JSON toJSON()
   {
      JSON json = new JSON();
      json.add("values", getChildren(TemplateMetaModel.class));
      json.add("qn", qn);
      return json;
   }

   public ApplicationMetaModel getApplication()
   {
      return application;
   }

   public QN getQN()
   {
      return qn;
   }

   public TemplateMetaModel get(String path)
   {
      return getChild(Key.of(path, TemplateMetaModel.class));
   }

   public Iterator<TemplateMetaModel> iterator()
   {
      return getChildren(TemplateMetaModel.class).iterator();
   }

   public TemplateMetaModel add(TemplateRefMetaModel ref)
   {
      TemplateMetaModel template = addChild(Key.of(ref.path, TemplateMetaModel.class), new TemplateMetaModel(ref.path));
      MetaModel.queue(MetaModelEvent.createAdded(template));
      return template;
   }

   public void remove(TemplateMetaModel template)
   {
      if (template.templates != this)
      {
         throw new IllegalArgumentException();
      }
      removeChild(Key.of(template.path, TemplateMetaModel.class));
   }

   @Override
   protected void postAttach(MetaModelObject parent)
   {
      this.application = (ApplicationMetaModel)parent;
   }
}
