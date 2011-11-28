/*
 * Copyright (C) 2011 eXo Platform SAS.
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

package org.juzu.impl.model.processor;

import org.juzu.impl.compiler.BaseProcessor;
import org.juzu.impl.compiler.ElementHandle;
import org.juzu.impl.utils.Content;
import org.juzu.impl.utils.FQN;
import org.juzu.impl.utils.Hash;
import org.juzu.impl.utils.Logger;
import org.juzu.impl.utils.Tools;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.NullType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.Elements;
import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class ProcessingContext implements Filer, Elements
{

   /**
    * We need two locations as the {@link javax.tools.StandardLocation#SOURCE_PATH} is not supported in eclipse ide
    * (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=341298), however the {@link javax.tools.StandardLocation#CLASS_OUTPUT}
    * seems to work fairly well.
    */
   private static final StandardLocation[] RESOURCE_LOCATIONS = { StandardLocation.SOURCE_PATH, StandardLocation.CLASS_OUTPUT};

   /** . */
   private ProcessingEnvironment env;

   /** . */
   private static final Logger log = BaseProcessor.getLogger(ProcessingContext.class);
   
   public ProcessingContext(ProcessingEnvironment env)
   {
      this.env = env;
   }

   // Various stuff ****************************************************************************************************

   public <E extends Element> E get(ElementHandle<E> handle)
   {
      return handle.get(env);
   }

   public long getClassHash(FQN fqn)
   {
      return getClassHash(fqn.getFullName());
   }

   public long getClassHash(String className)
   {
      TypeElement element = getTypeElement(className);
      if (element != null)
      {
         return getClassHash(element);
      }
      return 0;
   }

   public long getClassHash(TypeElement element)
   {
      Hash p = element.getAnnotation(Hash.class);
      if (p != null)
      {
         return p.value();
      }
      return 0;
   }

   /**
    * @param handle the class handle
    * @return the last modified
    * @see #getSourceHash(javax.lang.model.element.TypeElement)
    */
   public long getSourceHash(ElementHandle.Class handle)
   {
      TypeElement element = handle.get(env);
      if (element != null)
      {
         return getSourceHash(element);
      }
      return 0;
   }

   /**
    * Compute the hash of the specified type
    *
    * @param handle the class handle
    * @return the hash value
    */
   public long getSourceHash(TypeElement handle)
   {
      return Tools.handle(handle);
   }

   public Content resolveResource(FQN fqn, String extension)
   {
      for (StandardLocation location : RESOURCE_LOCATIONS)
      {
         String pkg = fqn.getPackageName().getValue();
         String relativeName = fqn.getSimpleName() + "." + extension;
         try
         {
            log.log("Attempt to obtain template " + pkg + " " + relativeName + " from " + location.getName());
            FileObject resource = getResource(location, pkg, relativeName);
            byte[] bytes = Tools.bytes(resource.openInputStream());
            return new Content(resource.getLastModified(), bytes, Charset.defaultCharset());
         }
         catch (Exception e)
         {
            log.log("Could not get template " + pkg + " " + relativeName + " from " + location.getName() + ":" + e.getMessage());
         }
      }

      //
      return null;
   }

   // Types implementation *********************************************************************************************

   public Element asElement(TypeMirror t)
   {
      return env.getTypeUtils().asElement(t);
   }

   public boolean isSameType(TypeMirror t1, TypeMirror t2)
   {
      return env.getTypeUtils().isSameType(t1, t2);
   }

   public boolean isSubtype(TypeMirror t1, TypeMirror t2)
   {
      return env.getTypeUtils().isSubtype(t1, t2);
   }

   public boolean isAssignable(TypeMirror t1, TypeMirror t2)
   {
      return env.getTypeUtils().isAssignable(t1, t2);
   }

   public boolean contains(TypeMirror t1, TypeMirror t2)
   {
      return env.getTypeUtils().contains(t1, t2);
   }

   public boolean isSubsignature(ExecutableType m1, ExecutableType m2)
   {
      return env.getTypeUtils().isSubsignature(m1, m2);
   }

   public List<? extends TypeMirror> directSupertypes(TypeMirror t)
   {
      return env.getTypeUtils().directSupertypes(t);
   }

   public TypeMirror erasure(TypeMirror t)
   {
      return env.getTypeUtils().erasure(t);
   }

   public TypeElement boxedClass(PrimitiveType p)
   {
      return env.getTypeUtils().boxedClass(p);
   }

   public PrimitiveType unboxedType(TypeMirror t)
   {
      return env.getTypeUtils().unboxedType(t);
   }

   public TypeMirror capture(TypeMirror t)
   {
      return env.getTypeUtils().capture(t);
   }

   public PrimitiveType getPrimitiveType(TypeKind kind)
   {
      return env.getTypeUtils().getPrimitiveType(kind);
   }

   public NullType getNullType()
   {
      return env.getTypeUtils().getNullType();
   }

   public NoType getNoType(TypeKind kind)
   {
      return env.getTypeUtils().getNoType(kind);
   }

   public ArrayType getArrayType(TypeMirror componentType)
   {
      return env.getTypeUtils().getArrayType(componentType);
   }

   public WildcardType getWildcardType(TypeMirror extendsBound, TypeMirror superBound)
   {
      return env.getTypeUtils().getWildcardType(extendsBound, superBound);
   }

   public DeclaredType getDeclaredType(TypeElement typeElem, TypeMirror... typeArgs)
   {
      return env.getTypeUtils().getDeclaredType(typeElem, typeArgs);
   }

   public DeclaredType getDeclaredType(DeclaredType containing, TypeElement typeElem, TypeMirror... typeArgs)
   {
      return env.getTypeUtils().getDeclaredType(containing, typeElem, typeArgs);
   }

   public TypeMirror asMemberOf(DeclaredType containing, Element element)
   {
      return env.getTypeUtils().asMemberOf(containing, element);
   }

   // Elements implementation ******************************************************************************************

   public PackageElement getPackageElement(CharSequence name)
   {
      return env.getElementUtils().getPackageElement(name);
   }

   public TypeElement getTypeElement(CharSequence name)
   {
      return env.getElementUtils().getTypeElement(name);
   }

   public Map<? extends ExecutableElement, ? extends AnnotationValue> getElementValuesWithDefaults(AnnotationMirror a)
   {
      return env.getElementUtils().getElementValuesWithDefaults(a);
   }

   public String getDocComment(Element e)
   {
      return env.getElementUtils().getDocComment(e);
   }

   public boolean isDeprecated(Element e)
   {
      return env.getElementUtils().isDeprecated(e);
   }

   public Name getBinaryName(TypeElement type)
   {
      return env.getElementUtils().getBinaryName(type);
   }

   public PackageElement getPackageOf(Element type)
   {
      return env.getElementUtils().getPackageOf(type);
   }

   public List<? extends Element> getAllMembers(TypeElement type)
   {
      return env.getElementUtils().getAllMembers(type);
   }

   public List<? extends AnnotationMirror> getAllAnnotationMirrors(Element e)
   {
      return env.getElementUtils().getAllAnnotationMirrors(e);
   }

   public boolean hides(Element hider, Element hidden)
   {
      return env.getElementUtils().hides(hider, hidden);
   }

   public boolean overrides(ExecutableElement overrider, ExecutableElement overridden, TypeElement type)
   {
      return env.getElementUtils().overrides(overrider, overridden, type);
   }

   public String getConstantExpression(Object value)
   {
      return env.getElementUtils().getConstantExpression(value);
   }

   public void printElements(Writer w, Element... elements)
   {
      env.getElementUtils().printElements(w, elements);
   }

   public Name getName(CharSequence cs)
   {
      return env.getElementUtils().getName(cs);
   }

   // Filer implementation *********************************************************************************************

   public JavaFileObject createSourceFile(CharSequence name, Element... originatingElements) throws IOException
   {
      return env.getFiler().createSourceFile(name, originatingElements);
   }

   public JavaFileObject createClassFile(CharSequence name, Element... originatingElements) throws IOException
   {
      return env.getFiler().createClassFile(name, originatingElements);
   }

   public FileObject createResource(JavaFileManager.Location location, CharSequence pkg, CharSequence relativeName, Element... originatingElements) throws IOException
   {
      return env.getFiler().createResource(location, pkg, relativeName, originatingElements);
   }

   public FileObject getResource(JavaFileManager.Location location, CharSequence pkg, CharSequence relativeName) throws IOException
   {
      return env.getFiler().getResource(location, pkg, relativeName);
   }
}
