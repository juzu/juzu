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

package juzu.impl.plugin.template.metamodel;

import juzu.impl.plugin.application.metamodel.ApplicationMetaModel;
import juzu.impl.compiler.ElementHandle;
import juzu.impl.metamodel.Key;
import juzu.impl.metamodel.MetaModelObject;
import juzu.impl.common.JSON;
import juzu.impl.common.Path;
import juzu.impl.common.QN;

import java.util.Iterator;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class TemplatesMetaModel extends MetaModelObject implements Iterable<TemplateMetaModel> {

  /** . */
  public final static Key<TemplatesMetaModel> KEY = Key.of(TemplatesMetaModel.class);

  /** . */
  ApplicationMetaModel application;

  /** . */
  private QN qn;

  /** . */
  TemplateResolver resolver;

  /** . */
  TemplateMetaModelPlugin plugin;

  @Override
  public JSON toJSON() {
    JSON json = new JSON();
    json.map("values", getChildren(TemplateMetaModel.class));
    json.set("qn", qn);
    return json;
  }

  public Path.Absolute resolve(Path path) {
    if (path instanceof Path.Absolute) {
      return (Path.Absolute)path;
    }
    else {
      return Path.Absolute.create(qn.append(path.getQN()), path.getRawName(), path.getExt());
    }
  }

  public ApplicationMetaModel getApplication() {
    return application;
  }

  public QN getQN() {
    return qn;
  }

  public TemplateMetaModel get(Path path) {
    return getChild(Key.of(path, TemplateMetaModel.class));
  }

  public Iterator<TemplateMetaModel> iterator() {
    return getChildren(TemplateMetaModel.class).iterator();
  }

  public void remove(ElementHandle.Field handle) {
    Key<TemplateRefMetaModel> key = Key.of(handle, TemplateRefMetaModel.class);
    TemplateRefMetaModel ref = getChild(key);
    TemplateMetaModel template = ref.getChild(TemplateMetaModel.KEY);
    removeChild(key);
    if (template.refCount == 0) {
      template.remove();
    }
  }

  public TemplateRefMetaModel add(ElementHandle.Field handle, Path path) {
    TemplateRefMetaModel ref = addChild(Key.of(handle, TemplateRefMetaModel.class), new TemplateRefMetaModel(handle, path));
    TemplateMetaModel template = getChild(Key.of(path, TemplateMetaModel.class));
    if (template == null) {
      template = addChild(Key.of(path, TemplateMetaModel.class), new TemplateMetaModel(path));
    }
    ref.addChild(TemplateMetaModel.KEY, template);
    return ref;
  }

  public void remove(TemplateMetaModel template) {
    if (template.templates != this) {
      throw new IllegalArgumentException();
    }
    removeChild(Key.of(template.path, TemplateMetaModel.class));
  }

  @Override
  protected void postAttach(MetaModelObject parent) {
    if (parent instanceof ApplicationMetaModel) {
      this.application = (ApplicationMetaModel)parent;
      this.qn = application.getName().append("templates");
      this.resolver = new TemplateResolver(application);
    }
  }
}
