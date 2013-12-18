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

import juzu.impl.common.JSON;
import juzu.impl.common.Name;
import juzu.impl.common.Tools;
import juzu.impl.compiler.ElementHandle;
import juzu.impl.compiler.ProcessingContext;
import juzu.impl.metamodel.AnnotationKey;
import juzu.impl.metamodel.AnnotationState;
import juzu.impl.plugin.application.metamodel.ApplicationMetaModel;
import juzu.impl.plugin.application.metamodel.ApplicationMetaModelPlugin;
import juzu.impl.plugin.asset.Asset;
import juzu.impl.plugin.asset.AssetsMetaModel;
import juzu.plugin.amd.Defines;
import juzu.plugin.amd.Requires;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 * 
 */
public class AMDMetaModelPlugin extends ApplicationMetaModelPlugin {

  /** . */
  private HashMap<ElementHandle.Package, AnnotationState> defines =
    new HashMap<ElementHandle.Package, AnnotationState>();

  /** . */
  private HashMap<ElementHandle.Package, AnnotationState> requires =
    new HashMap<ElementHandle.Package, AnnotationState>();

  public AMDMetaModelPlugin() {
    super("amd");
  }

  @Override
  public Set<Class<? extends java.lang.annotation.Annotation>> init(ProcessingContext env) {
    return Tools.set(Defines.class, Requires.class);
  }

  @Override
  public void processAnnotationAdded(ApplicationMetaModel metaModel, AnnotationKey key, AnnotationState added) {

    if (metaModel.getHandle().equals(key.getElement())) {
      String location = (String)added.get("location");
      List<Map<String, Object>> value = (List<Map<String, Object>>)added.get("value");
      AssetsMetaModel assetsMetaModel = metaModel.getChild(AssetsMetaModel.KEY);
      boolean define = key.getType().getIdentifier().equals("Defines");
      assetsMetaModel.removeAssets(define ? "define" : "require");
      for (Map<String, Object> asset : value) {
        String assetId = (String)asset.get("id");
        String assetValue = (String)asset.get("path");
        String assetLocation = (String)asset.get("location");
        if (assetLocation == null) {
          assetLocation = location;
        }
        Asset amdAsset;
        if (define) {
          List<AnnotationState> dependencies = (List<AnnotationState>)asset.get("dependencies");
          Map<String, String> aliases =  Collections.emptyMap();
          List<String> depends = Collections.emptyList();
          if (dependencies != null && dependencies.size() > 0) {
            for (AnnotationState dependency : dependencies) {
              String id = (String)dependency.get("id");
              String alias = (String)dependency.get("alias");
              if (depends.isEmpty()) {
                depends = new ArrayList<String>(dependencies.size());
              }
              depends.add(id);
              if (alias != null && alias.length() > 0) {
                if (aliases.isEmpty()) {
                  aliases = new HashMap<String, String>(dependencies.size());
                }
                aliases.put(id, alias);
              }
            }
          }
          String adapter = (String)asset.get("adapter");
          amdAsset = new AMDAsset(assetId, "amd", Collections.singletonList(assetValue), depends, assetLocation, adapter, aliases);
        } else {
          amdAsset = new Asset(assetId, "amd", Collections.singletonList(assetValue), Collections.<String>emptyList(), assetLocation);
        }
        assetsMetaModel.addAsset(amdAsset);
      }
    }

    if (key.getType().equals(Name.create(Defines.class))) {
      defines.put(metaModel.getHandle(), added);
    } else if (key.getType().equals(Name.create(Requires.class))) {
      requires.put(metaModel.getHandle(), added);
    }
  }

  @Override
  public void processAnnotationRemoved(ApplicationMetaModel metaModel, AnnotationKey key, AnnotationState removed) {

    if (metaModel.getHandle().equals(key.getElement())) {
      AssetsMetaModel assetsMetaModel = metaModel.getChild(AssetsMetaModel.KEY);
      boolean define = key.getType().getIdentifier().equals("Defines");
      assetsMetaModel.removeAssets(define ? "define" : "require");
    }

    if (key.getType().equals(Name.create(Defines.class))) {
      defines.remove(metaModel.getHandle());
    } else if (key.getType().equals(Name.create(Requires.class))) {
      requires.remove(metaModel.getHandle());
    }
  }

  @Override
  public void prePassivate(ApplicationMetaModel metaModel) {
    AnnotationState defineState = defines.get(metaModel.getHandle());
    AnnotationState requireState = requires.get(metaModel.getHandle());
  }

  private List<JSON> build(List<Map<String, Object>> scripts) {
    List<JSON> foo = Collections.emptyList();
    if (scripts != null && scripts.size() > 0) {
      foo = new ArrayList<JSON>(scripts.size());
      for (Map<String, Object> script : scripts) {
        JSON bar = new JSON();
        for (Map.Entry<String, Object> entry : script.entrySet()) {
          bar.set(entry.getKey(), entry.getValue());
        }
        foo.add(bar);
      }
    }
    return foo;
  }

  @Override
  public JSON getDescriptor(ApplicationMetaModel application) {
    AnnotationState definesState = defines.get(application.getHandle());
    AnnotationState requiresState = requires.get(application.getHandle());
    JSON config = null;
    if (definesState != null) {
      config = new JSON();
      JSON definesJSON = new JSON();
      List<Map<String, Object>> defines = (List<Map<String, Object>>)definesState.get("value");
      definesJSON.set("value", build(defines));
      config.set("defines", definesJSON);
    }
    if (requiresState != null) {
      if (config == null) {
        config = new JSON();
      }
      JSON requiresJSON = new JSON();
      List<Map<String, Object>> requires = (List<Map<String, Object>>)requiresState.get("value");
      requiresJSON.set("value", build(requires));
      requiresJSON.set("location", requiresState.get("location"));
      config.set("requires", requiresJSON);
    }

    if (config != null) {
      config.set("package", "assets");
    }

    return config;
  }
}
