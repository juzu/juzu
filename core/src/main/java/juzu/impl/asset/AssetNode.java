package juzu.impl.asset;

import juzu.asset.AssetLocation;

import java.util.Collections;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class AssetNode
{

   /** . */
   private static final Set<String> EMPTY_SET = Collections.emptySet();

   /** . */
   final String id;

   /** . */
   final AssetLocation location;

   /** . */
   final String value;

   /** . */
   Set<String> dependsOnMe;

   /** . */
   Set<String> iDependOn;

   AssetNode(String id, AssetLocation location, String value, Set<String> iDependOn)
   {
      this.id = id;
      this.location = location;
      this.value = value;
      this.dependsOnMe = EMPTY_SET;
      this.iDependOn = iDependOn;
   }

   public String getId()
   {
      return id;
   }

   public AssetLocation getLocation()
   {
      return location;
   }

   public String getValue()
   {
      return value;
   }
}
