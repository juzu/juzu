/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package juzu.impl.http;

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import junit.framework.AssertionFailedError;
import org.junit.Test;
import juzu.test.AbstractHttpTestCase;
import juzu.test.UserAgent;

import java.util.ArrayList;
import java.util.List;

/**  @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a> */
public class ResourceOrderTestCase extends AbstractHttpTestCase
{
   @Test
   public void testResourceOrder() throws Exception
   {
      assertDeploy("http", "resource");
      UserAgent ua = assertInitialPage();
      HtmlPage page = ua.getHomePage();
      DomNode head = page.getElementsByTagName("head").get(0);
      DomNodeList<DomNode> headChildren = head.getChildNodes();

      List<String> previous = new ArrayList<String>();
      for(DomNode node : headChildren) {
         assertOrder(previous, node);
         previous.add(node.getNodeName());
      }
   }

    private void assertOrder(List<String> previous, DomNode current) {

       if ("link".equals(current.getNodeName()) && previous.contains("script"))
       {
          throw new AssertionFailedError("js must be set before css resource");
       }

   }
}
