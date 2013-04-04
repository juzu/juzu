/*
 * Copyright 2013 eXo Platform SAS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
