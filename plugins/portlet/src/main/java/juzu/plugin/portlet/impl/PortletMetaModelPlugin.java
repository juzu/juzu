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

package juzu.plugin.portlet.impl;

import juzu.impl.plugin.application.metamodel.ApplicationMetaModel;
import juzu.impl.plugin.application.metamodel.ApplicationMetaModelPlugin;
import juzu.impl.metamodel.AnnotationKey;
import juzu.impl.metamodel.AnnotationState;
import juzu.impl.compiler.ProcessingException;
import juzu.impl.compiler.ElementHandle;
import juzu.impl.compiler.MessageCode;
import juzu.impl.compiler.ProcessingContext;
import juzu.impl.common.FQN;
import juzu.impl.common.JSON;
import juzu.impl.common.Tools;
import juzu.plugin.portlet.Portlet;
import juzu.portlet.JuzuPortlet;

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
  private static final FQN PORTLET = new FQN(Portlet.class);

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
    if (key.getType().equals(PORTLET) && key.getElement().getPackage().equals(pkg.getQN())) {
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
      PackageElement pkgElt = application.model.env.get(pkg);
      AnnotationMirror am = Tools.getAnnotation(pkgElt, Portlet.class.getName());
      if (am == null) {
        enabledMap.remove(pkg);
        toEmit.remove(pkg);
      }
      else {
        if (toEmit.contains(pkg)) {
          toEmit.remove(pkg);
          emitPortlet(application.model.env, pkgElt, names);
        }
      }
    }
  }

  private void emitPortlet(
    ProcessingContext env,
    PackageElement pkgElt,
    String[] names) throws ProcessingException {
    Writer writer = null;
    FQN fqn = new FQN(pkgElt.getQualifiedName(), names[0]);
    try {
      JavaFileObject file = env.createSourceFile(fqn, pkgElt);
      writer = file.openWriter();

      //
      writer.append("package ").append(pkgElt.getQualifiedName()).append(";\n");
      writer.append("import ").append(Tools.getImport(Generated.class)).append(";\n");
      writer.append("@Generated(value={})\n");
      writer.append("public class ").append(names[0]).append(" extends ").append(JuzuPortlet.class.getName()).append(" {\n");
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
