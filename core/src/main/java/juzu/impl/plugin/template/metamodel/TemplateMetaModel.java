/*
 * Copyright 2013 eXo Platform SAS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package juzu.impl.plugin.template.metamodel;

import juzu.impl.common.Name;
import juzu.impl.compiler.ElementHandle;
import juzu.impl.compiler.MessageCode;
import juzu.impl.metamodel.Key;
import juzu.impl.metamodel.MetaModelEvent;
import juzu.impl.metamodel.MetaModelObject;
import juzu.impl.common.JSON;
import juzu.impl.common.Path;
import juzu.impl.template.spi.Template;

import javax.lang.model.element.Element;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A template.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class TemplateMetaModel extends TemplateRefMetaModel {

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
  public static final MessageCode TEMPLATE_CYCLE = new MessageCode("TEMPLATE_CYCLE", "Detected a template cycle when registering %1$ conflicting with %2$");

  /** . */
  public static final MessageCode CONTROLLER_NOT_RESOLVED = new MessageCode("CONTROLLER_NOT_RESOLVED",
      "Controller %1$s not found in template %2$s at (%3$s,%4$s)");

  /** . */
  public static final MessageCode UNKNOWN_TAG = new MessageCode("UNKNOWN_TAG", "Tag %1$ does not exists");

  /** . */
  public final static Key<TemplateMetaModel> KEY = Key.of(TemplateMetaModel.class);

  /** The related application. */
  AbstractContainerMetaModel templates;

  /** . */
  final Path.Absolute path;

  /** . */
  int refCount;

  /** The related template. */
  Template<?> template;

  public TemplateMetaModel(Path.Absolute path) {
    this.path = path;
    this.template = null;
    this.refCount = 0;
  }

  public AbstractContainerMetaModel getTemplates() {
    return templates;
  }

  /**
   * Compute and return the {@link Element} referencing this template.
   *
   * @return the elements referencing this template
   */
  public Element[] getReferencingElements() {
    Set<Name> types = new LinkedHashSet<Name>();
    for (ElementMetaModel ref : getElementReferences()) {
      ElementHandle.Field handle = ref.getElement();
      types.add(handle.getFQN());
    }
    final Element[] elements = new Element[types.size()];
    int index = 0;
    for (Name type : types) {
      elements[index++] = templates.application.getProcessingContext().getTypeElement(type);
    }
    return elements;
  }

  /**
   * Compute the element references to this template.
   *
   * @return the metamodel elements referencing this template
   */
  public Collection<ElementMetaModel> getElementReferences() {
    Collection<TemplateRefMetaModel> refs = getReferences();
    for (Iterator<TemplateRefMetaModel> i = refs.iterator();i.hasNext();) {
      if (i.next() instanceof TemplateMetaModel) {
        i.remove();
      }
    }
    // Safe cast as the collection now only contains instances of ElementTemplateRefMetaModel
    return (Collection)refs;
  }

  /**
   * Compute the references to this template.
   *
   * @return the ancestors
   */
  public Collection<TemplateRefMetaModel> getReferences() {
    HashSet<TemplateRefMetaModel> refs = new HashSet<TemplateRefMetaModel>();
    buildReferences(refs);
    return refs;
  }

  private void buildReferences(HashSet<TemplateRefMetaModel> refs) {
    for (MetaModelObject parent : getParents()) {
      if (parent instanceof TemplateRefMetaModel) {
        TemplateRefMetaModel parentRef = (TemplateRefMetaModel)parent;
        if (!refs.contains(parentRef)) {
          refs.add(parentRef);
          if (parentRef instanceof TemplateMetaModel) {
            TemplateMetaModel parentTemplate = (TemplateMetaModel)parentRef;
            parentTemplate.buildReferences(refs);
          }
        }
      }
    }
  }

  public Path.Absolute getPath() {
    return path;
  }

  public JSON toJSON() {
    JSON json = new JSON();
    json.set("path", path.getCanonical());
    return json;
  }

  @Override
  protected void postAttach(MetaModelObject parent) {
    if (parent instanceof AbstractContainerMetaModel) {
      queue(MetaModelEvent.createAdded(this));
      this.templates = (AbstractContainerMetaModel)parent;
    }
    else if (parent instanceof TemplateRefMetaModel) {
      refCount++;
    }
  }

  @Override
  protected void preDetach(MetaModelObject parent) {
    if (parent instanceof AbstractContainerMetaModel) {
      ElementHandle.Package handle = templates.application.getHandle();
      this.templates = null;
      this.template = null;
      queue(MetaModelEvent.createRemoved(this, handle));
    }
    else if (parent instanceof TemplateRefMetaModel) {
      refCount--;
    }
  }
}
