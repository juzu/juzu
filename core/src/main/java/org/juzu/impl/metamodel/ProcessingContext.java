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

package org.juzu.impl.metamodel;

import org.juzu.impl.compiler.BaseProcessor;
import org.juzu.impl.compiler.CompilationException;
import org.juzu.impl.compiler.ElementHandle;
import org.juzu.impl.spi.fs.ReadFileSystem;
import org.juzu.impl.spi.fs.disk.DiskFileSystem;
import org.juzu.impl.utils.Content;
import org.juzu.impl.utils.FQN;
import org.juzu.impl.utils.Logger;
import org.juzu.impl.utils.Spliterator;
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
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

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

   /** Set for eclipse environment. */
   private final ReadFileSystem<File> sourcePath;

   public ProcessingContext(ProcessingEnvironment env)
   {
      ReadFileSystem<File> sourcePath = null;
      try
      {
         // As first attempt we tried to use the classpath since eclipse would copy the template to this location
         // but that could a chicken egg problem as a template is coped in the classpath only if the compilation
         // is successfull and sometimes a controller references a template literal that is generated from the
         // template source
         ClassLoader cl = env.getClass().getClassLoader();
         Class eclipseImplClass = cl.loadClass("org.eclipse.jdt.internal.apt.pluggable.core.dispatch.IdeProcessingEnvImpl");
         if (eclipseImplClass.isInstance(env))
         {
            Method getJavaProject = eclipseImplClass.getMethod("getJavaProject");
            Object javaProject = getJavaProject.invoke(env);
            Class aptConfigClass = cl.loadClass("org.eclipse.jdt.apt.core.util.AptConfig");
            Class javaProjectClass = cl.loadClass("org.eclipse.jdt.core.IJavaProject");
            Method getProcessorOptionsMethod = aptConfigClass.getMethod("getProcessorOptions", javaProjectClass);
            Map<String, String> options = (Map<String, String>)getProcessorOptionsMethod.invoke(null, javaProject);
            log.log("Retrieved options " + options);
            String sp = options.get("-sourcepath");
            log.log("Found sourcepath " + sp);
            if (sp != null)
            {
               // We take the first value
               Spliterator split = new Spliterator(sp, ':');
               if (split.hasNext())
               {
                  File root = new File(split.next());
                  if (root.isDirectory())
                  {
                     sourcePath = new DiskFileSystem(root);
                  }
               }
            }
         }
      }
      catch (Exception ignore)
      {
      }

      //
      log.log("Using processing " + env);
      log.log("Using source path " + sourcePath);

      //
      this.env = env;
      this.sourcePath = sourcePath;
   }

   // Various stuff ****************************************************************************************************

   public <E extends Element> E get(ElementHandle<E> handle)
   {
      return handle.get(env);
   }

   public <T> T executeWithin(ElementHandle element, Callable<T> callable) throws CompilationException
   {
      return executeWithin(element.get(env), callable);
   }

   public <T> T executeWithin(Element element, Callable<T> callable) throws CompilationException
   {
      try
      {
         return callable.call();
      }
      catch (CompilationException e)
      {
         if (e.getElement() != null)
         {
            throw e;
         }
         else
         {
            throw new CompilationException(e, element, e.getCode(), (Object)e.getArguments());
         }
      }
      catch (Exception e)
      {
         throw new CompilationException(e, element, MetaModelError.UNEXPECTED_ERROR, e.getMessage());
      }
   }

   /** . */
   private Map<ElementHandle<?>, ReadFileSystem<File>> sourcePathMap = new HashMap<ElementHandle<?>, ReadFileSystem<File>>();

   private ReadFileSystem<File> getSourcePath(ElementHandle.Package context)
   {
      if (sourcePath != null)
      {
         log.log("Found eclipse source path " + sourcePath + " for package " + context.getQN());
         return sourcePath;
      }
      else
      {
         ReadFileSystem<File> sourcePath = sourcePathMap.get(context);
         if (sourcePath == null)
         {
            try
            {
               PackageElement element = context.get(env);
               log.log("Trying to find a native file system for package " + context.getQN());
               List<? extends AnnotationMirror> annotations = element.getAnnotationMirrors();
               if (annotations.size() > 0)
               {
                  log.log("Found package " + context.getQN() + " annotations " + annotations + " will use first one");
                  AnnotationMirror annotation = annotations.get(0);
                  Class<?> treesClass = ClassLoader.getSystemClassLoader().loadClass("com.sun.source.util.Trees");
                  Method instanceMethod = treesClass.getMethod("instance", ProcessingEnvironment.class);
                  Method getPathMethod = treesClass.getMethod("getPath", Element.class, AnnotationMirror.class);
                  Object trees = instanceMethod.invoke(null, env);
                  Object path = getPathMethod.invoke(trees, element, annotation);
                  Method getCompilationUnitMethod = path.getClass().getMethod("getCompilationUnit");
                  Object cu = getCompilationUnitMethod.invoke(path);
                  Method getSourceFileMethod = cu.getClass().getMethod("getSourceFile");
                  JavaFileObject file = (JavaFileObject)getSourceFileMethod.invoke(cu);
                  URI uri = file.toUri();
                  log.log("Resolved uri " + uri + " for package " + context.getQN());
                  File f = new File(uri.getPath());
                  if (f.exists() && f.isFile())
                  {
                     File dir = f.getParentFile().getParentFile();
                     Name name = element.getQualifiedName();
                     for (int i = 0;i < name.length();i++)
                     {
                        if (name.charAt(i) == '.')
                        {
                           dir = dir.getParentFile();
                        }
                     }
                     sourcePathMap.put(context, sourcePath = new DiskFileSystem(dir));
                  }
               }
               else
               {
                  log.log("Package " + context.getQN() + " is not annotated (does not make sense)");
               }
            }
            catch (Exception ignore)
            {
               log.log("Could not resolve package " + context);
            }
         }
         else
         {
            log.log("Found cached source path " + sourcePath.getDescription() + " for package " + context.getQN());
         }
         return sourcePath;
      }
   }

   public Content resolveResource(ElementHandle.Package context, FQN fqn, String extension)
   {
      ReadFileSystem<File> sourcePath = getSourcePath(context);
      String path = "/" + fqn.getFullName().replace('.', '/') + "." + extension;
      if (sourcePath != null)
      {
         log.log("Attempt to resolve " + path + " from source path");
         try
         {
            List<String> list = new ArrayList<String>();
            Spliterator.split(fqn.getPackageName().getValue(), '.', list);
            list.add(fqn.getSimpleName() + "." + extension);
            File f = sourcePath.getPath(list);
            if (f != null)
            {
               log.log("Resolved " + fqn + "." + extension + " to " + f.getAbsolutePath());
               return sourcePath.getContent(f);
            }
            else
            {
               log.log("Resolving " + path + " from source path gave no result");
            }
         }
         catch (IOException e)
         {
            log.log("Could not resolve " + path + " from source path", e);
         }
      }
      else
      {
         for (StandardLocation location : RESOURCE_LOCATIONS)
         {
            String pkg = fqn.getPackageName().getValue();
            String relativeName = fqn.getSimpleName() + "." + extension;
            try
            {
               log.log("Attempt to resolve " + path + " from " + location.getName());
               FileObject resource = getResource(location, pkg, relativeName);
               byte[] bytes = Tools.bytes(resource.openInputStream());
               return new Content(resource.getLastModified(), bytes, Charset.defaultCharset());
            }
            catch (Exception e)
            {
               log.log("Could not resolve resource " + path + " from " + location.getName(), e);
            }
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
      log.log("Creating source file for name=" + name + " elements=" + Arrays.asList(originatingElements));
      return env.getFiler().createSourceFile(name, originatingElements);
   }

   public JavaFileObject createClassFile(CharSequence name, Element... originatingElements) throws IOException
   {
      log.log("Creating class file for name=" + name + " elements=" + Arrays.asList(originatingElements));
      return env.getFiler().createClassFile(name, originatingElements);
   }

   public FileObject createResource(JavaFileManager.Location location, CharSequence pkg, CharSequence relativeName, Element... originatingElements) throws IOException
   {
      log.log("Creating resource file for location=" + location + " pkg=" + pkg + " relativeName=" + relativeName + " elements=" + Arrays.asList(originatingElements));
      return env.getFiler().createResource(location, pkg, relativeName, originatingElements);
   }

   public FileObject getResource(JavaFileManager.Location location, CharSequence pkg, CharSequence relativeName) throws IOException
   {
      return env.getFiler().getResource(location, pkg, relativeName);
   }
}
