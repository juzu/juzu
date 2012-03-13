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

package org.juzu.impl.http;

import com.gargoylesoftware.htmlunit.BinaryPage;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.UnexpectedPage;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.junit.Test;
import org.juzu.test.AbstractHttpTestCase;
import org.juzu.test.UserAgent;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class BinaryTestCase extends AbstractHttpTestCase
{

   @Test
   public void testBinary() throws Exception
   {
      assertDeploy("http", "binary");
      UserAgent ua = assertInitialPage();
      HtmlPage page = ua.getHomePage();
      String url = page.asText();
      UnexpectedPage resource = ua.getPage(UnexpectedPage.class, url);
      WebResponse resp = resource.getWebResponse();
      assertEquals("application/octet-stream", resp.getContentType());
      assertEquals("hello", resp.getContentAsString());
   }
}
