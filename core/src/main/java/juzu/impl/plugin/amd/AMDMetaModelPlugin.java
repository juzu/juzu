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
package juzu.impl.plugin.amd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.inject.internal.asm.$ClassAdapter;
import juzu.impl.common.Tools;
import juzu.impl.compiler.ProcessingContext;
import juzu.impl.metamodel.AnnotationKey;
import juzu.impl.metamodel.AnnotationState;
import juzu.impl.plugin.application.metamodel.ApplicationMetaModel;
import juzu.impl.plugin.application.metamodel.ApplicationMetaModelPlugin;
import juzu.impl.plugin.asset.Asset;
import juzu.impl.plugin.asset.AssetsMetaModel;
import juzu.plugin.amd.Defines;
import juzu.plugin.amd.Modules;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 * 
 */
public class AMDMetaModelPlugin extends ApplicationMetaModelPlugin {

  public AMDMetaModelPlugin() {
    super("amd");
  }

  @Override
  public Set<Class<? extends java.lang.annotation.Annotation>> init(ProcessingContext env) {
    return Tools.set(Modules.class, Defines.class);
  }

  @Override
  public void processAnnotationAdded(ApplicationMetaModel metaModel, AnnotationKey key, AnnotationState added) {
    if (metaModel.getHandle().equals(key.getElement())) {
      List<Map<String, Object>> value = (List<Map<String, Object>>)added.get("value");
      AssetsMetaModel assetsMetaModel = metaModel.getChild(AssetsMetaModel.KEY);
      boolean module = key.getType().getIdentifier().equals("Modules");
      assetsMetaModel.removeAssets(module ? "module" : "define");
      for (Map<String, Object> a : value) {
        AnnotationState asset = (AnnotationState)a.get("value");
        Asset amdAsset;
        if (module) {
          List<String> aliases = (List<String>)a.get("aliases");
          String adapter = (String)a.get("adapter");
          amdAsset = new ModuleAsset(asset, adapter, aliases);
        } else {
          amdAsset = new Asset("define", asset);
        }
        assetsMetaModel.addAsset(amdAsset);
      }
    }
  }

  @Override
  public void processAnnotationRemoved(ApplicationMetaModel metaModel, AnnotationKey key, AnnotationState removed) {
    if (metaModel.getHandle().equals(key.getElement())) {
      AssetsMetaModel assetsMetaModel = metaModel.getChild(AssetsMetaModel.KEY);
      boolean module = key.getType().getIdentifier().equals("Modules");
      assetsMetaModel.removeAssets(module ? "module" : "define");
    }
  }
}
