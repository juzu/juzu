package org.juzu.impl.template.metadata;

import org.juzu.impl.metadata.BeanDescriptor;
import org.juzu.impl.metadata.Descriptor;
import org.juzu.impl.utils.JSON;
import org.juzu.template.Template;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class TemplatesDescriptor extends Descriptor
{

   /** . */
   private final List<TemplateDescriptor> templates;

   /** . */
   private final String packageName;

   /** . */
   private final ArrayList<BeanDescriptor> beans; 

   public TemplatesDescriptor(ClassLoader loader, JSON config) throws Exception
   {
      ArrayList<BeanDescriptor> beans = new ArrayList<BeanDescriptor>();
      List<TemplateDescriptor> templates = new ArrayList<TemplateDescriptor>();

      // Load templates
      for (String fqn : config.getList("templates", String.class))
      {
         Class<?> clazz = loader.loadClass(fqn);
         Field f = clazz.getField("DESCRIPTOR");
         TemplateDescriptor descriptor = (TemplateDescriptor)f.get(null);
         templates.add(descriptor);
         beans.add(new BeanDescriptor(Template.class, null, null, descriptor.getType()));
      }
      
      //
      String packageName = config.getString("package");

      //
      this.templates = templates;
      this.packageName = packageName;
      this.beans = beans;
   }
   
   public Iterable<BeanDescriptor> getBeans()
   {
      return beans;
   }

   public List<TemplateDescriptor> getTemplates()
   {
      return templates;
   }

   public TemplateDescriptor getTemplate(String path) throws NullPointerException
   {
      if (path == null)
      {
         throw new NullPointerException("No null path accepted");
      }
      for (TemplateDescriptor template : templates)
      {
         if (template.getPath().equals(path))
         {
            return template;
         }
      }
      return null;
   }

   public String getPackageName()
   {
      return packageName;
   }
}
