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

import org.junit.Test;
import org.juzu.impl.inject.BeanFilter;
import org.juzu.impl.inject.Export;
import org.juzu.impl.inject.ScopeController;
import org.juzu.impl.inject.Scoped;
import org.juzu.impl.request.Scope;
import org.juzu.impl.spi.fs.ReadFileSystem;
import org.juzu.impl.spi.fs.ram.RAMDir;
import org.juzu.impl.spi.fs.ram.RAMFileSystem;
import org.juzu.impl.spi.fs.ram.RAMPath;
import org.juzu.impl.spi.inject.bound.bean.injection.BoundBean;
import org.juzu.impl.spi.inject.bound.bean.injection.BoundBeanInjected;
import org.juzu.impl.spi.inject.bound.bean.qualifier.declared.DeclaredQualifierBoundBean;
import org.juzu.impl.spi.inject.bound.bean.qualifier.declared.DeclaredQualifierBoundBeanInjected;
import org.juzu.impl.spi.inject.bound.bean.qualifier.introspected.IntrospectedQualifierBoundBean;
import org.juzu.impl.spi.inject.bound.bean.qualifier.introspected.IntrospectedQualifierBoundBeanInjected;
import org.juzu.impl.spi.inject.bound.bean.supertype.BoundBeanApple;
import org.juzu.impl.spi.inject.bound.bean.supertype.BoundBeanFruit;
import org.juzu.impl.spi.inject.bound.bean.supertype.BoundBeanFruitInjected;
import org.juzu.impl.spi.inject.bound.provider.qualifier.declared.DeclaredQualifierBoundProvider;
import org.juzu.impl.spi.inject.bound.provider.qualifier.declared.DeclaredQualifierBoundProviderInjected;
import org.juzu.impl.spi.inject.bound.provider.qualifier.declared.GenericProvider;
import org.juzu.impl.spi.inject.configuration.Declared;
import org.juzu.impl.spi.inject.configuration.DeclaredInjected;
import org.juzu.impl.spi.inject.constructorthrowschecked.ConstructorThrowsCheckedBean;
import org.juzu.impl.spi.inject.constructorthrowserror.ConstructorThrowsErrorBean;
import org.juzu.impl.spi.inject.constructorthrowsruntime.ConstructorThrowsRuntimeBean;
import org.juzu.impl.spi.inject.declared.bean.qualifier.declared.DeclaredQualifierDeclaredBean;
import org.juzu.impl.spi.inject.declared.bean.qualifier.declared.DeclaredQualifierDeclaredBeanInjected;
import org.juzu.impl.spi.inject.declared.producer.injection.DeclaredProducer;
import org.juzu.impl.spi.inject.declared.producer.injection.DeclaredProducerProduct;
import org.juzu.impl.spi.inject.declared.producer.injection.DeclaredProducerProductInjected;
import org.juzu.impl.spi.inject.declared.provider.injected.DeclaredProviderInjected;
import org.juzu.impl.spi.inject.declared.provider.injected.DeclaredProviderInjectedDependency;
import org.juzu.impl.spi.inject.declared.provider.injected.DeclaredProviderInjectedProduct;
import org.juzu.impl.spi.inject.declared.provider.injected.DeclaredProviderInjectedProductInjected;
import org.juzu.impl.spi.inject.declared.provider.injection.DeclaredProvider;
import org.juzu.impl.spi.inject.declared.provider.injection.DeclaredProviderProduct;
import org.juzu.impl.spi.inject.declared.provider.injection.DeclaredProviderProductInjected;
import org.juzu.impl.spi.inject.declared.provider.qualifier.declared.ColorlessProvider;
import org.juzu.impl.spi.inject.declared.provider.qualifier.declared.DeclaredQualifierDeclaredProvider;
import org.juzu.impl.spi.inject.declared.provider.qualifier.declared.DeclaredQualifierDeclaredProviderInjected;
import org.juzu.impl.spi.inject.declared.provider.qualifier.declared.GreenProvider;
import org.juzu.impl.spi.inject.scope.defaultscope.UndeclaredScopeBean;
import org.juzu.impl.spi.inject.implementationtype.Extended;
import org.juzu.impl.spi.inject.implementationtype.Extension;
import org.juzu.impl.spi.inject.lifecycle.scoped.LifeCycleScopedBean;
import org.juzu.impl.spi.inject.lifecycle.singleton.LifeCycleSingletonBean;
import org.juzu.impl.spi.inject.lifecycle.unscoped.LifeCycleUnscopedBean;
import org.juzu.impl.spi.inject.managerinjection.ManagerInjected;
import org.juzu.impl.spi.inject.named.NamedBean;
import org.juzu.impl.spi.inject.named.NamedInjected;
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
import org.juzu.impl.spi.inject.spring.SpringBuilder;
import org.juzu.impl.spi.inject.supertype.Apple;
import org.juzu.impl.spi.inject.supertype.FruitInjected;
import org.juzu.impl.utils.Tools;
import org.juzu.test.AbstractInjectTestCase;
import org.juzu.test.CompilerHelper;

import javax.enterprise.inject.spi.Bean;
import javax.naming.AuthenticationException;
import java.io.File;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashSet;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class InjectManagerTestCase<B, I> extends AbstractInjectTestCase
{

   /** . */
   protected InjectBuilder bootstrap;

   /** . */
   protected InjectManager<B, I> mgr;

   /** . */
   private ReadFileSystem<?> fs;

   /** . */
   private ScopingContextImpl scopingContext;

   public InjectManagerTestCase(InjectImplementation di)
   {
      super(di);
   }

   protected final void init(String... pkg) throws Exception
   {
      File root = new File(InjectManagerTestCase.class.getProtectionDomain().getCodeSource().getLocation().toURI());
      assertTrue(root.exists());
      assertTrue(root.isDirectory());
      init(new DiskFileSystem(root, pkg), Thread.currentThread().getContextClassLoader());
   }

   protected final void init(ReadFileSystem<?> fs, ClassLoader classLoader) throws Exception
   {
      InjectBuilder bootstrap = getManager();
      bootstrap.addFileSystem(fs);
      bootstrap.setClassLoader(classLoader);
      bootstrap.setFilter(BeanFilter.DEFAULT);

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
      assertNotNull("Could not resolve bean of type " + beanType, bean);
      I beanInstance = mgr.create(bean);
      assertNotNull("Could not create bean instance of type " + beanType + " from bean " + bean, beanInstance);
      Object o = mgr.get(bean, beanInstance);
      assertNotNull("Could not obtain bean object from bean instance " + beanInstance + " of type " + beanType, o);
      return beanType.cast(o);
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

   protected InjectBuilder getManager() throws Exception
   {
      return getDI().bootstrap();
   }

   @Test
   public void testScopeScoped() throws Exception
   {
      init("org", "juzu", "impl", "spi", "inject", "scope", "scoped");
      bootstrap.declareBean(ScopedInjected.class, null, null);
      bootstrap.declareBean(ScopedBean.class, null, null);
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

   @Test
   public void testScopeSingleton() throws Exception
   {
      init("org", "juzu", "impl", "spi", "inject", "scope", "singleton");
      bootstrap.declareBean(SingletonBean.class, null, null);
      boot();

      //
      SingletonBean singleton1 = getBean(SingletonBean.class);
      SingletonBean singleton2 = getBean(SingletonBean.class);
      assertSame(singleton1, singleton2);
   }

   @Test
   public void testNamed() throws Exception
   {
      init("org", "juzu", "impl", "spi", "inject", "named");
      bootstrap.declareBean(NamedInjected.class, null, null);
      bootstrap.declareBean(NamedBean.class, null, NamedBean.Foo.class);
      bootstrap.declareBean(NamedBean.class, null, NamedBean.Bar.class);
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

   @Test
   public void testQualifier() throws Exception
   {
      init("org", "juzu", "impl", "spi", "inject", "qualifier");
      bootstrap.declareBean(QualifiedInjected.class, null, (Class<QualifiedInjected>)null);
      bootstrap.declareBean(Qualified.class, null, Qualified.Red.class);
      bootstrap.declareBean(Qualified.class, null, Qualified.Green.class);
      boot();

      //
      QualifiedInjected beanObject = getBean(QualifiedInjected.class);
      assertNotNull(beanObject);
      assertNotNull(beanObject.getRed());
      assertEquals(Qualified.Red.class, beanObject.getRed().getClass());
      assertNotNull(beanObject.getGreen());
      assertEquals(Qualified.Green.class, beanObject.getGreen().getClass());
   }

   @Test
   public void testSuperType() throws Exception
   {
      init("org", "juzu", "impl", "spi", "inject", "supertype");
      bootstrap.declareBean(Apple.class, null, null);
      bootstrap.declareBean(FruitInjected.class, null, null);
      boot();

      //
      FruitInjected beanObject = getBean(FruitInjected.class);
      assertNotNull(beanObject);
      assertNotNull(beanObject.fruit);
   }

   @Test
   public void testBoundBeanQualifierDeclared() throws Exception
   {
      DeclaredQualifierBoundBean blue = new DeclaredQualifierBoundBean();
      DeclaredQualifierBoundBean red = new DeclaredQualifierBoundBean();
      init("org", "juzu", "impl", "spi", "inject", "bound", "bean", "qualifier", "declared");
      bootstrap.declareBean(DeclaredQualifierBoundBeanInjected.class, null, null);
      bootstrap.bindBean(DeclaredQualifierBoundBean.class, Collections.<Annotation>singleton(new ColorizedLiteral(Color.BLUE)), blue);
      bootstrap.bindBean(DeclaredQualifierBoundBean.class, Collections.<Annotation>singleton(new ColorizedLiteral(Color.RED)), red);
      boot();

      //
      DeclaredQualifierBoundBeanInjected injected = getBean(DeclaredQualifierBoundBeanInjected.class);
      assertNotNull(injected);
      assertNotNull(injected.blue);
      assertNotNull(injected.red);
      assertSame(blue, injected.blue);
      assertSame(red, injected.red);
   }

   @Test
   public void testDeclaredProviderInjection() throws Exception
   {
      init("org", "juzu", "impl", "spi", "inject", "declared", "provider", "injection");
      bootstrap.declareProvider(DeclaredProviderProduct.class, null, DeclaredProvider.class);
      bootstrap.declareBean(DeclaredProviderProductInjected.class, null, null);
      boot();

      //
      DeclaredProviderProductInjected injected = getBean(DeclaredProviderProductInjected.class);
      assertNotNull(injected);
      assertNotNull(injected.dependency);
   }

   @Test
   public void testDeclaredProducerInjection() throws Exception
   {
      init("org", "juzu", "impl", "spi", "inject", "declared", "producer", "injection");
      bootstrap.declareProvider(DeclaredProducerProduct.class, null, DeclaredProducer.class);
      bootstrap.declareBean(DeclaredProducerProductInjected.class, null, null);
      boot();

      //
      DeclaredProducerProductInjected injected = getBean(DeclaredProducerProductInjected.class);
      assertNotNull(injected);
      assertNotNull(injected.dependency);
   }

   @Test
   public void testSiblingProducers() throws Exception
   {
      init("org", "juzu", "impl", "spi", "inject", "siblingproducers");
      bootstrap.declareBean(ProductInjected.class, null, null);
      bootstrap.declareProvider(ProductExt1.class, null, Ext1Producer.class);
      bootstrap.declareProvider(ProductExt2.class, null, Ext2Producer.class);
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

   @Test
   public void testProvider() throws Exception
   {
      init("org", "juzu", "impl", "spi", "inject", "provider");
      bootstrap.bindProvider(DeclaredProviderProduct.class, null, new DeclaredProvider());
      boot();

      //
      DeclaredProviderProduct product = getBean(DeclaredProviderProduct.class);
      assertNotNull(product);
   }

   @Test
   public void testProviderInjected() throws Exception
   {
      init("org", "juzu", "impl", "spi", "inject", "declared", "provider", "injected");
      DeclaredProviderInjectedDependency dependency = new DeclaredProviderInjectedDependency();
      bootstrap.bindBean(DeclaredProviderInjectedDependency.class, null, dependency);
      bootstrap.declareProvider(DeclaredProviderInjectedProduct.class, null, DeclaredProviderInjected.class);
      bootstrap.declareBean(DeclaredProviderInjectedProductInjected.class, null, null);
      boot();

      //
      DeclaredProviderInjectedProductInjected injected = getBean(DeclaredProviderInjectedProductInjected.class);
      assertNotNull(injected);
      assertNotNull(injected.product);
      assertSame(dependency, injected.product.dependency);
   }

   @Test
   public void testInjectManager() throws Exception
   {
      init("org", "juzu", "impl", "spi", "inject", "managerinjection");
      bootstrap.declareBean(ManagerInjected.class, null, null);
      boot();

      //
      ManagerInjected managerInjected = getBean(ManagerInjected.class);
      assertNotNull(managerInjected);
      assertNotNull(managerInjected.manager);
      assertSame(mgr, managerInjected.manager);
   }

   @Test
   public void testBoundBeanInjection() throws Exception
   {
      BoundBean singleton = new BoundBean();
      init("org", "juzu", "impl", "spi", "inject", "bound", "bean", "injection");
      bootstrap.declareBean(BoundBeanInjected.class, null, null);
      bootstrap.bindBean(BoundBean.class, null, singleton);
      boot();

      //
      BoundBeanInjected injected = getBean(BoundBeanInjected.class);
      assertNotNull(injected);
      assertNotNull(injected.dependency);
      assertSame(singleton, injected.dependency);
   }

   @Test
   public void testBoundBeanQualifierIntrospected() throws Exception
   {
      IntrospectedQualifierBoundBean singleton = new IntrospectedQualifierBoundBean();
      init("org", "juzu", "impl", "spi", "inject", "bound", "bean", "qualifier", "introspected");
      bootstrap.declareBean(IntrospectedQualifierBoundBeanInjected.class, null, null);
      bootstrap.bindBean(IntrospectedQualifierBoundBean.class, null, singleton);
      boot();

      //
      IntrospectedQualifierBoundBeanInjected injected = getBean(IntrospectedQualifierBoundBeanInjected.class);
      assertNotNull(injected);
      assertNotNull(injected.singleton);
      assertSame(singleton, injected.singleton);
   }

   @Test
   public void testBoundProviderQualifierDeclared() throws Exception
   {
      init("org", "juzu", "impl", "spi", "inject", "bound", "provider", "qualifier", "declared");
      bootstrap.declareBean(DeclaredQualifierBoundProviderInjected.class, null, null);
      DeclaredQualifierBoundProvider blue = new DeclaredQualifierBoundProvider();
      DeclaredQualifierBoundProvider red = new DeclaredQualifierBoundProvider();
      DeclaredQualifierBoundProvider green = new DeclaredQualifierBoundProvider.Green();
      bootstrap.bindProvider(DeclaredQualifierBoundProvider.class, Collections.<Annotation>singleton(new ColorizedLiteral(Color.BLUE)), new GenericProvider(blue));
      bootstrap.bindProvider(DeclaredQualifierBoundProvider.class, Collections.<Annotation>singleton(new ColorizedLiteral(Color.RED)), new GenericProvider(red));
      bootstrap.bindProvider(DeclaredQualifierBoundProvider.class, Collections.<Annotation>singleton(new ColorizedLiteral(Color.GREEN)), new GenericProvider(green));
      boot();

      //
      DeclaredQualifierBoundProviderInjected injected = getBean(DeclaredQualifierBoundProviderInjected.class);
      assertNotNull(injected);
      assertSame(blue, injected.blue);
      assertSame(red, injected.red);
      assertSame(green, injected.green);
   }

   @Test
   public void testDeclaredQualifierDeclaredBean() throws Exception
   {
      init("org", "juzu", "impl", "spi", "inject", "declared", "qualifier", "declared", "bean");
      bootstrap.declareBean(DeclaredQualifierDeclaredBeanInjected.class, null, null);
      bootstrap.declareBean(DeclaredQualifierDeclaredBean.class, Collections.<Annotation>singleton(new ColorizedLiteral(Color.BLUE)), null);
      bootstrap.declareBean(DeclaredQualifierDeclaredBean.class, Collections.<Annotation>singleton(new ColorizedLiteral(Color.RED)), null);
      bootstrap.declareBean(DeclaredQualifierDeclaredBean.class, Collections.<Annotation>singleton(new ColorizedLiteral(Color.GREEN)), DeclaredQualifierDeclaredBean.Green.class);
      boot();

      //
      DeclaredQualifierDeclaredBeanInjected injected = getBean(DeclaredQualifierDeclaredBeanInjected.class);
      assertNotNull(injected);
      assertNotNull(injected.blue);
      assertNotNull(injected.red);
      assertNotNull(injected.green);
      assertNotSame(injected.blue.getId(), injected.red.getId());
      assertNotSame(injected.green.getId(), injected.red.getId());
      assertNotSame(injected.blue.getId(), injected.green.getId());
      assertInstanceOf(DeclaredQualifierDeclaredBean.class, injected.blue);
      assertInstanceOf(DeclaredQualifierDeclaredBean.class, injected.red);
      assertInstanceOf(DeclaredQualifierDeclaredBean.Green.class, injected.green);
      assertNotInstanceOf(DeclaredQualifierDeclaredBean.Green.class, injected.blue);
      assertNotInstanceOf(DeclaredQualifierDeclaredBean.Green.class, injected.red);
   }

   @Test
   public void testDeclaredQualifierDeclaredProvider() throws Exception
   {
      init("org", "juzu", "impl", "spi", "inject", "declared", "provider", "qualifier", "declared");
      bootstrap.declareBean(DeclaredQualifierDeclaredProviderInjected.class, null, null);
      bootstrap.declareProvider(DeclaredQualifierDeclaredProvider.class, Collections.<Annotation>singleton(new ColorizedLiteral(Color.BLUE)), ColorlessProvider.class);
      bootstrap.declareProvider(DeclaredQualifierDeclaredProvider.class, Collections.<Annotation>singleton(new ColorizedLiteral(Color.RED)), ColorlessProvider.class);
      bootstrap.declareProvider(DeclaredQualifierDeclaredProvider.class, Collections.<Annotation>singleton(new ColorizedLiteral(Color.GREEN)), GreenProvider.class);
      boot();

      //
      DeclaredQualifierDeclaredProviderInjected injected = getBean(DeclaredQualifierDeclaredProviderInjected.class);
      assertNotNull(injected);
      assertNotNull(injected.blue);
      assertNotNull(injected.red);
      assertNotNull(injected.green);
      assertNotSame(injected.blue.getId(), injected.red.getId());
      assertNotSame(injected.green.getId(), injected.red.getId());
      assertNotSame(injected.blue.getId(), injected.green.getId());
      assertInstanceOf(DeclaredQualifierDeclaredProvider.class, injected.blue);
      assertInstanceOf(DeclaredQualifierDeclaredProvider.class, injected.red);
      assertInstanceOf(DeclaredQualifierDeclaredProvider.Green.class, injected.green);
      assertNotInstanceOf(DeclaredQualifierDeclaredProvider.Green.class, injected.blue);
      assertNotInstanceOf(DeclaredQualifierDeclaredProvider.Green.class, injected.red);
   }

   @Test
   public void testBoundBeanBeanSuperType() throws Exception
   {
      init("org", "juzu", "impl", "spi", "inject", "bound", "bean", "supertype");
      BoundBeanApple apple = new BoundBeanApple();
      bootstrap.bindBean(BoundBeanFruit.class, null, apple);
      bootstrap.declareBean(BoundBeanFruitInjected.class, null, null);
      boot();

      //
      BoundBeanFruitInjected beanObject = getBean(BoundBeanFruitInjected.class);
      assertNotNull(beanObject);
      assertNotNull(beanObject.fruit);
      assertSame(apple, beanObject.fruit);
   }

   @Test
   public void testRequestScopedProvider() throws Exception
   {
      init("org", "juzu", "impl", "spi", "inject", "requestscopedprovider");
      bootstrap.declareProvider(RequestBean.class, null, RequestBeanProvider.class);
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

   @Test
   public void testImplementationType() throws Exception
   {
      init("org", "juzu", "impl", "spi", "inject", "implementationtype");
      bootstrap.declareBean(Extended.class, null, Extension.class);
      boot();

      //
      Extended extended = getBean(Extended.class);
      assertEquals(Extension.class, extended.getClass());
   }

   @Test
   public void testDefaultScope() throws Exception
   {
      init("org", "juzu", "impl", "spi", "inject", "scope", "defaultscope");
      bootstrap.declareBean(UndeclaredScopeBean.class, null, null);
      boot();

      //
      UndeclaredScopeBean bean1 = getBean(UndeclaredScopeBean.class);
      UndeclaredScopeBean bean2 = getBean(UndeclaredScopeBean.class);
      assertTrue(bean1.count != bean2.count);
   }

   @Test
   public void testConstructorThrowsChecked() throws Exception
   {
      init("org", "juzu", "impl", "spi", "inject", "constructorthrowschecked");
      bootstrap.declareBean(ConstructorThrowsCheckedBean.class, null, null);
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

   @Test
   public void testConstructorThrowsRuntime() throws Exception
   {
      init("org", "juzu", "impl", "spi", "inject", "constructorthrowsruntime");
      bootstrap.declareBean(ConstructorThrowsRuntimeBean.class, null, null);
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

   @Test
   public void testConstructorThrowsError() throws Exception
   {
      init("org", "juzu", "impl", "spi", "inject", "constructorthrowserror");
      bootstrap.declareBean(ConstructorThrowsErrorBean.class, null, null);
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

   @Test
   public void testResolvableBeans() throws Exception
   {
      init("org", "juzu", "impl", "spi", "inject", "resolvebeans");
      bootstrap.declareBean(ResolveBeanSubclass1.class, null, null);
      bootstrap.declareBean(ResolveBeanSubclass2.class, null, null);
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

   @Test
   public void testLifeCycleUnscoped() throws Exception
   {
      init("org", "juzu", "impl", "spi", "inject", "lifecycle", "unscoped");
      bootstrap.declareBean(LifeCycleUnscopedBean.class, null, null);
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

   @Test
   public void testLifeCycleScoped() throws Exception
   {
      init("org", "juzu", "impl", "spi", "inject", "lifecycle", "scoped");
      bootstrap.declareBean(LifeCycleScopedBean.class, null, null);
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

   @Test
   public void testLifeCycleSingleton() throws Exception
   {
      init("org", "juzu", "impl", "spi", "inject", "lifecycle", "singleton");
      bootstrap.declareBean(LifeCycleSingletonBean.class, null, null);
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

   @Test
   public void testConfigurationURL() throws Exception
   {
      if (di == InjectImplementation.INJECT_SPRING)
      {
         URL configurationURL = Declared.class.getResource("spring.xml");
         assertNotNull(configurationURL);
         InputStream in = configurationURL.openStream();
         assertNotNull(in);
         Tools.safeClose(in);

         //
         init("org", "juzu", "impl", "spi", "inject", "configuration");
         bootstrap.declareBean(DeclaredInjected.class, null, null);
         ((SpringBuilder)bootstrap).setConfigurationURL(configurationURL);
         boot();

         //
         DeclaredInjected injected = getBean(DeclaredInjected.class);
         assertNotNull(injected);
         assertNotNull(injected.getDeclared());

         //
         Declared declared = getBean(Declared.class);
         assertNotNull(declared);
         declared = (Declared)getBean("declared");
         assertNotNull(declared);
      }
   }

   @Test
   public void testExport() throws Exception
   {
      if (di == InjectImplementation.CDI_WELD)
      {
         RAMFileSystem sources = new RAMFileSystem();
         RAMFileSystem classes = new RAMFileSystem();

         //
         RAMDir foo = sources.addDir(sources.getRoot(), "foo");
         foo.addFile("Bean1.java").update("package foo; public class Bean1 {}");
         foo.addFile("Bean2.java").update("package foo; @" + Export.class.getName() + " public class Bean2 {}");
         foo.addFile("Bean3.java").update("package foo; @" + Export.class.getName() + " public class Bean3 {}");
         foo.addFile("Bean4.java").update("package foo; @" + Export.class.getName() + " public class Bean4 {}");

         //
         CompilerHelper<RAMPath, RAMPath> helper = new CompilerHelper<RAMPath, RAMPath>(sources, classes);
         helper.assertCompile();
         URLClassLoader classLoader = new URLClassLoader(new URL[]{classes.getURL()}, Thread.currentThread().getContextClassLoader());

         //
         Class bean1Class = classLoader.loadClass("foo.Bean1");
         Class bean2Class = classLoader.loadClass("foo.Bean2");
         Class bean3Class = classLoader.loadClass("foo.Bean3");
         Class bean4Class = classLoader.loadClass("foo.Bean4");

         //
         init(classes, classLoader);
         bootstrap.declareBean(bean2Class, null, null);
         bootstrap.bindBean(bean3Class, null, bean3Class.newInstance());
         boot();

         //
         B bean1 = mgr.resolveBean(bean1Class);
         assertNotNull(bean1);
         B bean2 = mgr.resolveBean(bean2Class);
         assertNotNull(bean2);
         B bean3 = mgr.resolveBean(bean3Class);
         assertNotNull(bean3);
         B bean4 = mgr.resolveBean(bean4Class);
         assertNull(bean4);
      }
   }
}
