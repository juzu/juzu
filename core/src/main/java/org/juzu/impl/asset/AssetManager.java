package org.juzu.impl.asset;

import org.juzu.asset.AssetLocation;
import org.juzu.asset.AssetType;
import org.juzu.impl.utils.Tools;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class AssetManager
{

   /** . */
   private final AssetType type;

   /** . */
   private final LinkedHashMap<String, Asset> assets = new LinkedHashMap<String, Asset>();

   /** . */
   private final HashSet<String> classPathAssets = new HashSet<String>();

   public AssetManager(AssetType type)
   {
      this.type = type;
   }

   /**
    * Attempt to add an asset to the manager.
    *
    * @param metaData the metaData to add
    * @throws NullPointerException if the metaData argument is nul
    * @throws IllegalArgumentException if the metaData does not have an id set
    */
   public void addAsset(AssetMetaData metaData) throws NullPointerException, IllegalArgumentException
   {
      String id = metaData.id;
      if (id != null)
      {
         if (!assets.keySet().contains(id))
         {
            Asset asset = new Asset(id, metaData.location, metaData.value, metaData.dependencies);
            for (Asset deployed : assets.values())
            {
               if (deployed.iDependOn.contains(id))
               {
                  asset.dependsOnMe = Tools.addToHashSet(asset.dependsOnMe, deployed.id);
               }
               if (asset.iDependOn.contains(deployed.id))
               {
                  deployed.dependsOnMe = Tools.addToHashSet(deployed.dependsOnMe, id);
               }
            }
            assets.put(id, asset);
         }
         else
         {
            // log it ?
            return;
         }
      }

      //
      switch (metaData.location)
      {
         case CLASSPATH:
            classPathAssets.add(metaData.getValue());
            break;
         default:
            // Nothing to do
            break;
      }
   }

   public boolean isClassPath(String path)
   {
      return classPathAssets.contains(path);
   }

   /**
    * Perform a topological sort of the provided asset script values.
    *
    * @param scripts the asset id to resolve
    * @return the resolved asset or null
    * @throws NullPointerException if the asset id argument is null
    * @throws IllegalArgumentException when script dependencies cannot be resolved
    */
   public Iterable<Asset> resolveAssets(Iterable<String> scripts) throws
      NullPointerException,
      IllegalArgumentException
   {
      LinkedHashMap<String, HashSet<String>> sub = new LinkedHashMap<String, HashSet<String>>();
      for (String script : scripts)
      {
         if (script.startsWith("http:") || script.startsWith("https:") | script.startsWith("/"))
         {
            // resolved.addLast(script);
         }
         else
         {
            Asset asset = assets.get(script);
            if (asset != null)
            {
               sub.put(asset.id, new HashSet<String>(asset.iDependOn));
            }
            else
            {
               throw new IllegalArgumentException("Cannot resolve asset " + script);
            }
         }
      }

      //
      LinkedList<Asset> resolved = new LinkedList<Asset>();
      while (sub.size() > 0)
      {
         boolean found = false;
         for (Iterator<Map.Entry<String, HashSet<String>>> i = sub.entrySet().iterator();i.hasNext();)
         {
            Map.Entry<String, HashSet<String>> entry = i.next();
            if (entry.getValue().isEmpty())
            {
               i.remove();
               Asset asset = assets.get(entry.getKey());
               resolved.addLast(asset);
               for (String dependency : asset.dependsOnMe)
               {
                  HashSet<String> foo = sub.get(dependency);
                  if (foo != null)
                  {
                     foo.remove(entry.getKey());
                  }
               }
               found = true;
               break;
            }
         }
         if (!found)
         {
            StringBuilder sb = new StringBuilder("Cannot satisfy asset dependencies:\n");
            for (Map.Entry<String, HashSet<String>> entry : sub.entrySet())
            {
               sb.append(entry.getKey()).append(" -> ").append(entry.getValue());
            }
            throw new IllegalArgumentException(sb.toString());
         }
      }

      //
      for (String script : scripts)
      {
         if (script.startsWith("http:") || script.startsWith("https:"))
         {
            resolved.addLast(new Asset(null, AssetLocation.EXTERNAL, script, Collections.<String>emptySet()));
         }
         else if (script.startsWith("/"))
         {
            resolved.addLast(new Asset(null, AssetLocation.SERVER, script, Collections.<String>emptySet()));
         }
      }

      //
      return resolved;
   }
}
