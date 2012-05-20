package org.juzu.impl.plugin.asset;

import org.juzu.impl.application.metamodel.ApplicationMetaModel;
import org.juzu.impl.compiler.ElementHandle;
import org.juzu.impl.metamodel.MetaModelPlugin;
import org.juzu.impl.utils.JSON;
import org.juzu.plugin.asset.Assets;

import javax.lang.model.element.Element;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class AssetMetaModelPlugin extends MetaModelPlugin
{

   /** . */
   private final HashMap<ElementHandle.Package, JSON> enabledMap = new HashMap<ElementHandle.Package, JSON>();
   
   @Override
   public void processAnnotation(ApplicationMetaModel application, Element element, String fqn, Map<String, Object> values)
   {
      if (fqn.equals(Assets.class.getName()))
      {
         ElementHandle.Package handle = application.getHandle();
         JSON json = new JSON();
         json.set("scripts", build((List<Map<String,Object>>)values.get("scripts")));
         json.set("stylesheets", build((List<Map<String,Object>>)values.get("stylesheets")));
         json.set("package", application.getFQN().getPackageName().append("assets"));
         enabledMap.put(handle, json);
      }
   }

   private List<JSON> build(List<Map<String, Object>> scripts)
   {
      List<JSON> foo = Collections.emptyList();
      if (scripts != null && scripts.size() > 0)
      {
         foo = new ArrayList<JSON>(scripts.size());
         for (Map<String, Object> script : scripts)
         {
            JSON bar = new JSON();
            for (Map.Entry<String, Object> entry : script.entrySet())
            {
               bar.set(entry.getKey(), entry.getValue());
            }
            foo.add(bar);
         }
      }
      return foo;
   }

   @Override
   public void postConstruct(ApplicationMetaModel application)
   {
   }

   @Override
   public void preDestroy(ApplicationMetaModel application)
   {
      enabledMap.remove(application.getHandle());
   }

   @Override
   public JSON getDescriptor(ApplicationMetaModel application)
   {
      ElementHandle.Package handle = application.getHandle();
      return enabledMap.get(handle);
   }
}
