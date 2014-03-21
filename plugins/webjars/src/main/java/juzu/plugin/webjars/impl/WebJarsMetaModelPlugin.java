/*
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
package juzu.plugin.webjars.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.lang.model.element.PackageElement;

import juzu.impl.common.Logger;
import juzu.impl.common.Name;
import juzu.impl.common.Tools;
import juzu.impl.compiler.BaseProcessor;
import juzu.impl.compiler.ElementHandle;
import juzu.impl.compiler.MessageCode;
import juzu.impl.compiler.ProcessingContext;
import juzu.impl.metamodel.AnnotationKey;
import juzu.impl.metamodel.AnnotationState;
import juzu.impl.plugin.application.metamodel.ApplicationMetaModel;
import juzu.impl.plugin.application.metamodel.ApplicationMetaModelPlugin;
import juzu.impl.plugin.asset.AssetsMetaModel;
import juzu.plugin.webjars.WebJars;

import org.webjars.WebJarAssetLocator;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 *
 */
public class WebJarsMetaModelPlugin extends ApplicationMetaModelPlugin {

  /** . */
  static final Pattern VERSION_STRIPPER = Pattern.compile("^([^/]+)/[^/]+/(.*)$");

  /** . */
  public static final MessageCode MISSING_WEBJAR = new MessageCode(
      "MISSING_WEBJAR",
      "Missing Webjar %1$s");

  /** . */
  public static final MessageCode INVALID_WEBJAR = new MessageCode(
      "INVALID_WEBJAR",
      "Invalid Webjar %1$s %2$s");

  /** . */
  static final Logger log = BaseProcessor.getLogger(WebJarsMetaModelPlugin.class);
  
  public WebJarsMetaModelPlugin() {
    super("webjars");
  }
  
  @Override
  public Set<Class<? extends java.lang.annotation.Annotation>> init(ProcessingContext env) {
    return Collections.<Class<? extends java.lang.annotation.Annotation>>singleton(WebJars.class);
  }
  
  @Override
  public void processAnnotationAdded(ApplicationMetaModel metaModel, AnnotationKey key, AnnotationState added) {
    Name pkg = key.getElement().getPackageName();
    AssetsMetaModel assetsMetaModel = metaModel.getChild(AssetsMetaModel.KEY);
    for (Map.Entry<String, URL> entry : getAssets(metaModel, added).entrySet()) {
      assetsMetaModel.addResource(entry.getKey(), entry.getValue());

    }
  }

  @Override
  public void processAnnotationRemoved(ApplicationMetaModel metaModel, AnnotationKey key, AnnotationState removed) {
    Name pkg = key.getElement().getPackageName();
    AssetsMetaModel assetsMetaModel = metaModel.getChild(AssetsMetaModel.KEY);
    for (Map.Entry<String, URL> entry : getAssets(metaModel, removed).entrySet()) {
      assetsMetaModel.removeResource(entry.getKey(), entry.getValue());

    }
  }

  private Map<String, URL> getAssets(ApplicationMetaModel metaModel, AnnotationState annotation) {

    //
    Map<String, URL> ret = Collections.emptyMap();

    //
    Name pkg = metaModel.getHandle().getPackageName();
    ProcessingContext env = metaModel.processingContext;
    List<AnnotationState> webJars = (List<AnnotationState>)annotation.get("value");
    if (webJars != null && webJars.size() > 0) {
      for(AnnotationState webJar : webJars) {
        log.info("Processing declared webjars " + webJar);
        String id = (String)webJar.get("value");
        String version = (String)webJar.get("version");

        //
        if (version == null || version.length() == 0) {
          String path = "META-INF/maven/org.webjars/" + id + "/pom.properties";
          URL resource = WebJarAssetLocator.class.getClassLoader().getResource(path);
          ElementHandle.Package pkgHandle = ElementHandle.Package.create(pkg);
          PackageElement pkgElt = env.get(pkgHandle);
          if (resource == null) {
            throw MISSING_WEBJAR.failure(pkgElt, id);
          } else {
            Properties props = new Properties();
            InputStream in = null;
            try {
              in = resource.openStream();
              props.load(in);
              version = props.getProperty("version");
            }
            catch (IOException e) {
              throw INVALID_WEBJAR.failure(pkgElt, id, "Could not read " + path).initCause(e);
            }
            finally {
              Tools.safeClose(in);
            }
            if (version == null) {
              throw INVALID_WEBJAR.failure(pkgElt, id, "No version found in " + path);
            }
          }
        }

        //
        Boolean strip = (Boolean)webJar.get("stripVersion");

        //
        WebJarAssetLocator locator = new WebJarAssetLocator();
        String folderPath = "/" + id + "/" + version;
        Set<String> assetsPaths = locator.listAssets(folderPath);
        log.info("Webjars " + webJar + " resolved assets " + assetsPaths + " from " + folderPath);
        for (String assetPath : assetsPaths) {
          URL assetURL = WebJarAssetLocator.class.getClassLoader().getResource(assetPath);
          if (assetURL != null) {
            String dst = assetPath.substring(WebJarAssetLocator.WEBJARS_PATH_PREFIX.length() + 1);
            if (Boolean.TRUE.equals(strip)) {
              Matcher matcher = VERSION_STRIPPER.matcher(dst);
              if (matcher.matches()) {
                String stripped = matcher.group(1) + "/" + matcher.group(2);
                log.info("Rewriting asset value " + dst + " as " + stripped);
                dst = stripped;
              } else {
                throw INVALID_WEBJAR.failure("Asset version cannot be determined: " + dst);
              }
            }
            if (ret.isEmpty()) {
              ret = new HashMap<String, URL>();
            }
            ret.put(dst, assetURL);
            log.info("Webjars " + webJar + " adding asset resource " + assetPath + " as " + dst);
          } else {
            log.info("Could not resolve WebJars asset " + webJar + " with resource path " + assetPath);
          }
        }
      }
    }

    //
    return ret;
  }
}
