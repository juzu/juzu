package org.juzu.impl.model.meta;

import org.juzu.impl.model.processor.ProcessingContext;
import org.juzu.test.AbstractTestCase;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class MetaModelTestCase extends AbstractTestCase
{

   /** . */
   private static final Key<MetaModelObject> A = Key.of("a", MetaModelObject.class);

   /** . */
   private static final Key<MetaModelObject> B = Key.of("b", MetaModelObject.class);

   /** . */
   private static final Key<MetaModelObject> C = Key.of("c", MetaModelObject.class);

   /** . */
   private static final Key<MetaModelObject> D = Key.of("d", MetaModelObject.class);

   public void testCannotRemoveRoot()
   {
      Simple a = new Simple("a");
      Simple b = new Simple("b");
      a.addChild(B, b);
      
      //
      a.remove();
      assertSame(b, a.getChild(B));
      assertEquals(0, a.removed);
      assertEquals(0, b.removed);
   }

   public void testTransitiveRemove()
   {
      Simple a = new Simple("a");
      Simple b = new Simple("b");
      Simple c = new Simple("c");
      a.addChild(B, b).addChild(C, c);

      //
      b.remove();
      assertEquals(0, a.removed);
      assertEquals(1, b.removed);
      assertEquals(1, c.removed);
   }

   public void testTransitiveRemoveChild()
   {
      Simple a = new Simple("a");
      Simple b = new Simple("b");
      Simple c = new Simple("c");
      a.addChild(B, b).addChild(C, c);

      //
      a.removeChild(B);
      assertEquals(0, a.removed);
      assertEquals(1, b.removed);
      assertEquals(1, c.removed);
   }
   
   public void testRemoveOrphan()
   {
      Simple a = new Simple("a");
      Simple b = new Simple("b");
      Simple c = new Simple("c");
      a.addChild(C, c);
      b.addChild(C, c);

      //
      a.removeChild(C);
      assertEquals(0, c.removed);
      b.removeChild(C);
      assertEquals(1, c.removed);
   }

   public void testTransitiveGarbage()
   {
      MetaModel m = new MetaModel();
      Simple a = new Simple("a");
      Simple b = new Simple("b");
      m.addChild(A, a).addChild(B, b);

      //
      a.exist = b.exist = false;
      m.postActivate((ProcessingContext)null);
      assertNull(a.getChild(B));
      assertNull(m.getChild(B));
      assertEquals(1, a.removed);
      assertEquals(1, b.removed);
   }

   public void testForcedGarbage()
   {
      MetaModel m = new MetaModel();
      Simple a = new Simple("a");
      Simple b = new Simple("b");
      m.addChild(A, a).addChild(B, b);

      //
      a.exist = false;
      m.postActivate((ProcessingContext)null);
      assertNull(a.getChild(B));
      assertNull(m.getChild(A));
      assertEquals(1, a.removed);
      assertEquals(1, b.removed);
   }

   public void testForcedGarbage2()
   {
      MetaModel m = new MetaModel();
      Simple a = new Simple("a");
      Simple b = new Simple("b");
      Simple c = new Simple("c");
      m.addChild(A, a).addChild(B, b);
      m.addChild(C, c).addChild(B, b);

      //
      a.exist = false;
      m.postActivate((ProcessingContext)null);
      assertNull(a.getChild(B));
      assertNull(m.getChild(A));
      assertSame(b, c.getChild(B));
      assertEquals(1, a.removed);
      assertEquals(0, b.removed);
      assertEquals(0, c.removed);
      c.exist = false;
      m.postActivate((ProcessingContext)null);
      assertEquals(1, a.removed);
      assertEquals(1, b.removed);
      assertEquals(1, c.removed);
   }

   public void testBug()
   {
      Simple a = new Simple("a");
      Simple b = new Simple("b");
      Simple c = new Simple("c");
      Simple d = new Simple("d");
      a.addChild(C, c);
      b.addChild(C, c);
      c.addChild(D, d);
      
      //
      assertSame(c, a.removeChild(C));
      assertNull(a.getChild(C));
      assertSame(c, b.getChild(C));
      assertSame(d, c.getChild(D));
   }
   
   public void testWeakReference()
   {
      Simple a = new Simple("a");
      Simple b = new Simple("b");
      Simple c = new Simple("c");
      a.addChild(C, c);
      b.addChild(C, c, false);
      
      //
      a.removeChild(C);
      assertNull(b.getChild(C));
   }
   
   public void testGarbageWeaklyReferenced()
   {
      MetaModel m = new MetaModel();
      Simple a = new Simple("a");
      Simple b = new Simple("b");
      Simple c = new Simple("c");
      m.addChild(A, a).addChild(C, c);
      m.addChild(B, b).addChild(C, c, false);

      //
      a.exist = false;
      m.postActivate((ProcessingContext)null);
      
      //
      assertEquals(1, a.removed);
      assertEquals(1, c.removed);

   }

   static class Simple extends MetaModelObject
   {

      /** . */
      final String name;
      
      /** . */
      boolean exist = true;

      /** . */
      int removed = 0;

      Simple(String name)
      {
         this.name = name;
      }

      @Override
      public boolean exist(MetaModel model)
      {
         return exist;
      }

      @Override
      protected void preRemove()
      {
         removed++;
      }

      @Override
      public String toString()
      {
         return "Simple[" + name + "]";
      }
   }
}
