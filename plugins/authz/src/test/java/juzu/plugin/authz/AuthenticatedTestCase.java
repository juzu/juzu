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
package juzu.plugin.authz;

import juzu.impl.bridge.DescriptorBuilder;
import juzu.test.AbstractWebTestCase;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;

/**
 * @author Julien Viet
 */
public class AuthenticatedTestCase extends AbstractWebTestCase {

  @Deployment
  public static WebArchive createDeployment() {

    DescriptorBuilder def = DescriptorBuilder.DEFAULT.servletApp("juzu.authenticated");


    //
    DescriptorBuilder desc = new DescriptorBuilder(def) {

      @Override
      protected void appendWebXmlFooter(StringBuilder buffer) {
        buffer.append("" +
            "<security-constraint>\n" +
            "<web-resource-collection>\n" +
            "<web-resource-name>secured</web-resource-name>\n" +
            "<url-pattern>/</url-pattern>\n" +
            "</web-resource-collection>\n" +
            "<auth-constraint>\n" +
            "<role-name>myrole</role-name>\n" +
            "</auth-constraint>\n" +
            "</security-constraint>\n" +
            "<login-config>\n" +
            "<auth-method>BASIC</auth-method>\n" +
            "<realm-name>MyUserDatabase</realm-name>\n" +
            "</login-config>\n" +
            "<security-role>\n" +
            "<role-name>myrole</role-name>\n" +
            "</security-role>\n" +
            "<session-config>\n" +
            "<tracking-mode>URL</tracking-mode>\n" +
            "</session-config>\n");
        super.appendWebXmlFooter(buffer);
      }
    };

    WebArchive war = createServletDeployment(desc, true);

    war.addAsManifestResource("juzu/authenticated/resources/context.xml", "context.xml");

    return war;
  }

  @Test
  @RunAsClient
  public void testFoo() throws Exception {

    // Deny all
    HttpResponse response = execute(deploymentURL.toString() + "/denyall");
    assertEquals(403, response.getStatusLine().getStatusCode());

    // Manager
    response = execute(deploymentURL.toString() + "/manager");
    assertEquals(403, response.getStatusLine().getStatusCode());

    // My
    response = execute(deploymentURL.toString() + "/myrole");
    assertEquals(200, response.getStatusLine().getStatusCode());
  }

  private HttpResponse execute(String url) throws Exception {
    HttpClient builder = HttpClientBuilder.create().build();
    HttpClientContext context = new HttpClientContext();
    BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
    credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials("foo", "foo"));
    context.setCredentialsProvider(credentialsProvider);
    AuthCache authCache = new BasicAuthCache();
    HttpHost host = new HttpHost("localhost");
    authCache.put(host, new BasicScheme());
    context.setAuthCache(authCache);
    return builder.execute(new HttpGet(url), context);
  }
}
