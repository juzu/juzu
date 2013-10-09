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
    if (key.getElement().getPackageName().equals(pkg.getPackageName())) {
      servlets.put(pkg, added);
    }
  }

  @Override
  public void processAnnotationRemoved(ApplicationMetaModel metaModel, AnnotationKey key, AnnotationState removed) {
    ElementHandle.Package pkg = metaModel.getHandle();
    if (key.getElement().getPackageName().equals(pkg.getPackageName())) {
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
      String resourceBundle = (String)annotation.get("resourceBundle");
      if (simpleName == null) {
        simpleName = metaModel.getBaseName() + "Servlet";
      }
      Name clazz = pkg.getPackageName().append(simpleName);
      Writer writer = null;
      try {
        JavaFileObject file = metaModel.processingContext.createSourceFile(clazz, pkgElt);
        writer = file.openWriter();
        writer.append("package ").append(pkg.getPackageName()).append(";\n");
        writer.append("import javax.servlet.annotation.WebServlet;\n");
        writer.append("import javax.servlet.annotation.WebInitParam;\n");
        writer.append("@WebServlet(name=\"").append(simpleName).append("\",urlPatterns=\"").append(urlPattern).append("\"");
        if (resourceBundle != null) {
          writer.append(",initParams=@WebInitParam(name=\"juzu.resource_bundle\",value=\"").append(resourceBundle).append("\")");
        }
        writer.append(")\n");
        writer.append("public class " ).append(simpleName).append(" extends juzu.bridge.servlet.JuzuServlet {\n");
        writer.append("@Override\n");
        writer.append("protected String getApplicationName(javax.servlet.ServletConfig config) {\n");
        writer.append("return \"").append(pkg.getPackageName()).append("\";\n");
        writer.append("}\n");
        writer.append("}\n");
      }
      catch (IOException e) {
        throw CANNOT_WRITE_SERVLET_CLASS.failure(e, pkgElt, pkg.getPackageName());
      }
      finally {
        Tools.safeClose(writer);
      }
    }
  }
}
