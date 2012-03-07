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

import org.juzu.impl.spi.inject.configuration.Declared;
import org.juzu.impl.spi.inject.configuration.DeclaredInjected;
import org.juzu.impl.spi.inject.spring.SpringBuilder;
import org.juzu.impl.utils.Tools;

import java.io.InputStream;
import java.net.URL;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class SpringManagerTestCase extends InjectManagerTestCase<Class<?>, Object>
{

   @Override
   protected InjectBuilder getManager() throws Exception
   {
      return new SpringBuilder();
   }

   @Override
   public void testRequestScopedProvider() throws Exception
   {
   }

   @Override
   public void testSiblingProducers() throws Exception
   {
   }

   public void testConfigurationURL() throws Exception
   {
      URL configurationURL = Declared.class.getResource("spring.xml");
      assertNotNull(configurationURL);
      InputStream in = configurationURL.openStream();
      assertNotNull(in);
      Tools.safeClose(in);

      //
      init("org", "juzu", "impl", "spi", "inject", "configuration");
      bootstrap.declareBean(DeclaredInjected.class, null);
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

/*
   @Override
   public void testPreDestroyInScope() throws Exception
   {
   }

   @Override
   public void testPostConstruct() throws Exception
   {
   }

   @Override
   public void testPreDestroy() throws Exception
   {
   }
*/
}
