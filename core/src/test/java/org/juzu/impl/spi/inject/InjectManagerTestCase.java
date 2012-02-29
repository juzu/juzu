/*
 * Copyright (C) 2011 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.juzu.impl.spi.inject;

import org.juzu.impl.inject.ScopeController;
import org.juzu.impl.inject.Scoped;
import org.juzu.impl.request.Scope;
import org.juzu.impl.spi.inject.boundsingleton.injection.BoundSingleton;
import org.juzu.impl.spi.inject.boundsingleton.injection.BoundSingletonInjected;
import org.juzu.impl.spi.inject.boundsingleton.qualifier.declared.DeclaredQualifierBoundSingleton;
import org.juzu.impl.spi.inject.boundsingleton.qualifier.declared.DeclaredQualifierBoundSingletonInjected;
import org.juzu.impl.spi.inject.boundsingleton.qualifier.introspected.IntrospectedQualifierBoundSingleton;
import org.juzu.impl.spi.inject.boundsingleton.qualifier.introspected.IntrospectedQualifierBoundSingletonInjected;
import org.juzu.impl.spi.inject.boundsingleton.supertype.BoundApple;
import org.juzu.impl.spi.inject.boundsingleton.supertype.BoundFruit;
import org.juzu.impl.spi.inject.boundsingleton.supertype.BoundFruitInjected;
import org.juzu.impl.spi.inject.constructorthrowschecked.ConstructorThrowsCheckedBean;
import org.juzu.impl.spi.inject.constructorthrowserror.ConstructorThrowsErrorBean;
import org.juzu.impl.spi.inject.constructorthrowsruntime.ConstructorThrowsRuntimeBean;
import org.juzu.impl.spi.inject.defaultscope.UndeclaredScopeBean;
import org.juzu.impl.spi.inject.export.ExportedBean;
import org.juzu.impl.spi.inject.export.NonExportedBean;
import org.juzu.impl.spi.inject.implementationtype.Extended;
import org.juzu.impl.spi.inject.implementationtype.Extension;
import org.juzu.impl.spi.inject.dependencyinjection.Bean;
import org.juzu.impl.spi.inject.dependencyinjection.Dependency;
import org.juzu.impl.spi.inject.lifecycle.scoped.LifeCycleScopedBean;
import org.juzu.impl.spi.inject.lifecycle.singleton.LifeCycleSingletonBean;
import org.juzu.impl.spi.inject.lifecycle.unscoped.LifeCycleUnscopedBean;
import org.juzu.impl.spi.inject.managerinjection.ManagerInjected;
import org.juzu.impl.spi.inject.named.NamedBean;
import org.juzu.impl.spi.inject.named.NamedInjected;
import org.juzu.impl.spi.inject.producer.Producer;
import org.juzu.impl.spi.inject.producer.Product;
import org.juzu.impl.spi.inject.qualifier.Qualified;
import org.juzu.impl.spi.inject.qualifier.QualifiedInjected;
import org.juzu.impl.spi.inject.requestscopedprovider.RequestBean;
import org.juzu.impl.spi.inject.requestscopedprovider.RequestBeanProvider;
import org.juzu.impl.spi.inject.resolvebeans.ResolvableBean;
import org.juzu.impl.spi.inject.resolvebeans.ResolveBeanSubclass1;
import org.juzu.impl.spi.inject.resolvebeans.ResolveBeanSubclass2;
import org.juzu.impl.spi.inject.scope.scoped.ScopedBean;
import org.juzu.impl.spi.inject.scope.scoped.ScopedInjected;
import org.juzu.impl.spi.inject.scope.singleton.SingletonBean;
import org.juzu.impl.spi.inject.siblingproducers.Ext1Producer;
import org.juzu.impl.spi.inject.siblingproducers.Ext2Producer;
import org.juzu.impl.spi.inject.siblingproducers.ProductExt1;
import org.juzu.impl.spi.inject.siblingproducers.ProductExt2;
import org.juzu.impl.spi.inject.siblingproducers.ProductInjected;
import org.juzu.impl.spi.fs.disk.DiskFileSystem;
import org.juzu.impl.spi.inject.supertype.Apple;
import org.juzu.impl.spi.inject.supertype.FruitInjected;
import org.juzu.impl.utils.Tools;
import org.juzu.test.AbstractTestCase;

import javax.enterprise.util.AnnotationLiteral;
import javax.naming.AuthenticationException;
import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashSet;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public abstract class InjectManagerTestCase<B, I> extends AbstractTestCase
{

   /** . */
   protected InjectBuilder bootstrap;

   /** . */
   protected InjectManager<B, I> mgr;

   /** . */
   private DiskFileSystem fs;

   /** . */
   private ScopingContextImpl scopingContext;

   @Override
   protected void setUp() throws Exception
   {
   }

   protected final void init(String... pkg) throws Exception
   {
      File root = new File(Bean.class.getProtectionDomain().getCodeSource().getLocation().toURI());
      assertTrue(root.exists());
      assertTrue(root.isDirectory());
      DiskFileSystem fs = new DiskFileSystem(root, pkg);
      InjectBuilder bootstrap = getManager();

      //
      bootstrap.addFileSystem(fs);
      bootstrap.setClassLoader(Thread.currentThread().getContextClassLoader());

      //
      this.bootstrap = bootstrap;
      this.fs = fs;
   }

   protected final void boot(Scope... scopes) throws Exception
   {
      for (Scope scope : scopes)
      {
         bootstrap.addScope(scope);
      }
      mgr = bootstrap.create();
   }

   protected final <T> T getBean(Class<T> beanType) throws Exception
   {
      B bean = mgr.resolveBean(beanType);
      assertNotNull(bean);
      I beanInstance = mgr.create(bean);
      assertNotNull(beanInstance);
      return (T)mgr.get(bean, beanInstance);
   }

   protected final Object getBean(String beanName) throws Exception
   {
      B bean = mgr.resolveBean(beanName);
      assertNotNull("Could not find bean " + beanName, bean);
      I beanInstance = mgr.create(bean);
      assertNotNull(beanInstance);
      return mgr.get(bean, beanInstance);
   }

   protected final void beginScoping() throws Exception
   {
      if (scopingContext != null)
      {
         throw failure("Already scoping");
      }
      ScopeController.begin(scopingContext = new ScopingContextImpl());
   }
   
   protected final void endScoping() throws Exception
   {
      if (scopingContext == null)
      {
         throw failure("Not scoping");
      }
      ScopeController.end();
      for (Scoped scoped : scopingContext.getEntries().values())
      {
         scoped.destroy();
      }
      scopingContext = null;
   }

   protected abstract InjectBuilder getManager() throws Exception;

   public void testExport() throws Exception
   {
      init("org", "juzu", "impl", "spi", "inject", "export");
      bootstrap.declareBean(ExportedBean.class, null);
      boot();

      //
      B bean = mgr.resolveBean(NonExportedBean.class);
      assertNull(bean);
      bean = mgr.resolveBean(ExportedBean.class);
      assertNotNull(bean);
   }

   public void testDependencyInjection() throws Exception
   {
      init("org", "juzu", "impl", "spi", "inject", "dependencyinjection");
      bootstrap.declareBean(Bean.class, null);
      bootstrap.declareBean(Dependency.class, null);
      boot();

      //
      B bean = mgr.resolveBean(Bean.class);
      assertNotNull(bean);
      I beanInstance = mgr.create(bean);
      assertNotNull(beanInstance);
      Bean beanObject = getBean(Bean.class);
      assertNotNull(beanObject);
      beanObject.method();
      Dependency dependency = beanObject.getDependency();
      assertNotNull(dependency);
      mgr.release(bean, beanInstance);
   }

   public void testScopeScoped() throws Exception
   {
      init("org", "juzu", "impl", "spi", "inject", "scope", "scoped");
      bootstrap.declareBean(ScopedInjected.class, null);
      bootstrap.declareBean(ScopedBean.class, null);
      boot(Scope.REQUEST);

      //
      beginScoping();
      try
      {
         assertEquals(0, scopingContext.getEntries().size());
         ScopedInjected injected = getBean(ScopedInjected.class);
         assertNotNull(injected);
         assertNotNull(injected.scoped);
         String value = injected.scoped.getValue();
         assertEquals(1, scopingContext.getEntries().size());
         ScopedKey key = scopingContext.getEntries().keySet().iterator().next();
         assertEquals(Scope.REQUEST, key.getScope());
         ScopedBean scoped = (ScopedBean)scopingContext.getEntries().get(key).get();
         assertEquals(scoped.getValue(), value);
      }
      finally
      {
         endScoping();
      }
   }

   public void testScopeSingleton() throws Exception
   {
      init("org", "juzu", "impl", "spi", "inject", "scope", "singleton");
      bootstrap.declareBean(SingletonBean.class, null);
      boot();

      //
      SingletonBean singleton1 = getBean(SingletonBean.class);
      SingletonBean singleton2 = getBean(SingletonBean.class);
      assertSame(singleton1, singleton2);
   }

   public void testNamed() throws Exception
   {
      init("org", "juzu", "impl", "spi", "inject", "named");
      bootstrap.declareBean(NamedInjected.class, null);
      bootstrap.declareBean(NamedBean.class, NamedBean.Foo.class);
      bootstrap.declareBean(NamedBean.class, NamedBean.Bar.class);
      boot();

      //
      NamedInjected beanObject = getBean(NamedInjected.class);
      assertNotNull(beanObject);
      assertNotNull(beanObject.getFoo());
      assertEquals(NamedBean.Foo.class, beanObject.getFoo().getClass());
      assertNotNull(beanObject.getBar());
      assertEquals(NamedBean.Bar.class, beanObject.getBar().getClass());

      //
      Object foo = getBean("foo");
      assertNotNull(foo);

      //
      assertNull(mgr.resolveBean("juu"));
   }

   public void testQualifier() throws Exception
   {
      init("org", "juzu", "impl", "spi", "inject", "qualifier");
      bootstrap.declareBean(QualifiedInjected.class, (Class<QualifiedInjected>)null);
      bootstrap.declareBean(Qualified.class, Qualified.Red.class);
      bootstrap.declareBean(Qualified.class, Qualified.Green.class);
      boot();

      //
      QualifiedInjected beanObject = getBean(QualifiedInjected.class);
      assertNotNull(beanObject);
      assertNotNull(beanObject.getRed());
      assertEquals(Qualified.Red.class, beanObject.getRed().getClass());
      assertNotNull(beanObject.getGreen());
      assertEquals(Qualified.Green.class, beanObject.getGreen().getClass());
   }

   public void testSuperType() throws Exception
   {
      init("org", "juzu", "impl", "spi", "inject", "supertype");
      bootstrap.declareBean(Apple.class, null);
      bootstrap.declareBean(FruitInjected.class, null);
      boot();

      //
      FruitInjected beanObject = getBean(FruitInjected.class);
      assertNotNull(beanObject);
      assertNotNull(beanObject.fruit);
   }

   public void testProducer() throws Exception
   {
      init("org", "juzu", "impl", "spi", "inject", "producer");
      bootstrap.declareProvider(Product.class, Producer.class);
      boot();

      //
      Product product = getBean(Product.class);
      assertNotNull(product);
   }

   public void testSiblingProducers() throws Exception
   {
      init("org", "juzu", "impl", "spi", "inject", "siblingproducers");
      bootstrap.declareBean(ProductInjected.class, null);
      bootstrap.declareProvider(ProductExt1.class, Ext1Producer.class);
      bootstrap.declareProvider(ProductExt2.class, Ext2Producer.class);
      bootstrap.addFileSystem(fs);
      boot();

      //
      ProductExt1 productExt1 = getBean(ProductExt1.class);
      assertNotNull(productExt1);

      //
      ProductExt2 productExt2 = getBean(ProductExt2.class);
      assertNotNull(productExt2);

      //
      ProductInjected productInjected = getBean(ProductInjected.class);
      assertNotNull(productInjected);
      assertNotNull(productInjected.productExt1);
      assertNotNull(productInjected.productExt2);
   }

   public void testProvider() throws Exception
   {
      init("org", "juzu", "impl", "spi", "inject", "provider");
      bootstrap.bindProvider(Product.class, new Producer());
      boot();

      //
      Product product = getBean(Product.class);
      assertNotNull(product);
   }

   public void testInjectManager() throws Exception
   {
      init("org", "juzu", "impl", "spi", "inject", "managerinjection");
      bootstrap.declareBean(ManagerInjected.class, null);
      boot();

      //
      ManagerInjected managerInjected = getBean(ManagerInjected.class);
      assertNotNull(managerInjected);
      assertNotNull(managerInjected.manager);
      assertSame(mgr, managerInjected.manager);
   }

   public void testBoundSingletonInjection() throws Exception
   {
      BoundSingleton singleton = new BoundSingleton();
      init("org", "juzu", "impl", "spi", "inject", "boundsingleton", "injection");
      bootstrap.declareBean(BoundSingletonInjected.class, null);
      bootstrap.bindBean(BoundSingleton.class, null, singleton);
      boot();

      //
      BoundSingletonInjected injected = getBean(BoundSingletonInjected.class);
      assertNotNull(injected);
      assertNotNull(injected.singleton);
      assertSame(singleton, injected.singleton);
   }

   public void testBoundSingletonQualifierIntrospected() throws Exception
   {
      IntrospectedQualifierBoundSingleton singleton = new IntrospectedQualifierBoundSingleton();
      init("org", "juzu", "impl", "spi", "inject", "boundsingleton", "qualifier", "introspected");
      bootstrap.declareBean(IntrospectedQualifierBoundSingletonInjected.class, null);
      bootstrap.bindBean(IntrospectedQualifierBoundSingleton.class, null, singleton);
      boot();

      //
      IntrospectedQualifierBoundSingletonInjected injected = getBean(IntrospectedQualifierBoundSingletonInjected.class);
      assertNotNull(injected);
      assertNotNull(injected.singleton);
      assertSame(singleton, injected.singleton);
   }

   public void testBoundSingletonQualifierDeclared() throws Exception
   {
      class ColorizedLiteral extends AnnotationLiteral<Colorized> implements Colorized
      {
         public Color value()
         {
            return Color.BLUE;
         }
      }
      Annotation blue = new ColorizedLiteral();
      
      //
      DeclaredQualifierBoundSingleton singleton = new DeclaredQualifierBoundSingleton();
      init("org", "juzu", "impl", "spi", "inject", "boundsingleton", "qualifier", "declared");
      bootstrap.declareBean(DeclaredQualifierBoundSingletonInjected.class, null);
      bootstrap.bindBean(DeclaredQualifierBoundSingleton.class, Collections.singleton(blue), singleton);
      boot();

      //
      DeclaredQualifierBoundSingletonInjected injected = getBean(DeclaredQualifierBoundSingletonInjected.class);
      assertNotNull(injected);
      assertNotNull(injected.singleton);
      assertSame(singleton, injected.singleton);
   }

   public void testBoundSingletonSuperType() throws Exception
   {
      init("org", "juzu", "impl", "spi", "inject", "boundsingleton", "supertype");
      BoundApple apple = new BoundApple();
      bootstrap.bindBean(BoundFruit.class, null, apple);
      bootstrap.declareBean(BoundFruitInjected.class, null);
      boot();

      //
      BoundFruitInjected beanObject = getBean(BoundFruitInjected.class);
      assertNotNull(beanObject);
      assertNotNull(beanObject.fruit);
      assertSame(apple, beanObject.fruit);
   }

   public void testRequestScopedProvider() throws Exception
   {
      init("org", "juzu", "impl", "spi", "inject", "requestscopedprovider");
      bootstrap.declareProvider(RequestBean.class, RequestBeanProvider.class);
      boot(Scope.REQUEST);

      //
      beginScoping();
      try
      {
         RequestBean bean = getBean(RequestBean.class);
         assertNotNull(bean);
         assertNotNull(bean.provider);
      }
      finally
      {
         endScoping();
      }
   }

   public void testImplementationType() throws Exception
   {
      init("org", "juzu", "impl", "spi", "inject", "implementationtype");
      bootstrap.declareBean(Extended.class, Extension.class);
      boot();

      //
      Extended extended = getBean(Extended.class);
      assertEquals(Extension.class, extended.getClass());
   }

   public void testDefaultScope() throws Exception
   {
      init("org", "juzu", "impl", "spi", "inject", "defaultscope");
      bootstrap.declareBean(UndeclaredScopeBean.class, null);
      boot();

      //
      UndeclaredScopeBean bean1 = getBean(UndeclaredScopeBean.class);
      UndeclaredScopeBean bean2 = getBean(UndeclaredScopeBean.class);
      assertTrue(bean1.count != bean2.count);
   }

   public void testConstructorThrowsChecked() throws Exception
   {
      init("org", "juzu", "impl", "spi", "inject", "constructorthrowschecked");
      bootstrap.declareBean(ConstructorThrowsCheckedBean.class, null);
      boot();

      //
      try
      {
         getBean(ConstructorThrowsCheckedBean.class);
         throw failure("Was expecting exception");
      }
      catch (InvocationTargetException e)
      {
         assertInstanceOf(AuthenticationException.class, e.getCause());
      }
   }

   public void testConstructorThrowsRuntime() throws Exception
   {
      init("org", "juzu", "impl", "spi", "inject", "constructorthrowsruntime");
      bootstrap.declareBean(ConstructorThrowsRuntimeBean.class, null);
      boot();

      //
      try
      {
         getBean(ConstructorThrowsRuntimeBean.class);
         throw failure("Was expecting exception");
      }
      catch (InvocationTargetException e)
      {
         assertInstanceOf(ConcurrentModificationException.class, e.getCause());
      }
   }

   public void testConstructorThrowsError() throws Exception
   {
      init("org", "juzu", "impl", "spi", "inject", "constructorthrowserror");
      bootstrap.declareBean(ConstructorThrowsErrorBean.class, null);
      boot();

      //
      try
      {
         getBean(ConstructorThrowsErrorBean.class);
         throw failure("Was expecting exception");
      }
      catch (InvocationTargetException e)
      {
         assertInstanceOf(UnknownError.class, e.getCause());
      }
   }
   
   public void testResolvableBeans() throws Exception
   {
      init("org", "juzu", "impl", "spi", "inject", "resolvebeans");
      bootstrap.declareBean(ResolveBeanSubclass1.class, null);
      bootstrap.declareBean(ResolveBeanSubclass2.class, null);
      boot();
      
      //
      ArrayList<B> beans = Tools.list(mgr.resolveBeans(ResolvableBean.class));
      assertEquals(2, beans.size());
      HashSet<Class<?>> classes = new HashSet<Class<?>>(); 
      for (B bean : beans)
      {
         I instance = mgr.create(bean);
         Object o = mgr.get(bean, instance);
         classes.add(o.getClass());
      }
      assertEquals(Tools.<Class<?>>set(ResolveBeanSubclass1.class, ResolveBeanSubclass2.class), classes);
   }
   
   public void testLifeCycleUnscoped() throws Exception
   {
      init("org", "juzu", "impl", "spi", "inject", "lifecycle", "unscoped");
      bootstrap.declareBean(LifeCycleUnscopedBean.class, null);
      boot();
      
      //
      LifeCycleUnscopedBean.construct = 0;
      LifeCycleUnscopedBean.destroy = 0;

      //
      beginScoping();
      try
      {
         B bean = mgr.resolveBean(LifeCycleUnscopedBean.class);
         I instance = mgr.create(bean);
         LifeCycleUnscopedBean o = (LifeCycleUnscopedBean)mgr.get(bean, instance);
         assertEquals(1, LifeCycleUnscopedBean.construct);
         assertEquals(0, LifeCycleUnscopedBean.destroy);
         mgr.release(bean, instance);
         assertEquals(1, LifeCycleUnscopedBean.construct);
         assertEquals(1, LifeCycleUnscopedBean.destroy);
      }
      finally
      {
         endScoping();
      }
      assertEquals(1, LifeCycleUnscopedBean.construct);
      assertEquals(1, LifeCycleUnscopedBean.destroy);
   }

   public void testLifeCycleScoped() throws Exception
   {
      init("org", "juzu", "impl", "spi", "inject", "lifecycle", "scoped");
      bootstrap.declareBean(LifeCycleScopedBean.class, null);
      boot(Scope.SESSION);

      //
      LifeCycleScopedBean.construct = 0;
      LifeCycleScopedBean.destroy = 0;

      //
      beginScoping();
      try
      {
         B bean = mgr.resolveBean(LifeCycleScopedBean.class);
         I instance = mgr.create(bean);
         LifeCycleScopedBean o = (LifeCycleScopedBean)mgr.get(bean, instance);
         assertNotNull(o);
         o.m();
         assertEquals(1, LifeCycleScopedBean.construct);
         assertEquals(0, LifeCycleScopedBean.destroy);
         mgr.release(bean, instance);
         assertEquals(1, LifeCycleScopedBean.construct);
         assertEquals(0, LifeCycleScopedBean.destroy);
      }
      finally
      {
         endScoping();
      }
      assertEquals(1, LifeCycleScopedBean.construct);
      assertEquals(1, LifeCycleScopedBean.destroy);
   }

   public void testLifeCycleSingleton() throws Exception
   {
      init("org", "juzu", "impl", "spi", "inject", "lifecycle", "singleton");
      bootstrap.declareBean(LifeCycleSingletonBean.class, null);
      boot(Scope.SESSION);

      //
      LifeCycleSingletonBean.construct = 0;
      LifeCycleSingletonBean.destroy = 0;

      //
      beginScoping();
      try
      {
         B bean = mgr.resolveBean(LifeCycleSingletonBean.class);
         I instance = mgr.create(bean);
         LifeCycleSingletonBean o = (LifeCycleSingletonBean)mgr.get(bean, instance);
         assertNotNull(o);
         assertEquals(1, LifeCycleSingletonBean.construct);
         assertEquals(0, LifeCycleSingletonBean.destroy);
         mgr.release(bean, instance);
         assertEquals(1, LifeCycleSingletonBean.construct);
         assertEquals(0, LifeCycleSingletonBean.destroy);
      }
      finally
      {
         endScoping();
      }
      assertEquals(1, LifeCycleSingletonBean.construct);
      assertEquals(0, LifeCycleSingletonBean.destroy);

      //
      mgr.shutdown();
      assertEquals(1, LifeCycleSingletonBean.construct);
      assertEquals(1, LifeCycleSingletonBean.destroy);
   }
}
