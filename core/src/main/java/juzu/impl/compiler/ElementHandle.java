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

package juzu.impl.compiler;

import juzu.impl.common.FQN;
import juzu.impl.common.MethodHandle;
import juzu.impl.common.QN;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class ElementHandle<E extends Element> implements Serializable {

  public static ElementHandle<?> create(Element elt) {
    ElementKind kind = elt.getKind();
    switch (kind) {
      case FIELD: {
        VariableElement variableElt = (VariableElement)elt;
        return Field.create(variableElt);
      }
      case CLASS: {
        TypeElement typeElt = (TypeElement)elt;
        return Class.create(typeElt);
      }
      case PACKAGE: {
        PackageElement packageElt = (PackageElement)elt;
        return Package.create(packageElt);
      }
      case METHOD: {
        ExecutableElement packageElt = (ExecutableElement)elt;
        return Method.create(packageElt);
      }
      default:
        throw new UnsupportedOperationException("Element " + elt + " with kind " + kind + " not supported");
    }
  }

  public final E get(ProcessingEnvironment env) {
    try {
      return doGet(env);
    }
    catch (RuntimeException e) {
      if (e.getClass().getName().equals("org.eclipse.jdt.internal.compiler.problem.AbortCompilation")) {
        // In case of eclipse we catch it and return null instead
        return null;
      }
      else {
        // Rethrow
        throw e;
      }
    }
  }

  protected abstract E doGet(ProcessingEnvironment env);

  public abstract QN getPackage();

  public abstract boolean equals(Object obj);

  public abstract int hashCode();

  public abstract String toString();

  public static class Package extends ElementHandle<PackageElement> {

    public static Package create(QN packageName) {
      return new Package(packageName);
    }

    public static Package create(PackageElement elt) {
      return new Package(QN.parse(elt.getQualifiedName()));
    }

    /** . */
    private final QN qn;

    private Package(QN qn) {
      this.qn = qn;
    }

    public QN getQN() {
      return qn;
    }

    @Override
    public QN getPackage() {
      return qn;
    }

    @Override
    protected PackageElement doGet(ProcessingEnvironment env) {
      return env.getElementUtils().getPackageElement(qn);
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this) {
        return true;
      }
      else if (obj instanceof Package) {
        Package that = (Package)obj;
        return qn.equals(that.qn);
      }
      return false;
    }

    @Override
    public int hashCode() {
      return qn.hashCode();
    }

    @Override
    public String toString() {
      return "ElementHandle.Package[qn=" + qn + "]";
    }
  }

  public static class Class extends ElementHandle<TypeElement> {

    public static Class create(FQN fqn) {
      return new Class(fqn);
    }

    public static Class create(TypeElement elt) {
      return new Class(new FQN(elt.getQualifiedName().toString()));
    }

    /** . */
    private final FQN fqn;

    private Class(FQN fqn) {
      this.fqn = fqn;
    }

    public FQN getFQN() {
      return fqn;
    }

    @Override
    public QN getPackage() {
      return fqn.getPackageName();
    }

    @Override
    protected TypeElement doGet(ProcessingEnvironment env) {
      return env.getElementUtils().getTypeElement(fqn.getName());
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this) {
        return true;
      }
      else if (obj instanceof Class) {
        Class that = (Class)obj;
        return fqn.equals(that.fqn);
      }
      return false;
    }

    @Override
    public int hashCode() {
      return fqn.hashCode();
    }

    @Override
    public String toString() {
      return "ElementHandle.Class[fqn=" + fqn + "]";
    }
  }

  public static class Method extends ElementHandle<ExecutableElement> {

    public static Method create(FQN fqn, String name, Collection<String> parameterTypes) {
      return new Method(fqn, name, new ArrayList<String>(parameterTypes));
    }

    public static Method create(ExecutableElement elt) {
      TypeElement typeElt = (TypeElement)elt.getEnclosingElement();
      String name = elt.getSimpleName().toString();
      FQN fqn = new FQN(typeElt.getQualifiedName().toString());
      ArrayList<String> parameterTypes = new ArrayList<String>();
      for (TypeMirror parameterType : ((ExecutableType)elt.asType()).getParameterTypes()) {
        parameterTypes.add(parameterType.toString());
      }

      return new Method(fqn, name, parameterTypes);
    }

    private Method(FQN fqn, String name, ArrayList<String> parameterTypes) {
      this.fqn = fqn;
      this.name = name;
      this.parameterTypes = parameterTypes;
    }

    /** . */
    private final FQN fqn;

    /** . */
    private final String name;

    /** . */
    private final ArrayList<String> parameterTypes;

    public FQN getFQN() {
      return fqn;
    }

    public String getName() {
      return name;
    }

    public List<String> getParameterTypes() {
      return parameterTypes;
    }

    @Override
    public QN getPackage() {
      return fqn.getPackageName();
    }

    @Override
    protected ExecutableElement doGet(ProcessingEnvironment env) {
      TypeElement typeElt = env.getElementUtils().getTypeElement(fqn.getName());
      if (typeElt != null) {
        next:
        for (ExecutableElement executableElement : ElementFilter.methodsIn(typeElt.getEnclosedElements())) {
          if (executableElement.getSimpleName().toString().equals(name)) {
            List<? extends TypeMirror> parameterTypes = ((ExecutableType)executableElement.asType()).getParameterTypes();
            int len = parameterTypes.size();
            if (len == this.parameterTypes.size()) {
              for (int i = 0;i < len;i++) {
                if (!parameterTypes.get(i).toString().equals(this.parameterTypes.get(i))) {
                  continue next;
                }
              }
              return executableElement;
            }
          }
        }
      }
      return null;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this) {
        return true;
      }
      else if (obj instanceof Method) {
        Method that = (Method)obj;
        return fqn.equals(that.fqn) && name.equals(that.name) && parameterTypes.equals(that.parameterTypes);
      }
      return false;
    }

    @Override
    public int hashCode() {
      int hashCode = fqn.hashCode() ^ name.hashCode();
      for (String parameterType : parameterTypes) {
        hashCode = hashCode * 41 + parameterType.hashCode();
      }
      return hashCode;
    }

    public MethodHandle getMethodHandle() {
      return new MethodHandle(fqn.getName(), name, parameterTypes.toArray(new String[parameterTypes.size()]));
    }

    @Override
    public String toString() {
      return "ElementHandle.Method[fqn=" + fqn + ",name=" + name + ",parameterTypes" + parameterTypes + "]";
    }
  }

  public static class Field extends ElementHandle<VariableElement> {

    public static Field create(VariableElement elt) {
      TypeElement typeElt = (TypeElement)elt.getEnclosingElement();
      String name = elt.getSimpleName().toString();
      FQN fqn = new FQN(typeElt.getQualifiedName().toString());
      return new Field(fqn, name);
    }

    public static Field create(String fqn, String name) {
      return new Field(new FQN(fqn), name);
    }

    public static Field create(FQN fqn, String name) {
      return new Field(fqn, name);
    }

    /** . */
    private final FQN fqn;

    /** . */
    private final String name;

    private Field(FQN fqn, String name) {
      this.fqn = fqn;
      this.name = name;
    }

    public FQN getFQN() {
      return fqn;
    }

    public String getName() {
      return name;
    }

    @Override
    public QN getPackage() {
      return fqn.getPackageName();
    }

    @Override
    protected VariableElement doGet(ProcessingEnvironment env) {
      TypeElement typeElt = env.getElementUtils().getTypeElement(fqn.getName());
      if (typeElt != null) {
        for (VariableElement variableElt : ElementFilter.fieldsIn(typeElt.getEnclosedElements())) {
          if (variableElt.getSimpleName().contentEquals(name)) {
            return variableElt;
          }
        }
      }
      return null;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this) {
        return true;
      }
      else if (obj instanceof Field) {
        Field that = (Field)obj;
        return fqn.equals(that.fqn) && name.equals(that.name);
      }
      return false;
    }

    @Override
    public int hashCode() {
      return fqn.hashCode() ^ name.hashCode();
    }

    @Override
    public String toString() {
      return "ElementHandle.Field[fqn=" + fqn + ",name=" + name + "]";
    }
  }
}
