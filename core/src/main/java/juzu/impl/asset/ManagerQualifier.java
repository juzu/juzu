package juzu.impl.asset;

import juzu.asset.AssetType;

import javax.enterprise.util.AnnotationLiteral;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ManagerQualifier extends AnnotationLiteral<Manager> implements Manager
{

   /** . */
   private final AssetType value;

   public ManagerQualifier(AssetType value) throws NullPointerException
   {
      if (value == null)
      {
         throw new NullPointerException("No null value accepted");
      }
      this.value = value;
   }

   public AssetType value()
   {
      return value;
   }
}
