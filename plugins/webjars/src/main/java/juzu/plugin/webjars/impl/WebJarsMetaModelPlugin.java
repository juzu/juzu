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
import java.io.Writer;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.lang.model.element.PackageElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import juzu.impl.common.Logger;
import juzu.impl.common.Name;
import juzu.impl.common.Path;
import juzu.impl.common.Tools;
import juzu.impl.compiler.BaseProcessor;
import juzu.impl.compiler.ElementHandle;
import juzu.impl.compiler.MessageCode;
import juzu.impl.compiler.ProcessingContext;
import juzu.impl.metamodel.AnnotationKey;
import juzu.impl.metamodel.AnnotationState;
import juzu.impl.plugin.module.metamodel.ModuleMetaModel;
import juzu.impl.plugin.module.metamodel.ModuleMetaModelPlugin;
import juzu.plugin.webjars.WebJars;

import org.webjars.WebJarAssetLocator;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 *
 */
public class WebJarsMetaModelPlugin extends ModuleMetaModelPlugin {

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
  
  /** . */
  private HashMap<Name, AnnotationState> annotations;

  public WebJarsMetaModelPlugin() {
    super("webjars");
  }
  
  @Override
  public void init(ModuleMetaModel metaModel) {
    annotations = new HashMap<Name, AnnotationState>();
  }

  @Override
  public Set<Class<? extends java.lang.annotation.Annotation>> init(ProcessingContext env) {
    return Collections.<Class<? extends java.lang.annotation.Annotation>>singleton(WebJars.class);
  }
  
  @Override
  public void processAnnotationAdded(ModuleMetaModel metaModel, AnnotationKey key, AnnotationState added) {
    Name pkg = key.getElement().getPackageName();
    annotations.put(pkg, added);
  }

  @Override
  public void processAnnotationRemoved(ModuleMetaModel metaModel, AnnotationKey key, AnnotationState removed) {
    Name pkg = key.getElement().getPackageName();
    annotations.remove(pkg);
  }

  @Override
  public void postActivate(ModuleMetaModel metaModel) {
    annotations = new HashMap<Name, AnnotationState>();
  }
  
  @Override
  public void prePassivate(ModuleMetaModel metaModel) {
    // First clear annotation map
    HashMap<Name, AnnotationState> clone = annotations;
    annotations = null;

    //
    for (Map.Entry<Name, AnnotationState> entry : clone.entrySet()) {
      AnnotationState annotation = entry.getValue();
      Name pkg = entry.getKey();
      ProcessingContext env = metaModel.processingContext;
      List<AnnotationState> webJars = (List<AnnotationState>)annotation.get("value");
      if (webJars != null && webJars.size() > 0) {
        Name assetPkg = pkg.append("assets");
        for(AnnotationState webJar : webJars) {
          log.info("Processing declared webjars " + webJar);
          String id = (String)webJar.get("value");
          String path = "META-INF/maven/org.webjars/" + id + "/pom.properties";
          URL resource = WebJarAssetLocator.class.getClassLoader().getResource(path);
          ElementHandle.Package pkgHandle = ElementHandle.Package.create(pkg);
          PackageElement pkgElt = env.get(pkgHandle);
          String version;
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

          //
          WebJarAssetLocator locator = new WebJarAssetLocator();
          String folderPath = "/" + id + "/" + version;
          Set<String> assetsPaths = locator.listAssets(folderPath);
          for (String assetPath : assetsPaths) {
            URL assetURL = WebJarAssetLocator.class.getClassLoader().getResource(assetPath);
            if (assetURL != null) {
              String dst = assetPath.substring(WebJarAssetLocator.WEBJARS_PATH_PREFIX.length() + folderPath.length() + 1);
              Path.Absolute to = assetPkg.resolve(Path.parse(dst));
              try {
                log.info("Webjars " + webJar + " write on disk as " + to);
                FileObject fo = env.createResource(StandardLocation.CLASS_OUTPUT, to);
                Writer writer = fo.openWriter();
                try {
                  writer.write(Tools.read(assetURL));
                }
                finally {
                  Tools.safeClose(writer);
                }
              } catch (IOException e) {
                log.info("Resource " + to + " could not be written on disk", e);
              }
            } else {
              log.info("Could not resolve WebJars asset " + webJar + " with resource path " + assetURL);
            }
          }
        }
      }
    }
  }
}
