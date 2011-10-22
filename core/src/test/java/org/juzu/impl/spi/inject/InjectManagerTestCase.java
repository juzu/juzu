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
import org.juzu.impl.spi.inject.implementationtype.Extended;
import org.juzu.impl.spi.inject.implementationtype.Extension;
import org.juzu.impl.spi.inject.lifecycle.Bean;
import org.juzu.impl.spi.inject.lifecycle.Dependency;
import org.juzu.impl.spi.inject.managerinjection.ManagerInjected;
import org.juzu.impl.spi.inject.named.NamedBean;
import org.juzu.impl.spi.inject.named.NamedInjected;
import org.juzu.impl.spi.inject.producer.Producer;
import org.juzu.impl.spi.inject.producer.Product;
import org.juzu.impl.spi.inject.requestscopedprovider.RequestBean;
import org.juzu.impl.spi.inject.requestscopedprovider.RequestBeanProvider;
import org.juzu.impl.spi.inject.scope.ScopedInjected;
import org.juzu.impl.spi.inject.siblingproducers.Ext1Producer;
import org.juzu.impl.spi.inject.siblingproducers.Ext2Producer;
import org.juzu.impl.spi.inject.siblingproducers.ProductExt1;
import org.juzu.impl.spi.inject.siblingproducers.ProductExt2;
import org.juzu.impl.spi.inject.siblingproducers.ProductInjected;
import org.juzu.impl.spi.fs.disk.DiskFileSystem;
import org.juzu.test.AbstractTestCase;

import java.io.File;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
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
      assertNotNull(bean);
      I beanInstance = mgr.create(bean);
      assertNotNull(beanInstance);
      return mgr.get(bean, beanInstance);
   }

   protected abstract InjectBootstrap getManager() throws Exception;

   public void testLifeCycle() throws Exception
   {
      init("org", "juzu", "impl", "spi", "inject", "lifecycle");
      boot();

      //
      B bean = mgr.resolveBean(Bean.class);
      assertNotNull(bean);
      I beanInstance = mgr.create(bean);
      assertNotNull(beanInstance);
      Bean beanObject = getBean(Bean.class);
      assertNotNull(beanObject);
      beanObject.method();
      Dependency dependency = beanObject.dependency;
      assertNotNull(dependency);
      mgr.release(beanInstance);
   }

   public void testScope() throws Exception
   {
      init("org", "juzu", "impl", "spi", "inject", "scope");
      boot(Scope.REQUEST);

      //
      ScopeController.begin(new ScopingContextImpl());
      try
      {
         ScopedInjected injected = getBean(ScopedInjected.class);
         assertNotNull(injected);
         assertNotNull(injected.scoped);
      }
      finally
      {
         ScopeController.end();
      }
   }

   public void testNamed() throws Exception
   {
      init("org", "juzu", "impl", "spi", "inject", "named");
      bootstrap.declareBean(NamedBean.class, (Class<NamedBean>)null);
      boot(Scope.SESSION);

      //
      ScopeController.begin(new ScopingContextImpl());
      try
      {
         NamedInjected beanObject = getBean(NamedInjected.class);
         assertNotNull(beanObject);
         assertNotNull(beanObject.getFoo());

         //
         Object foo = getBean("foo");
         assertNotNull(foo);
      }
      finally
      {
         ScopeController.end();
      }
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

   public void testInjectManager() throws Exception
   {
      init("org", "juzu", "impl", "spi", "inject", "managerinjection");
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
      bootstrap.bindSingleton(Singleton.class, singleton);
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
}
