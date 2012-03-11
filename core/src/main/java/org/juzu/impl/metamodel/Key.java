package org.juzu.impl.metamodel;

import org.juzu.impl.utils.JSON;

import java.io.Serializable;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class Key<O extends MetaModelObject> implements Serializable
{
   
   public static <O extends MetaModelObject> Key<O> of(Object value, Class<O> type)
   {
      return new Wrapper<O>(value, type);
   }

   public static <O extends MetaModelObject> Key<O> of(Class<O> type)
   {
      return new Literal<O>(type);
   }
   
   public abstract JSON toJSON();

   protected abstract Class<O> getType();

   @Override
   public abstract boolean equals(Object obj);

   @Override
   public abstract int hashCode();

   private static final class Literal<O extends MetaModelObject> extends Key<O>
   {

      /** . */
      private final Class<O> type;
      
      private Literal(Class<O> type) throws NullPointerException
      {
         if (type == null)
         {
            throw new NullPointerException("No null type accepted");
         }

         //
         this.type = type;
      }

      @Override
      public JSON toJSON()
      {
         return new JSON().set("type", type.getName());
      }

      @Override
      protected Class<O> getType()
      {
         return type;
      }

      @Override
      public boolean equals(Object obj)
      {
         if (obj == this)
         {
            return true;
         }
         if (obj instanceof Literal<?>)
         {
            Literal<?> that = (Literal<?>)obj;
            return type.equals(that.type);
         }
         return false;
      }

      @Override
      public int hashCode()
      {
         return type.hashCode();
      }

      @Override
      public String toString()
      {
         return getClass().getSimpleName() + "[" + type.getName() + "]";
      }
   }
   
   private static final class Wrapper<O extends MetaModelObject> extends Key<O>
   {

      /** . */
      private final Object value;

      /** . */
      private final Class<O> type;

      private Wrapper(Object value, Class<O> type) throws NullPointerException
      {
         if (value == null)
         {
            throw new NullPointerException("No null value accepted");
         }
         if (type == null)
         {
            throw new NullPointerException("No null type accepted");
         }

         //
         this.value = value;
         this.type = type;
      }

      @Override
      protected Class<O> getType()
      {
         return type;  
      }

      @Override
      public JSON toJSON()
      {
         return new JSON().set("value", value).set("type", type.getName());
      }

      @Override
      public boolean equals(Object obj)
      {
         if (obj == this)
         {
            return true;
         }
         if (obj instanceof Wrapper<?>)
         {
            Wrapper<?> that = (Wrapper<?>)obj;
            return value.equals(that.value) && type.equals(that.type);
         }
         return false;  
      }

      @Override
      public int hashCode()
      {
         return value.hashCode() ^ type.hashCode();
      }

      @Override
      public String toString()
      {
         return getClass().getSimpleName() + "[value=" + value + ",type=" + type.getName() + "]";
      }
   }
}
