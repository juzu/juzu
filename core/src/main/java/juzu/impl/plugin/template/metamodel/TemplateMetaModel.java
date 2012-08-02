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

import juzu.impl.compiler.MessageCode;
import juzu.impl.metamodel.Key;
import juzu.impl.metamodel.MetaModelEvent;
import juzu.impl.metamodel.MetaModelObject;
import juzu.impl.common.JSON;
import juzu.impl.common.Path;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A template.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class TemplateMetaModel extends MetaModelObject {

  /** . */
  public static final MessageCode CANNOT_WRITE_TEMPLATE_STUB = new MessageCode("CANNOT_WRITE_TEMPLATE_STUB", "The template stub %1$s cannot be written");

  /** . */
  public static final MessageCode CANNOT_WRITE_TEMPLATE_CLASS = new MessageCode("CANNOT_WRITE_TEMPLATE_CLASS", "The template class %1$s cannot be written");

  /** . */
  public static final MessageCode CANNOT_WRITE_APPLICATION = new MessageCode("CANNOT_WRITE_APPLICATION", "The application %1$s cannot be written");

  /** . */
  public static final MessageCode TEMPLATE_NOT_RESOLVED = new MessageCode("TEMPLATE_NOT_RESOLVED", "The template %1$s cannot be resolved");

  /** . */
  public static final MessageCode TEMPLATE_SYNTAX_ERROR = new MessageCode("TEMPLATE_SYNTAX_ERROR", "Template syntax error");

  /** . */
  public static final MessageCode TEMPLATE_VALIDATION_ERROR = new MessageCode("TEMPLATE_VALIDATION_ERROR", "Template validation error");

  /** . */
  public static final MessageCode TEMPLATE_ILLEGAL_PATH = new MessageCode("TEMPLATE_ILLEGAL_PATH", "The reference to the template %1$s is malformed");

  /** . */
  public static final MessageCode CANNOT_WRITE_TEMPLATE_SCRIPT = new MessageCode("CANNOT_WRITE_TEMPLATE_SCRIPT", "The template script %1$s cannot be written");

  /** . */
  public final static Key<TemplateMetaModel> KEY = Key.of(TemplateMetaModel.class);

  /** The related application. */
  TemplatesMetaModel templates;

  /** . */
  final Path path;

  public TemplateMetaModel(Path path) {
    this.path = path;
  }

  public TemplatesMetaModel getTemplates() {
    return templates;
  }

  public Path getPath() {
    return path;
  }

  public JSON toJSON() {
    JSON json = new JSON();
    json.set("path", path.getCanonical());
    json.map("refs", getKeys(TemplateRefMetaModel.class));
    return json;
  }

  public Collection<TemplateRefMetaModel> getRefs() {
    ArrayList<TemplateRefMetaModel> refs = new ArrayList<TemplateRefMetaModel>();
    for (MetaModelObject parent : getParents()) {
      if (parent instanceof TemplateRefMetaModel) {
        refs.add((TemplateRefMetaModel)parent);
      }
    }
    return refs;
  }

  /** . */
  int refCount = 0;

  @Override
  protected void postAttach(MetaModelObject parent) {
    if (parent instanceof TemplatesMetaModel) {
      queue(MetaModelEvent.createAdded(this));
      this.templates = (TemplatesMetaModel)parent;
    }
    else if (parent instanceof TemplateRefMetaModel) {
      refCount++;
    }
  }

  @Override
  protected void preDetach(MetaModelObject parent) {
    if (parent instanceof TemplatesMetaModel) {
      templates.resolver.removeTemplate(path);
      queue(MetaModelEvent.createRemoved(this, templates.application.getHandle()));
      this.templates = null;
    }
    else if (parent instanceof TemplateRefMetaModel) {
      refCount--;
    }
  }
}
