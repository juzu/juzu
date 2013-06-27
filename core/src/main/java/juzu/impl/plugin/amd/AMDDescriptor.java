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

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;

import juzu.Scope;
import juzu.impl.asset.amd.AMDMetaData;
import juzu.impl.asset.amd.AMDScriptManager;
import juzu.impl.common.NameLiteral;
import juzu.impl.common.Tools;
import juzu.impl.inject.BeanDescriptor;
import juzu.impl.plugin.PluginDescriptor;
import juzu.impl.resource.ResourceResolver;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 *
 */
public class AMDDescriptor extends PluginDescriptor {
  
  private final List<AMDMetaData> defines;
  
  private final List<AMDMetaData> requires;
  
  public AMDDescriptor(List<AMDMetaData> defines, List<AMDMetaData> requires) {
    this.defines = defines;
    this.requires = requires;
  }
  
  public List<AMDMetaData> getDefines() {
    return defines;
  }
  
  public List<AMDMetaData> getRequires() {
    return requires;
  }
  
  @Override
  public Iterable<BeanDescriptor> getBeans() {
    return Tools.list(
      BeanDescriptor.createFromBean(
        AMDScriptManager.class,
        Scope.SINGLETON,
        Collections.<Annotation>singletonList(new NameLiteral("juzu.asset_manager.amd"))),
        BeanDescriptor.createFromImpl(ResourceResolver.class, Scope.SINGLETON, null, AMDResolver.class));
  }
}
