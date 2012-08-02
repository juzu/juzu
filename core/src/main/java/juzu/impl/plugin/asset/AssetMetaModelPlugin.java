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

package juzu.impl.plugin.asset;

import juzu.impl.plugin.application.metamodel.ApplicationMetaModel;
import juzu.impl.plugin.application.metamodel.ApplicationMetaModelPlugin;
import juzu.impl.common.FQN;
import juzu.impl.metamodel.AnnotationKey;
import juzu.impl.metamodel.AnnotationState;
import juzu.impl.compiler.ElementHandle;
import juzu.impl.common.JSON;
import juzu.impl.compiler.ProcessingContext;
import juzu.plugin.asset.Assets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class AssetMetaModelPlugin extends ApplicationMetaModelPlugin {

  /** . */
  private final HashMap<ElementHandle.Package, JSON> enabledMap = new HashMap<ElementHandle.Package, JSON>();

  /** . */
  private static final FQN ASSETS = new FQN(Assets.class);

  public AssetMetaModelPlugin() {
    super("asset");
  }

  @Override
  public Set<Class<? extends java.lang.annotation.Annotation>> init(ProcessingContext env) {
    return Collections.<Class<? extends java.lang.annotation.Annotation>>singleton(Assets.class);
  }

  @Override
  public void processAnnotationAdded(ApplicationMetaModel metaModel, AnnotationKey key, AnnotationState added) {
    if (key.getType().equals(ASSETS)) {
      ElementHandle.Package handle = metaModel.getHandle();
      JSON json = new JSON();
      json.set("scripts", build((List<Map<String, Object>>)added.get("scripts")));
      json.set("stylesheets", build((List<Map<String, Object>>)added.get("stylesheets")));
      json.set("package", metaModel.getName().append("assets").getValue());
      enabledMap.put(handle, json);
    }
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
  public void destroy(ApplicationMetaModel application) {
    enabledMap.remove(application.getHandle());
  }

  @Override
  public JSON getDescriptor(ApplicationMetaModel application) {
    ElementHandle.Package handle = application.getHandle();
    return enabledMap.get(handle);
  }
}
