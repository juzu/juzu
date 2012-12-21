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

import juzu.Scope;
import juzu.impl.asset.AssetManager;
import juzu.impl.asset.AssetMetaData;
import juzu.impl.common.NameLiteral;
import juzu.impl.common.Tools;
import juzu.impl.inject.BeanDescriptor;
import juzu.impl.metadata.Descriptor;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class AssetDescriptor extends Descriptor {

  /** . */
  private List<AssetMetaData> scripts;

  /** . */
  private List<AssetMetaData> stylesheets;

  public AssetDescriptor(List<AssetMetaData> scripts, List<AssetMetaData> stylesheets) {
    this.scripts = scripts;
    this.stylesheets = stylesheets;
  }

  public List<AssetMetaData> getScripts() {
    return scripts;
  }

  public List<AssetMetaData> getStylesheets() {
    return stylesheets;
  }

  @Override
  public Iterable<BeanDescriptor> getBeans() {
    return Tools.list(
        BeanDescriptor.createFromBean(
            AssetManager.class,
            Scope.SINGLETON,
            Collections.<Annotation>singletonList(new NameLiteral("juzu.asset_manager.script"))),
        BeanDescriptor.createFromBean(
            AssetManager.class,
            Scope.SINGLETON,
            Collections.<Annotation>singletonList(new NameLiteral("juzu.asset_manager.stylesheet"))));
  }
}
