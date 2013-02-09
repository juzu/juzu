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

package juzu.plugin.servlet.impl;

import juzu.impl.common.Name;
import juzu.impl.common.Tools;
import juzu.impl.compiler.ElementHandle;
import juzu.impl.compiler.MessageCode;
import juzu.impl.compiler.ProcessingContext;
import juzu.impl.metamodel.AnnotationKey;
import juzu.impl.metamodel.AnnotationState;
import juzu.impl.plugin.application.metamodel.ApplicationMetaModel;
import juzu.impl.plugin.application.metamodel.ApplicationMetaModelPlugin;
import juzu.plugin.servlet.Servlet;

import javax.lang.model.element.PackageElement;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ServletMetaModelPlugin extends ApplicationMetaModelPlugin {

  /** . */
  public static final MessageCode CANNOT_WRITE_SERVLET_CLASS = new MessageCode("CANNOT_WRITE_SERVLET_CLASS", "The servlet class %1$s cannot be written");

  /** . */
  private final HashMap<ElementHandle.Package, AnnotationState> servlets = new HashMap<ElementHandle.Package, AnnotationState>();

  public ServletMetaModelPlugin() {
    super("servlet");
  }

  @Override
  public Set<Class<? extends java.lang.annotation.Annotation>> init(ProcessingContext env) {
    return Collections.<Class<? extends java.lang.annotation.Annotation>>singleton(Servlet.class);
  }

  @Override
  public void processAnnotationAdded(ApplicationMetaModel metaModel, AnnotationKey key, AnnotationState added) {
    ElementHandle.Package pkg = metaModel.getHandle();
    if (key.getElement().getPackage().equals(pkg.getPackage())) {
      servlets.put(pkg, added);
    }
  }

  @Override
  public void processAnnotationRemoved(ApplicationMetaModel metaModel, AnnotationKey key, AnnotationState removed) {
    ElementHandle.Package pkg = metaModel.getHandle();
    if (key.getElement().getPackage().equals(pkg.getPackage())) {
      servlets.remove(pkg);
    }
  }

  @Override
  public void postProcessAnnotations(ApplicationMetaModel metaModel) {
    ElementHandle.Package pkg = metaModel.getHandle();
    AnnotationState annotation = servlets.remove(pkg);
    if (annotation != null) {
      PackageElement pkgElt = metaModel.processingContext.get(pkg);
      String urlPattern = (String)annotation.get("value");
      String simpleName = (String)annotation.get("name");
      if (simpleName == null) {
        simpleName = metaModel.getBaseName() + "Servlet";
      }
      Name clazz = pkg.getPackage().append(simpleName);
      Writer writer = null;
      try {
        JavaFileObject file = metaModel.processingContext.createSourceFile(clazz, pkgElt);
        writer = file.openWriter();
        writer.append("package ").append(pkg.getPackage()).append(";\n");
        writer.append("import javax.servlet.annotation.WebServlet;\n");
        writer.append("import javax.servlet.annotation.WebInitParam;\n");
        writer.append("@WebServlet(name=\"").append(simpleName).append("\",urlPatterns=\"").append(urlPattern).append("\")\n");
        writer.append("public class " ).append(simpleName).append(" extends juzu.bridge.servlet.JuzuServlet {\n");
        writer.append("@Override\n");
        writer.append("protected String getApplicationName(javax.servlet.ServletConfig config) {\n");
        writer.append("return \"").append(pkg.getPackage()).append("\";\n");
        writer.append("}\n");
        writer.append("}\n");
      }
      catch (IOException e) {
        throw CANNOT_WRITE_SERVLET_CLASS.failure(e, pkgElt, pkg.getPackage());
      }
      finally {
        Tools.safeClose(writer);
      }
    }
  }
}
