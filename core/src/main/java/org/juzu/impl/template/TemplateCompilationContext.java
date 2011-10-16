package org.juzu.impl.template;

import org.juzu.impl.utils.MethodInvocation;
import org.juzu.impl.tags.DecorateTag;
import org.juzu.impl.tags.IncludeTag;
import org.juzu.impl.tags.InsertTag;
import org.juzu.template.TagHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class TemplateCompilationContext
{

   /** . */
   private final Map<String, TagHandler> tags = new HashMap<String, TagHandler>();

   public TemplateCompilationContext()
   {
      // Built in tags

      tags.put("include", new IncludeTag());
      tags.put("insert", new InsertTag());
      tags.put("decorate", new DecorateTag());
   }

   public TagHandler resolve(String name)
   {
      return tags.get(name);
   }

   public String resolveTemplate(String path) throws IOException
   {
      return null;
   }

   public MethodInvocation resolveMethodInvocation(String typeName, String methodName, Map<String, String> parameterMap)
   {
      return null;
   }

}
