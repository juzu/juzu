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

package org.juzu.test;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.juzu.impl.spi.inject.InjectImplementation;
import org.juzu.test.protocol.http.InvocationServlet;
import org.juzu.test.protocol.mock.MockApplication;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
@RunWith(Arquillian.class)
public abstract class AbstractHttpTestCase extends AbstractTestCase
{

   /** . */
   private static AbstractHttpTestCase currentTest;

   /** The currently deployed application. */
   private MockApplication<?> application;

   public static MockApplication<?> getApplication() throws IllegalStateException
   {
      if (currentTest == null)
      {
         throw new IllegalStateException("No deployed test");
      }
      return currentTest.application;
   }

   @Before
   @Override
   public void setUp() throws Exception
   {
      currentTest = this;
   }

   @After
   @Override
   public void tearDown() throws Exception
   {
      application = null;
      currentTest = null;
   }

   @Deployment(testable=false)
   public static WebArchive createDeployment()
   {
      URL descriptor = InvocationServlet.class.getResource("web.xml");
      URL jquery = InvocationServlet.class.getResource("jquery-1.7.1.js");
      return ShrinkWrap.create(WebArchive.class, "juzu.war").
         addAsWebResource(jquery, "jquery.js").
         setWebXML(descriptor);
   }

   @ArquillianResource
   protected URL deploymentURL;

   public MockApplication<?> assertDeploy(String... packageName)
   {
      try
      {
         return application = application(InjectImplementation.CDI_WELD, packageName);
      }
      catch (Exception e)
      {
         throw failure("Could not deploy application " + Arrays.asList(packageName), e);
      }
   }
   
   public void assertInternalError()
   {
      WebClient client = new WebClient();
      try
      {
         Page page = client.getPage(deploymentURL + "/juzu");
         throw failure("Was expecting an internal error instead of page " + page.toString());
      }
      catch (FailingHttpStatusCodeException e)
      {
         assertEquals(500, e.getStatusCode());
      }
      catch (IOException e)
      {
         throw failure("Was not expecting io exception", e);
      }
   }
   
   public UserAgent assertInitialPage()
   {
      return new UserAgent(deploymentURL);
   }
}
