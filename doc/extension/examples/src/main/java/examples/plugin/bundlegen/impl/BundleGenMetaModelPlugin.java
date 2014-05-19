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
package examples.plugin.bundlegen.impl;

import examples.plugin.bundlegen.BundleGen;
import juzu.impl.common.Name;
import juzu.impl.common.Path;
import juzu.impl.compiler.ElementHandle;
import juzu.impl.compiler.MessageCode;
import juzu.impl.compiler.ProcessingContext;
import juzu.impl.metamodel.AnnotationKey;
import juzu.impl.metamodel.AnnotationState;
import juzu.impl.plugin.application.metamodel.ApplicationMetaModel;
import juzu.impl.plugin.application.metamodel.ApplicationMetaModelPlugin;

import javax.lang.model.element.PackageElement;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;

/**
 * @author Julien Viet
 */
public class BundleGenMetaModelPlugin extends ApplicationMetaModelPlugin {

  // tag::BUNDLE_NOT_FOUND[]
  public static final MessageCode BUNDLE_NOT_FOUND = new MessageCode("BUNDLE_NOT_FOUND", "The bundle %1$s cannot be resolved");
  // end::BUNDLE_NOT_FOUND[]

  // tag::CANNOT_CREATE_BUNDLE[]
  public static final MessageCode CANNOT_CREATE_BUNDLE = new MessageCode("CANNOT_CREATE_BUNDLE", "The bundle %1$s cannot be created");
  // end::CANNOT_CREATE_BUNDLE[]

  /** . */
  private HashMap<ElementHandle.Package, String> bundles = new HashMap<ElementHandle.Package, String>();

  // tag::constructor[]
  public BundleGenMetaModelPlugin() {
    super("bundlegen");
  }
  // end::constructor[]

  // tag::init[]
  @Override
  public Set<Class<? extends Annotation>> init(ProcessingContext env) {
    return Collections.<Class<? extends Annotation>>singleton(BundleGen.class);
  }
  // end::init[]

  // tag::processAnnotationAdded[]
  @Override
  public void processAnnotationAdded(ApplicationMetaModel application, AnnotationKey key, AnnotationState added) {
    Name type = key.getType();
    if (type.toString().equals(BundleGen.class.getName())) {
      ElementHandle.Package pkg = (ElementHandle.Package)key.getElement();
      bundles.put(pkg, (String)added.get("value"));
    }
  }
  // end::processAnnotationAdded[]

  // tag::processAnnotationRemoved[]
  @Override
  public void processAnnotationRemoved(ApplicationMetaModel application, AnnotationKey key, AnnotationState removed) {
    Name type = key.getType();
    if (type.toString().equals(BundleGen.class.getName())) {
      ElementHandle.Package pkg = (ElementHandle.Package)key.getElement();
      bundles.remove(pkg);
    }
  }
  // end::processAnnotationRemoved[]

  // tag::prePassivate[]
  @Override
  public void prePassivate(ApplicationMetaModel application) {
    for (Map.Entry<ElementHandle.Package, String> entry : bundles.entrySet()) {
      ElementHandle.Package pkg = entry.getKey();
      String bundleName = entry.getValue();
      processBundle(application, pkg, bundleName);
    }
  }
  // end::prePassivate[]

  // tag::processBundle[]
  private void processBundle(ApplicationMetaModel application, ElementHandle.Package pkg, String bundleName) {
    ProcessingContext context = application.getProcessingContext();
    PackageElement pkgElt = context.get(pkg);
    Properties properties = loadBundle(context, pkg, bundleName);
    if (properties == null) {
      throw BUNDLE_NOT_FOUND.failure(pkgElt, bundleName);
    } else {
      try {
        generateBundleClass(context, properties, pkgElt, bundleName);
      }
      catch (IOException e) {
        throw CANNOT_CREATE_BUNDLE.failure(pkgElt, bundleName).initCause(e);
      }
    }
  }
  // end::processBundle[]

  // tag::loadBundle[]
  private Properties loadBundle(ProcessingContext context, ElementHandle.Package pkg, String bundleName) {
    Path.Absolute absolute = pkg.getPackageName().resolve(bundleName + ".properties");
    FileObject bundle = context.resolveResourceFromSourcePath(pkg, absolute);
    if (bundle != null) {
      try {
        InputStream in = bundle.openInputStream();
        Properties properties = new Properties();
        properties.load(in);
        return properties;
      }
      catch (IOException e) {
        context.log(Level.SEVERE, "Could not load resource bundle", e);
      }
    }
    return null;
  }
  // end::loadBundle[]

  // tag::generateBundleClass[]
  private void generateBundleClass(ProcessingContext context, Properties properties, PackageElement pkgElt, String bundleName) throws IOException {
    String fqn = pkgElt.getQualifiedName() + "." + bundleName;
    JavaFileObject source = context.createSourceFile(fqn, pkgElt);
    PrintWriter writer = new PrintWriter(source.openWriter());
    writer.println("package " + pkgElt.getQualifiedName() + ";");
    writer.println("import examples.plugin.bundlegen.impl.BundleResolver;");
    writer.println("public class " + bundleName + " {");
    for (String key : properties.stringPropertyNames()) {
      writer.println("public static String " + key + "() {");
      writer.println("return BundleResolver.resolve(" + bundleName + ".class, \"" + key + "\");");
      writer.println("}");
    }
    writer.println("}");
    writer.close();
  }
  // end::generateBundleClass[]
}
