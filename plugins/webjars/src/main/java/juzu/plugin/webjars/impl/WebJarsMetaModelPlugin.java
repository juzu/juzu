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
import java.io.Writer;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.tools.FileObject;
import javax.tools.StandardLocation;

import juzu.impl.common.Logger;
import juzu.impl.common.Name;
import juzu.impl.common.Path;
import juzu.impl.common.Tools;
import juzu.impl.compiler.BaseProcessor;
import juzu.impl.compiler.ElementHandle;
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
    Name pkg = key.getElement().getPackage();
    annotations.put(pkg, added);
  }

  @Override
  public void processAnnotationRemoved(ModuleMetaModel metaModel, AnnotationKey key, AnnotationState removed) {
    Name pkg = key.getElement().getPackage();
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
      ElementHandle.Package pkgHandle = ElementHandle.Package.create(pkg);
      List<String> resources = (List<String>)annotation.get("value");
      
      //
      if (resources != null && resources.size() > 0) {

        // For now we use the hardcoded assets package
        Name assetPkg = pkg.append("assets");
        for(String resource : resources) {
          log.log("Processing declared webjars " + resource);
          
          URL url = Thread.currentThread().getContextClassLoader().getResource( new WebJarAssetLocator().getFullPath(resource));
          if (url != null) {
            Path.Absolute to = assetPkg.resolve(resource).as("js");
            try {
              log.log("Webjars " + resource + " write on disk as " + to);
              FileObject fo = env.createResource(StandardLocation.CLASS_OUTPUT, to);
              Writer writer = fo.openWriter();
              try {
                writer.write(Tools.read(url));
              }
              finally {
                Tools.safeClose(writer);
              }
            } catch (IOException e) {
              log.log("Resource " + to + " could not be written on disk", e);
            }
          } else {
            log.log("Could not resolve WebJars asset " + resource);
          }
        }
      }
    }
  }
}
