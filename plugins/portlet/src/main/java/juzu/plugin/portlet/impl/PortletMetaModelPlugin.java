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

package juzu.plugin.portlet.impl;

import juzu.impl.common.Name;
import juzu.impl.plugin.application.metamodel.ApplicationMetaModel;
import juzu.impl.plugin.application.metamodel.ApplicationMetaModelPlugin;
import juzu.impl.metamodel.AnnotationKey;
import juzu.impl.metamodel.AnnotationState;
import juzu.impl.compiler.ProcessingException;
import juzu.impl.compiler.ElementHandle;
import juzu.impl.compiler.MessageCode;
import juzu.impl.compiler.ProcessingContext;
import juzu.impl.common.JSON;
import juzu.impl.common.Tools;
import juzu.plugin.portlet.Portlet;

import javax.annotation.Generated;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.PackageElement;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class PortletMetaModelPlugin extends ApplicationMetaModelPlugin {

  /** . */
  public static final MessageCode CANNOT_WRITE_PORTLET_CLASS = new MessageCode("CANNOT_WRITE_PORTLET_CLASS", "The portlet class %1$s cannot be written");

  /** . */
  private final HashMap<ElementHandle.Package, String[]> enabledMap = new HashMap<ElementHandle.Package, String[]>();

  /** . */
  private HashSet<ElementHandle.Package> toEmit = new HashSet<ElementHandle.Package>();

  public PortletMetaModelPlugin() {
    super("portlet");
  }

  @Override
  public Set<Class<? extends java.lang.annotation.Annotation>> init(ProcessingContext env) {
    return Collections.<Class<? extends java.lang.annotation.Annotation>>singleton(Portlet.class);
  }

  @Override
  public void processAnnotationAdded(ApplicationMetaModel metaModel, AnnotationKey key, AnnotationState added) {
    ElementHandle.Package pkg = metaModel.getHandle();
    if (key.getElement().getPackage().equals(pkg.getPackage())) {
      String name = (String)added.get("name");
      if (name == null) {
        name = metaModel.getBaseName() + "Portlet";
      }
      enabledMap.put(pkg, new String[]{name, metaModel.getName().toString()});
      toEmit.add(pkg);
    }
  }

  @Override
  public void destroy(ApplicationMetaModel application) {
    enabledMap.remove(application.getHandle());
  }

  @Override
  public void postProcessEvents(ApplicationMetaModel application) {
    // Do GC
    ElementHandle.Package pkg = application.getHandle();
    String[] names = enabledMap.get(pkg);
    if (names != null) {
      PackageElement pkgElt = application.model.processingContext.get(pkg);
      AnnotationMirror am = Tools.getAnnotation(pkgElt, Portlet.class.getName());
      if (am == null) {
        enabledMap.remove(pkg);
        toEmit.remove(pkg);
      }
      else {
        if (toEmit.contains(pkg)) {
          toEmit.remove(pkg);
          emitPortlet(application.model.processingContext, pkgElt, names);
        }
      }
    }
  }

  private void emitPortlet(
    ProcessingContext env,
    PackageElement pkgElt,
    String[] names) throws ProcessingException {
    Writer writer = null;
    Name fqn = Name.parse(pkgElt.getQualifiedName()).append(names[0]);
    try {
      JavaFileObject file = env.createSourceFile(fqn, pkgElt);
      writer = file.openWriter();

      //
      writer.append("package ").append(pkgElt.getQualifiedName()).append(";\n");
      writer.append("import ").append(Generated.class.getCanonicalName()).append(";\n");
      writer.append("@Generated(value={})\n");
      writer.append("public class ").append(names[0]).append(" extends juzu.bridge.portlet.JuzuPortlet {\n");
      writer.append("@Override\n");
      writer.append("protected String getApplicationName(javax.portlet.PortletConfig config) {\n");
      writer.append("return \"").append(names[1]).append("\";\n");
      writer.append("}\n");
      writer.append("}\n");
    }
    catch (IOException e) {
      throw CANNOT_WRITE_PORTLET_CLASS.failure(e, pkgElt, fqn);
    }
    finally {
      Tools.safeClose(writer);
    }
  }

  @Override
  public JSON getDescriptor(ApplicationMetaModel application) {
    return new JSON();
  }
}
