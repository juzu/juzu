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
package juzu.impl.bridge;

import juzu.bridge.portlet.JuzuPortlet;
import juzu.bridge.servlet.JuzuServlet;
import juzu.impl.asset.AssetServlet;
import juzu.impl.common.RunMode;
import juzu.impl.common.Tools;
import juzu.impl.inject.spi.InjectorProvider;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

/**
 * The bridge descriptor for a web archive.
 *
 * @author Julien Viet
 */
public class DescriptorBuilder {

  /** . */
  public static DescriptorBuilder DEFAULT = new DescriptorBuilder(
      InjectorProvider.GUICE,
      Tools.ISO_8859_1,
      RunMode.PROD,
      null,
      new String[0],
      new int[0],
      new String[0],
      new String[0],
      new String[0],
      new String[0],
      new String[0],
      new Integer[0],
      new Boolean[0],
      new String[0],
      new String[0]
  );

  /** . */
  private final RunMode runMode;

  /** . */
  private final String sourcePath;

  /** . */
  private final InjectorProvider injector;

  /** . */
  private final String[] applicationNames;

  /** . */
  private final int[] applicationTypes;

  /** . */
  private final String[] urlPatterns;

  /** . */
  private final Charset requestEncoding;

  /** . */
  private final String[] servletsName;

  /** . */
  private final String[] servletsUrlPattern;

  /** . */
  private final String[] servletsClass;

  /** . */
  private final Integer[] servletsLoadOnStartup;

  /** . */
  private final Boolean[] servletsAsync;

  /** . */
  private final String[] listenersClass;

  /** . */
  private final String[] resourcesEnvRefName;

  /** . */
  private final String[] resourcesEnvRefType;

  private DescriptorBuilder(
      InjectorProvider injector,
      Charset requestEncoding,
      RunMode runMode,
      String sourcePath,
      String[] applicationNames,
      int[] applicationTypes,
      String[] urlPatterns,
      String[] listenersClass,
      String[] servletsName,
      String[] servletsUrlPattern,
      String[] servletsClass,
      Integer[] servletsLoadOnStartup,
      Boolean[] servletsAsync,
      String[] resourcesEnvRefName,
      String[] resourcesEnvRefType) {
    this.applicationNames = applicationNames;
    this.runMode = runMode;
    this.injector = injector;
    this.applicationTypes = applicationTypes;
    this.sourcePath = sourcePath;
    this.urlPatterns = urlPatterns;
    this.requestEncoding = requestEncoding;
    this.listenersClass = listenersClass;
    this.servletsName = servletsName;
    this.servletsUrlPattern = servletsUrlPattern;
    this.servletsClass = servletsClass;
    this.servletsLoadOnStartup = servletsLoadOnStartup;
    this.servletsAsync = servletsAsync;
    this.resourcesEnvRefName = resourcesEnvRefName;
    this.resourcesEnvRefType = resourcesEnvRefType;
  }

  public DescriptorBuilder runMode(RunMode runMode) {
    if (runMode == null) {
      throw new NullPointerException("No null run mode");
    }
    return new DescriptorBuilder(injector, requestEncoding, runMode, sourcePath, applicationNames, applicationTypes, urlPatterns, listenersClass, servletsName, servletsUrlPattern, servletsClass, servletsLoadOnStartup, servletsAsync, resourcesEnvRefName, resourcesEnvRefType);
  }

  public DescriptorBuilder injector(InjectorProvider injector) {
    if (injector == null) {
      throw new NullPointerException("No null injector");
    }
    return new DescriptorBuilder(injector, requestEncoding, runMode, sourcePath, applicationNames, applicationTypes, urlPatterns, listenersClass, servletsName, servletsUrlPattern, servletsClass, servletsLoadOnStartup, servletsAsync, resourcesEnvRefName, resourcesEnvRefType);
  }

  public DescriptorBuilder portletApp(String applicationName, String portletName) {
    return app(applicationName, 1, portletName);
  }

  public DescriptorBuilder servletApp(String applicationName) {
    return servletApp(applicationName, "/");
  }

  public DescriptorBuilder servletApp(String applicationName, String urlPattern) {
    if (urlPattern == null) {
      throw new NullPointerException("No null url pattern");
    }
    return app(applicationName, 0, urlPattern);
  }

  private DescriptorBuilder app(String applicationName, int applicationType, String urlPattern) {
    int[] tmp = Arrays.copyOf(applicationTypes, applicationTypes.length + 1);
    tmp[applicationTypes.length] = applicationType;
    return new DescriptorBuilder(
        injector,
        requestEncoding,
        runMode,
        sourcePath,
        Tools.appendTo(applicationNames, applicationName),
        tmp,
        Tools.appendTo(urlPatterns, urlPattern),
        listenersClass,
        servletsName,
        servletsUrlPattern,
        servletsClass,
        servletsLoadOnStartup,
        servletsAsync,
        resourcesEnvRefName,
        resourcesEnvRefType
    );
  }

  public DescriptorBuilder sourcePath(String sourcePath) {
    if (sourcePath == null) {
      throw new NullPointerException("No null source path");
    }
    return new DescriptorBuilder(injector, requestEncoding, runMode, sourcePath, applicationNames, applicationTypes, urlPatterns, listenersClass, servletsName, servletsUrlPattern, servletsClass, servletsLoadOnStartup, servletsAsync, resourcesEnvRefName, resourcesEnvRefType);
  }

  public DescriptorBuilder requestEncoding(Charset requestEncoding) {
    if (requestEncoding == null) {
      throw new NullPointerException("No null request encoding");
    }
    return new DescriptorBuilder(injector, requestEncoding, runMode, sourcePath, applicationNames, applicationTypes, urlPatterns, listenersClass, servletsName, servletsUrlPattern, servletsClass, servletsLoadOnStartup, servletsAsync, resourcesEnvRefName, resourcesEnvRefType);
  }

  public DescriptorBuilder listener(String listenerClass) {
    if (requestEncoding == null) {
      throw new NullPointerException("No null listener class");
    }
    return new DescriptorBuilder(injector, requestEncoding, runMode, sourcePath, applicationNames, applicationTypes, urlPatterns, Tools.appendTo(listenersClass, listenerClass), servletsName, servletsUrlPattern, servletsClass, servletsLoadOnStartup, servletsAsync, resourcesEnvRefName, resourcesEnvRefType);
  }

  public DescriptorBuilder servlet(String servletName, String servletUrlPattern, String servletClass, Integer servletLoadOnStartup, Boolean servletAsync) {
    return new DescriptorBuilder(injector, requestEncoding, runMode, sourcePath, applicationNames, applicationTypes, urlPatterns, listenersClass, Tools.appendTo(servletsName, servletName), Tools.appendTo(servletsUrlPattern, servletUrlPattern), Tools.appendTo(servletsClass, servletClass), Tools.appendTo(servletsLoadOnStartup, servletLoadOnStartup), Tools.appendTo(servletsAsync, servletAsync), resourcesEnvRefName, resourcesEnvRefType);
  }

  public DescriptorBuilder embedPortletContainer() {
    return servlet("EmbedServlet", "/embed/*", "org.gatein.pc.embed.EmbedServlet", 0, null);
  }

  public DescriptorBuilder resourceEnvRef(String name, String type) {
    return new DescriptorBuilder(injector, requestEncoding, runMode, sourcePath, applicationNames, applicationTypes, urlPatterns, listenersClass, servletsName, servletsUrlPattern, servletsClass,servletsLoadOnStartup, servletsAsync, Tools.appendTo(resourcesEnvRefName, name), Tools.appendTo(resourcesEnvRefType, type));
  }

  public Iterable<String> getApplications() {
    return Tools.iterable(applicationNames);
  }

  public String getURLPattern() {
    return urlPatterns.length > 0 ? urlPatterns[0] : null;
  }

  public RunMode getRunMode() {
    return runMode;
  }

  public InjectorProvider getInjector() {
    return injector;
  }

  public Charset getRequestEncoding() {
    return requestEncoding;
  }

  private void appendContextParam(StringBuilder buffer, String paramName, String paramValue) {
    buffer.append("<context-param>\n");
    buffer.append("<param-name>").append(paramName).append("</param-name>");
    buffer.append("<param-value>").append(paramValue).append("</param-value>");
    buffer.append("</context-param>\n");
  }

  private void appendServlet(StringBuilder buffer, Map<String, String> initParams, String servletName, String servletClass, Integer loadOnStartup, Boolean async) {
    buffer.append("<servlet>");
    buffer.append("<servlet-name>").append(servletName).append("</servlet-name>");
    buffer.append("<servlet-class>").append(servletClass).append("</servlet-class>");
    if (initParams.size() > 0) {
      for (Map.Entry<String, String> initParam : initParams.entrySet()) {
        buffer.append("<init-param>");
        buffer.append("<param-name>").append(initParam.getKey()).append("</param-name>");
        buffer.append("<param-value>").append(initParam.getValue()).append("</param-value>");
        buffer.append("</init-param>");
      }
    }
    if (loadOnStartup != null) {
      buffer.append("<load-on-startup>").append(loadOnStartup).append("</load-on-startup>");
    }
    if (async != null) {
      buffer.append("<async-supported>").append(async).append("</async-supported>");
    }
    buffer.append("</servlet>");
  }

  private void appendMapping(StringBuilder buffer, String servletName, String urlPattern) {
    buffer.append("<servlet-mapping>");
    buffer.append("<servlet-name>").append(servletName).append("</servlet-name>");
    buffer.append("<url-pattern>").append(urlPattern).append("</url-pattern>");
    buffer.append("</servlet-mapping>");
  }

  private void appendListener(StringBuilder buffer, String listenerClass) {
    buffer.append("<listener>");
    buffer.append("<listener-class>").append(listenerClass).append("</listener-class>");
    buffer.append("</listener>");
  }

  public String toWebXml() {

    StringBuilder buffer = new StringBuilder();
    buffer.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>");
    buffer.append("<web-app xmlns=\"http://java.sun.com/xml/ns/javaee\"\n" +
        "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
        "xsi:schemaLocation=\"http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd\"\n" +
        "version=\"3.0\">\n");
    appendContextParam(buffer, "juzu.run_mode", runMode.getValue());
    appendContextParam(buffer, "juzu.inject", injector.getValue());
    appendContextParam(buffer, "juzu.request_encoding", requestEncoding.name());
    if (sourcePath != null) {
      appendContextParam(buffer, "juzu.src_path", sourcePath);
    }
    for (String listenerClass : listenersClass) {
      appendListener(buffer, listenerClass);
    }

    //
    for (int i = 0;i < applicationNames.length;i++) {
      if (applicationTypes[i] == 0) {
        appendServlet(
            buffer,
            Collections.singletonMap(BridgeConfig.APP_NAME, applicationNames[i]),
            "JuzuServlet-" + i,
            JuzuServlet.class.getName(),
            null,
            true);
        appendMapping(buffer, "JuzuServlet-" + i, urlPatterns[i]);
      }
    }

    //
    for (int i = 0;i < servletsName.length;i++) {
      appendServlet(buffer, Collections.<String, String>emptyMap(), servletsName[i], servletsClass[i], servletsLoadOnStartup[i], servletsAsync[i]);
      appendMapping(buffer, servletsName[i], servletsUrlPattern[i]);
    }

    //
    appendServlet(buffer, Collections.<String, String>emptyMap(), "AssetServlet", AssetServlet.class.getName(), 0, null);
    appendMapping(buffer, "AssetServlet", "/assets/*");

    //
    if (resourcesEnvRefName.length > 0) {
      for (int i = 0;i < resourcesEnvRefName.length;i++) {
        buffer.append("<resource-env-ref>");
        buffer.append("<resource-env-ref-name>").append(resourcesEnvRefName[i]).append("</resource-env-ref-name>");
        buffer.append("<resource-env-ref-type>").append(resourcesEnvRefType[i]).append("</resource-env-ref-type>");
        buffer.append("</resource-env-ref>");
      }
    }

    //
    buffer.append("</web-app>");
    return buffer.toString();
  }

  public String toPortletXml() {
    StringBuilder buffer = new StringBuilder();
    buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<portlet-app xmlns=\"http://java.sun.com/xml/ns/portlet/portlet-app_2_0.xsd\"\n" +
        "version=\"2.0\"\n" +
        "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
        "xsi:schemaLocation=\"http://java.sun.com/xml/ns/portlet/portlet-app_2_0.xsd http://java.sun.com/xml/ns/portlet/portlet-app_2_0.xsd\">");
    for (int i = 0;i < applicationNames.length;i++) {
      if (applicationTypes[i] == 1) {
        buffer.append("<portlet>");
        buffer.append("<portlet-name>").append(urlPatterns[i]).append("</portlet-name>");
        buffer.append("<display-name xml:lang=\"EN\">").append(urlPatterns[i]).append("</display-name>");
        buffer.append("<portlet-class>").append(JuzuPortlet.class.getName()).append("</portlet-class>");
        buffer.append("<init-param>");
        buffer.append("<name>").append(BridgeConfig.APP_NAME).append("</name>");
        buffer.append("<value>").append(applicationNames[i]).append("</value>");
        buffer.append("</init-param>");
        buffer.append("<supports>");
        buffer.append("<mime-type>text/html</mime-type>");
        buffer.append("</supports>");
        buffer.append("<portlet-info>");
        buffer.append("<title>").append(urlPatterns[i]).append(" Application</title>");
        buffer.append("</portlet-info>");
        buffer.append("</portlet>");
      }
    }
    buffer.append("</portlet-app>");
    return buffer.toString();
  }
}
