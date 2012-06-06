package juzu.impl.controller.metamodel;

import juzu.impl.compiler.ElementHandle;
import juzu.impl.metamodel.MetaModelObject;
import juzu.impl.utils.Cardinality;
import juzu.impl.utils.JSON;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ParameterMetaModel extends MetaModelObject
{

   /** . */
   final String name;

   /** . */
   final Cardinality cardinality;

   /** . */
   final ElementHandle.Class type;

   /** . */
   final String declaredType;

   public ParameterMetaModel(String name, Cardinality cardinality, ElementHandle.Class type, String declaredType)
   {
      this.name = name;
      this.cardinality = cardinality;
      this.type = type;
      this.declaredType = declaredType;
   }

   public String getName()
   {
      return name;
   }

   public Cardinality getCardinality()
   {
      return cardinality;
   }

   public ElementHandle.Class getType()
   {
      return type;
   }

   @Override
   public JSON toJSON()
   {
      return new JSON().
         set("name", name).
         set("type", type).
         set("declaredType", declaredType).
         set("cardinality", cardinality);
   }
}
