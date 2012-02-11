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
import org.juzu.impl.request.Scope;
import org.juzu.impl.spi.inject.bindsingleton.Singleton;
import org.juzu.impl.spi.inject.bindsingleton.SingletonInjected;
import org.juzu.impl.spi.inject.constructorthrowschecked.ConstructorThrowsCheckedBean;
import org.juzu.impl.spi.inject.constructorthrowserror.ConstructorThrowsErrorBean;
import org.juzu.impl.spi.inject.constructorthrowsruntime.ConstructorThrowsRuntimeBean;
import org.juzu.impl.spi.inject.defaultscope.UndeclaredScopeBean;
import org.juzu.impl.spi.inject.implementationtype.Extended;
import org.juzu.impl.spi.inject.implementationtype.Extension;
import org.juzu.impl.spi.inject.lifecycle.Bean;
import org.juzu.impl.spi.inject.lifecycle.Dependency;
import org.juzu.impl.spi.inject.managerinjection.ManagerInjected;
import org.juzu.impl.spi.inject.named.NamedBean;
import org.juzu.impl.spi.inject.named.NamedInjected;
import org.juzu.impl.spi.inject.producer.Producer;
import org.juzu.impl.spi.inject.producer.Product;
import org.juzu.impl.spi.inject.qualifier.Qualified;
import org.juzu.impl.spi.inject.qualifier.QualifiedInjected;
import org.juzu.impl.spi.inject.requestscopedprovider.RequestBean;
import org.juzu.impl.spi.inject.requestscopedprovider.RequestBeanProvider;
import org.juzu.impl.spi.inject.scope.ScopedBean;
import org.juzu.impl.spi.inject.scope.ScopedInjected;
import org.juzu.impl.spi.inject.siblingproducers.Ext1Producer;
import org.juzu.impl.spi.inject.siblingproducers.Ext2Producer;
import org.juzu.impl.spi.inject.siblingproducers.ProductExt1;
import org.juzu.impl.spi.inject.siblingproducers.ProductExt2;
import org.juzu.impl.spi.inject.siblingproducers.ProductInjected;
import org.juzu.impl.spi.fs.disk.DiskFileSystem;
import org.juzu.test.AbstractTestCase;

import javax.naming.AuthenticationException;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ConcurrentModificationException;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public abstract class InjectManagerTestCase<B, I> extends AbstractTestCase
{

   /** . */
   protected InjectBootstrap bootstrap;

   /** . */
   protected InjectManager<B, I> mgr;

   /** . */
   private DiskFileSystem fs;

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
      InjectBootstrap bootstrap = getManager();

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

   protected abstract InjectBootstrap getManager() throws Exception;

   public void testLifeCycle() throws Exception
   {
      init("org", "juzu", "impl", "spi", "inject", "lifecycle");
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
      mgr.release(beanInstance);
   }

   public void testScope() throws Exception
   {
      init("org", "juzu", "impl", "spi", "inject", "scope");
      bootstrap.declareBean(ScopedInjected.class, null);
      bootstrap.declareBean(ScopedBean.class, null);
      boot(Scope.REQUEST);

      //
      ScopingContextImpl context = new ScopingContextImpl();
      ScopeController.begin(context);
      try
      {
         assertEquals(0, context.getEntries().size());
         ScopedInjected injected = getBean(ScopedInjected.class);
         assertNotNull(injected);
         assertNotNull(injected.scoped);
         String value = injected.scoped.getValue();
         assertEquals(1, context.getEntries().size());
         ScopedKey key = context.getEntries().keySet().iterator().next();
         assertEquals(Scope.REQUEST, key.getScope());
         ScopedBean scoped = (ScopedBean)context.getEntries().get(key);
         assertEquals(scoped.getValue(), value);
      }
      finally
      {
         ScopeController.end();
      }
   }

   public void testNamed() throws Exception
   {
      init("org", "juzu", "impl", "spi", "inject", "named");
      bootstrap.declareBean(NamedInjected.class, (Class<NamedInjected>)null);
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

   public void testSingleton() throws Exception
   {
      Singleton singleton = new Singleton();
      init("org", "juzu", "impl", "spi", "inject", "bindsingleton");
      bootstrap.declareBean(SingletonInjected.class, null);
      bootstrap.bindBean(Singleton.class, singleton);
      boot();

      //
      SingletonInjected injected = getBean(SingletonInjected.class);
      assertNotNull(injected);
      assertNotNull(injected.singleton);
      assertSame(singleton, injected.singleton);
   }

   public void testRequestScopedProvider() throws Exception
   {
      init("org", "juzu", "impl", "spi", "inject", "requestscopedprovider");
      bootstrap.declareProvider(RequestBean.class, RequestBeanProvider.class);
      boot(Scope.REQUEST);

      //
      ScopeController.begin(new ScopingContextImpl());
      try
      {
         RequestBean bean = getBean(RequestBean.class);
         assertNotNull(bean);
         assertNotNull(bean.provider);
      }
      finally
      {
         ScopeController.end();
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
}
