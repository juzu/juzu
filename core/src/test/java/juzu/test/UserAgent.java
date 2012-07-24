/*
 * Copyright (C) 2012 eXo Platform SAS.
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

package juzu.test;

import com.gargoylesoftware.htmlunit.AlertHandler;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.junit.Assert;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class UserAgent {

  /** . */
  private WebClient client;

  /** . */
  private final URL homeURL;

  /** . */
  private Page currentPage;

  /** . */
  private IdentityHashMap<Page, List<String>> alerts;

  public UserAgent(URL homeURL) {
    WebClient client = new WebClient(BrowserVersion.FIREFOX_3_6);
    client.setAlertHandler(new AlertHandler() {
      public void handleAlert(Page page, String message) {
        List<String> l = alerts.get(page);
        if (l == null) {
          alerts.put(page, l = new ArrayList<String>());
        }
        l.add(message);
      }
    });

    //
    this.alerts = new IdentityHashMap<Page, List<String>>();
    this.client = client;
    this.homeURL = homeURL;
  }

  public HtmlPage getPage(URL url) {
    return getPage(HtmlPage.class, url);
  }

  public HtmlPage getPage(String path) {
    return getPage(HtmlPage.class, path);
  }

  public <P extends Page> P getPage(Class<P> pageType, URL url) {
    Page page;
    try {
      page = client.getPage(url);
    }
    catch (FailingHttpStatusCodeException e) {
      throw AbstractTestCase.failure("Cannot get initial page", e);
    }
    catch (IOException e) {
      throw AbstractTestCase.failure("Cannot get initial page", e);
    }
    if (pageType.isInstance(page)) {
      currentPage = page;
      return pageType.cast(page);
    }
    else {
      throw AbstractTestCase.failure("Was expecting an HTML page instead of " + page + " for URL " + page.getUrl());
    }
  }

  public <P extends Page> P getPage(Class<P> pageType, String path) {
    URL url;
    try {
      url = homeURL.toURI().resolve(path).toURL();
    }
    catch (Exception e) {
      throw AbstractTestCase.failure("Cannot build page URL " + path);
    }
    return getPage(pageType, url);
  }

  public HtmlPage getHomePage() {
    return getPage(homeURL);
  }

  public void assertRedirect(String expectedLocation, String url) {
    boolean redirectEnabled = client.isRedirectEnabled();
    boolean throwExceptionOnFailingStatusCode = client.isThrowExceptionOnFailingStatusCode();
    try {
      client.setRedirectEnabled(false);
      client.setThrowExceptionOnFailingStatusCode(false);
      Page redirect = client.getPage(url);
      WebResponse resp = redirect.getWebResponse();
      Assert.assertEquals(302, resp.getStatusCode());
      Assert.assertEquals(expectedLocation, resp.getResponseHeaderValue("Location"));
    }
    catch (IOException e) {
      throw AbstractTestCase.failure("Cannot get load " + url, e);
    }
    finally {
      client.setRedirectEnabled(redirectEnabled);
      client.setThrowExceptionOnFailingStatusCode(throwExceptionOnFailingStatusCode);
    }
  }

  public List<String> getAlerts(Page page) {
    return alerts.get(page);
  }
}
