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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import juzu.asset.AssetLocation;
import juzu.impl.compiler.ProcessingContext;
import juzu.impl.metamodel.AnnotationKey;
import juzu.impl.metamodel.AnnotationState;
import juzu.impl.plugin.application.metamodel.ApplicationMetaModel;
import juzu.impl.plugin.application.metamodel.ApplicationMetaModelPlugin;
import juzu.impl.plugin.asset.Asset;
import juzu.impl.plugin.asset.AssetsMetaModel;
import juzu.plugin.amd.Modules;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 * 
 */
public class AmdMetaModelPlugin extends ApplicationMetaModelPlugin {

  /** . */
  private static final Asset REQUIRE_JS = new Asset(
      "juzu.amd",
      "script",
      "/juzu/impl/plugin/amd/require.js",
      null, Collections.<String>emptyList(),
      AssetLocation.APPLICATION,
      null, null);

  public AmdMetaModelPlugin() {
    super("amd");
  }

  @Override
  public Set<Class<? extends java.lang.annotation.Annotation>> init(ProcessingContext env) {
    return Collections.<Class<? extends java.lang.annotation.Annotation>>singleton(Modules.class);
  }

  @Override
  public void processAnnotationAdded(ApplicationMetaModel metaModel, AnnotationKey key, AnnotationState added) {
    if (metaModel.getHandle().equals(key.getElement())) {
      List<Map<String, Serializable>> value = (List<Map<String, Serializable>>)added.get("value");
      Integer maxAge = (Integer)added.get("maxAge");

      //
      AssetsMetaModel assetsMetaModel = metaModel.getChild(AssetsMetaModel.KEY);
      assetsMetaModel.removeAssets("module");
      ArrayList<Asset> list = new ArrayList<Asset>();
      for (Map<String, Serializable> a : value) {
        HashMap<String, Serializable> asset = new HashMap<String, Serializable>(a);
        if (asset.get("location") == null) {
          asset.put("location", AssetLocation.APPLICATION.name());
        }
        if (asset.get("maxAge") == null && maxAge != null) {
          asset.put("maxAge", maxAge);
        }
        if (asset.get("id") == null) {
          asset.put("id", asset.get("value"));
        }
        List<String> aliases = (List<String>)a.get("aliases");
        String adapter = (String)a.get("adapter");
        list.add(new ModuleAsset(asset, adapter, aliases));
      }

      //
      for (Asset asset : list) {
        assetsMetaModel.addAsset(asset);
      }

      //
      assetsMetaModel.addAsset(REQUIRE_JS);
    }
  }

  @Override
  public void processAnnotationRemoved(ApplicationMetaModel metaModel, AnnotationKey key, AnnotationState removed) {
    if (metaModel.getHandle().equals(key.getElement())) {
      AssetsMetaModel assetsMetaModel = metaModel.getChild(AssetsMetaModel.KEY);
      assetsMetaModel.removeAssets("module");
      assetsMetaModel.removeAsset(REQUIRE_JS);
    }
  }
}
