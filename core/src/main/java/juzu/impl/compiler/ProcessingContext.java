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

package juzu.impl.compiler;

import juzu.impl.common.Name;
import juzu.impl.common.FileKey;
import juzu.impl.common.Path;
import juzu.impl.compiler.file.JavaFileObjectImpl;
import juzu.impl.fs.spi.ReadFileSystem;
import juzu.impl.fs.spi.disk.DiskFileSystem;
import juzu.impl.common.Logger;
import juzu.impl.common.Spliterator;
import juzu.impl.common.Tools;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
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
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.concurrent.Callable;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ProcessingContext implements Filer, Elements, Logger, Types {

  /** . */
  private static final MessageCode UNEXPECTED_ERROR = new MessageCode("UNEXPECTED_ERROR", "Unexpected error: %1$s");

  /**
   * We need two locations as the {@link javax.tools.StandardLocation#SOURCE_PATH} is not supported in eclipse ide (see
   * https://bugs.eclipse.org/bugs/show_bug.cgi?id=341298), however the {@link javax.tools.StandardLocation#CLASS_OUTPUT}
   * seems to work fairly well.
   */
  private static final StandardLocation[] RESOURCE_LOCATIONS = {StandardLocation.SOURCE_PATH, StandardLocation.CLASS_OUTPUT};

  /** . */
  private ProcessingEnvironment env;

  /** . */
  private static final Logger log = BaseProcessor.getLogger(ProcessingContext.class);

  /** Set for eclipse environment. */
  private final ReadFileSystem<File> sourcePath;

  /** . */
  private final ProcessingTool tool;

  /** The classloader for loading service via ServiceLoader. */
  private final ClassLoader serviceCL;

  /** The resources accessed, we need to have this map because some resources may only open one time (cf eclipse filer). */
  private Map<Key, FileObject> resources;

  /** . */
  private static final char PATH_SEPARATOR_CHAR = System.getProperty("path.separator").charAt(0);

  public ProcessingContext(ProcessingEnvironment env) {
    ProcessingTool tool;
    if (env.getMessager().getClass().getName().startsWith("org.eclipse.jdt")) {
      tool = ProcessingTool.ECLIPSE_IDE;
    }
    else {
      tool = ProcessingTool.JAVAC;
    }

    // Use this classloader by default
    ClassLoader serviceCL = ProcessingContext.class.getClassLoader();

    //
    ReadFileSystem<File> sourcePath = null;
    try {
      // As first attempt we tried to use the classpath since eclipse would copy the template to this location
      // but that could a chicken egg problem as a template is coped in the classpath only if the compilation
      // is successfull and sometimes a controller references a template literal that is generated from the
      // template source
      ClassLoader cl = env.getClass().getClassLoader();
      Class eclipseImplClass = cl.loadClass("org.eclipse.jdt.internal.apt.pluggable.core.dispatch.IdeProcessingEnvImpl");
      if (eclipseImplClass.isInstance(env)) {
        Method getJavaProject = eclipseImplClass.getMethod("getJavaProject");
        Object javaProject = getJavaProject.invoke(env);
        Class aptConfigClass = cl.loadClass("org.eclipse.jdt.apt.core.util.AptConfig");
        Class javaProjectClass = cl.loadClass("org.eclipse.jdt.core.IJavaProject");
        Method getProcessorOptionsMethod = aptConfigClass.getMethod("getProcessorOptions", javaProjectClass);
        Map<String, String> options = (Map<String, String>)getProcessorOptionsMethod.invoke(null, javaProject);
        log.info("Retrieved options " + options);

        //
        String sp = options.get("-sourcepath");
        log.info("Found sourcepath " + sp);
        if (sp != null) {
          // We take the first value
          Spliterator split = new Spliterator(sp, PATH_SEPARATOR_CHAR);
          if (split.hasNext()) {
            File root = new File(split.next());
            if (root.isDirectory()) {
              sourcePath = new DiskFileSystem(root);
            }
          }
        }

        // Building service class loader, this works better in eclipse specially with m2e and
        // the externally loaded plugins
        String cp = options.get("-classpath");
        log.info("Found classpath " + cp);
        if (cp != null) {
          ArrayList<URL> urls = new ArrayList<URL>();
          for (String s : Spliterator.split(cp, PATH_SEPARATOR_CHAR)) {
            File f = new File(s);
            if (f.exists()) {
              if (f.isFile() && f.getName().endsWith(".jar") || f.isDirectory()) {
                urls.add(f.toURI().toURL());
              }
            }
          }
          serviceCL = new URLClassLoader(urls.toArray(new URL[urls.size()]), serviceCL);
        }
      }
    }
    catch (Exception ignore) {
    }

    //
    log.info("Using processing tool " + tool);
    log.info("Using processing " + env);
    log.info("Using source path " + sourcePath);

    //
    this.env = env;
    this.sourcePath = sourcePath;
    this.tool = tool;
    this.serviceCL = serviceCL;
    this.resources = null;
  }

  // Various stuff ****************************************************************************************************

  public void report(Diagnostic.Kind kind,
                     CharSequence msg,
                     Element e,
                     AnnotationMirror a,
                     AnnotationValue v) {
    tool.report(env.getMessager(), kind, msg, e, a, v);
  }

  public ProcessingEnvironment getEnv() {
    return env;
  }
  
  public boolean isCopyFromSourcesExternallyManaged() {
      return tool.isCopyFromSourcesExternallyManaged();
  }

  public <E extends Element> E get(ElementHandle<E> handle) {
    return handle.get(env);
  }

  public <T> T executeWithin(ElementHandle element, Callable<T> callable) throws ProcessingException {
    return executeWithin(element.get(env), callable);
  }

  public <T> T executeWithin(Element element, Callable<T> callable) throws ProcessingException {
    try {
      return callable.call();
    }
    catch (ProcessingException e) {
      if (e.getElement() != null) {
        throw e;
      }
      else {
        throw new ProcessingException(element, null, e.getMessages()).initCause(e);
      }
    }
    catch (Exception e) {
      throw new ProcessingException(element, UNEXPECTED_ERROR, e.getMessage()).initCause(e);
    }
  }

  /** . */
  private Map<ElementHandle<?>, ReadFileSystem<File>> sourcePathMap = new HashMap<ElementHandle<?>, ReadFileSystem<File>>();

  /**
   * Returns the source path, this may return null.
   *
   * @param context the package element related
   * @return the source path
   * @throws IllegalArgumentException if the package cannot be resolved
   */
  public ReadFileSystem<File> getSourcePath(ElementHandle.Package context) throws IllegalArgumentException {
    PackageElement element = context.get(env);
    if (element == null) {
      throw new IllegalArgumentException("Package element cannot be resolved " + context);
    }
    if (sourcePath != null) {
      log.info("Found eclipse source path " + sourcePath + " for package " + context.getPackageName());
      return sourcePath;
    }
    else {
      ReadFileSystem<File> sourcePath = sourcePathMap.get(context);
      if (sourcePath == null) {
        try {
          log.info("Trying to find a native file system for package " + context.getPackageName());
          List<? extends AnnotationMirror> annotations = element.getAnnotationMirrors();
          if (annotations.size() > 0) {
            log.info("Found package " + context.getPackageName() + " annotations " + annotations + " will use first one");
            AnnotationMirror annotation = annotations.get(0);
            ClassLoader cl = env.getClass().getClassLoader();
            if (cl == null) {
              cl = ClassLoader.getSystemClassLoader();
            }
            Class<?> treesClass = cl.loadClass("com.sun.source.util.Trees");
            Method instanceMethod = treesClass.getMethod("instance", ProcessingEnvironment.class);
            Method getPathMethod = treesClass.getMethod("getPath", Element.class, AnnotationMirror.class);
            Object trees = instanceMethod.invoke(null, env);
            Object path = getPathMethod.invoke(trees, element, annotation);
            if (path != null) {
              Method getCompilationUnitMethod = path.getClass().getMethod("getCompilationUnit");
              Object cu = getCompilationUnitMethod.invoke(path);
              Method getSourceFileMethod = cu.getClass().getMethod("getSourceFile");
              JavaFileObject file = (JavaFileObject)getSourceFileMethod.invoke(cu);
              URI uri = file.toUri();
              log.info("Resolved uri " + uri + " for package " + context.getPackageName());
              File f = new File(uri.getPath());
              if (f.exists() && f.isFile()) {
                File dir = f.getParentFile().getParentFile();
                javax.lang.model.element.Name name = element.getQualifiedName();
                for (int i = 0;i < name.length();i++) {
                  if (name.charAt(i) == '.') {
                    dir = dir.getParentFile();
                  }
                }
                sourcePathMap.put(context, sourcePath = new DiskFileSystem(dir));
              }
            } else {
              log.info("No path object for package " + context.getPackageName());
            }
          }
          else {
            log.info("Package " + context.getPackageName() + " is not annotated (does not make sense)");
          }
        }
        catch (Exception e) {
          log.info("Could not resolve package " + context, e);
        }
      }
      else {
        log.info("Found cached source path " + sourcePath.getDescription() + " for package " + context.getPackageName());
      }
      return sourcePath;
    }
  }

  /**
   * Resolve a resource from the provided context and path.
   *
   * @param context the context of the application that will help to resolve the path source code
   * @param path the path of the resource to resolve
   * @return the resolved resource or null if it cannot be determined
   * @throws NullPointerException if any argument is null
   * @throws IllegalArgumentException if the context package is not valid
   */
  public FileObject resolveResourceFromSourcePath(ElementHandle.Package context, Path.Absolute path) throws NullPointerException, IllegalArgumentException {
    return resolveResourceFromSourcePath(context, FileKey.newName(path));
  }

  /**
   * Resolve a resource from the provided context and key.
   *
   * @param context the context of the application that will help to resolve the path source code
   * @param key the key of the resource to resolve
   * @return the resolved resource or null if it cannot be determined
   * @throws NullPointerException if any argument is null
   * @throws IllegalArgumentException if the context package is not valid
   */
  public FileObject resolveResourceFromSourcePath(ElementHandle.Package context, FileKey key) throws NullPointerException, IllegalArgumentException {
    if (context == null) {
      throw new NullPointerException("No null package accepted");
    }
    if (key == null) {
      throw new NullPointerException("No null path accepted");
    }
    ReadFileSystem<File> sourcePath = getSourcePath(context);
    if (sourcePath != null) {
      log.info("Attempt to resolve " + key + " from source path");
      try {
        File f = sourcePath.getPath(key.names);
        if (f != null) {
          log.info("Resolved " + key + " to " + f.getAbsolutePath());
          return new JavaFileObjectImpl<File>(StandardLocation.SOURCE_PATH, key, sourcePath, f);
        }
        else {
          log.info("Resolving " + key + " from source path gave no result");
        }
      }
      catch (IOException e) {
        log.info("Could not resolve " + key + " from source path", e);
      }
    }
    else {
      for (StandardLocation location : RESOURCE_LOCATIONS) {
        try {
          log.info("Attempt to resolve " + key + " from " + location.getName());
          FileObject resource = getResource(location, key);
          if (resource != null && resource.getLastModified() > 0) {
            return resource;
          }
        }
        catch (Exception e) {
          log.info("Could not resolve resource " + key + " from " + location.getName(), e);
        }
      }
    }
    return null;
  }

  /**
   * Resolve a resource from the provided context and path.
   *
   * @param context the context of the application that will help to resolve the path source code
   * @param path the path of the resource to resolve
   * @return the resolved resource or null if it cannot be determined
   * @throws NullPointerException if any argument is null
   * @throws IllegalArgumentException if the context package is not valid
   */
  public FileObject resolveResourceFromClassPath(ElementHandle.Package context, Path.Absolute path) throws NullPointerException, IllegalArgumentException {
    return resolveResourceFromClassPath(context, FileKey.newName(path));
  }

  /**
   * Resolve a resource from the provided context and key.
   *
   * @param context the context of the application that will help to resolve the path source code
   * @param key the key of the resource to resolve
   * @return the resolved resource or null if it cannot be determined
   * @throws NullPointerException if any argument is null
   * @throws IllegalArgumentException if the context package is not valid
   */
  public FileObject resolveResourceFromClassPath(ElementHandle.Package context, FileKey key) throws NullPointerException, IllegalArgumentException {
    if (context == null) {
      throw new NullPointerException("No null package accepted");
    }
    if (key == null) {
      throw new NullPointerException("No null path accepted");
    }
    try {
      log.info("Attempt to resolve " + key + " from classpath");
      Filer classPath = env.getFiler();
      FileObject object = classPath.getResource(StandardLocation.CLASS_PATH, key.packageFQN, key.name);
      if (object != null && object.getLastModified() > 0) {
        return object;
      }
    }
    catch (IOException e) {
      log.info("Could not resolve " + key + " from classpath", e);
    }

    //
    return null;
  }

  /**
   * Load a service, this is a replacement method for {@link ServiceLoader#load(Class, ClassLoader)}.
   *
   * @param service the service class to load
   * @param <S> the service generic type
   * @return an iterable of the loaded services
   * @throws NullPointerException if the <code>service</code> argument is null
   */
  public <S> Iterable<S> loadServices(Class<S> service) throws NullPointerException {
    if (service == null) {
      throw new NullPointerException("No null service class accepted");
    }
    log.info("Loading services implementation of " + service.getName());
    try {
      return Tools.list(ServiceLoader.load(service, serviceCL));
    }
    catch (ServiceConfigurationError e) {
      log.info("Could not load service for service " + service.getName(), e);
      return Collections.emptyList();
    }
  }

  // Types implementation *********************************************************************************************

  public Element asElement(TypeMirror t) {
    return env.getTypeUtils().asElement(t);
  }

  public boolean isSameType(TypeMirror t1, TypeMirror t2) {
    return env.getTypeUtils().isSameType(t1, t2);
  }

  public boolean isSubtype(TypeMirror t1, TypeMirror t2) {
    return env.getTypeUtils().isSubtype(t1, t2);
  }

  public boolean isAssignable(TypeMirror t1, TypeMirror t2) {
    return env.getTypeUtils().isAssignable(t1, t2);
  }

  public boolean contains(TypeMirror t1, TypeMirror t2) {
    return env.getTypeUtils().contains(t1, t2);
  }

  public boolean isSubsignature(ExecutableType m1, ExecutableType m2) {
    return env.getTypeUtils().isSubsignature(m1, m2);
  }

  public List<? extends TypeMirror> directSupertypes(TypeMirror t) {
    return env.getTypeUtils().directSupertypes(t);
  }

  public TypeMirror erasure(TypeMirror t) {
    return env.getTypeUtils().erasure(t);
  }

  public TypeElement boxedClass(PrimitiveType p) {
    return env.getTypeUtils().boxedClass(p);
  }

  public PrimitiveType unboxedType(TypeMirror t) {
    return env.getTypeUtils().unboxedType(t);
  }

  public TypeMirror capture(TypeMirror t) {
    return env.getTypeUtils().capture(t);
  }

  public PrimitiveType getPrimitiveType(TypeKind kind) {
    return env.getTypeUtils().getPrimitiveType(kind);
  }

  public NullType getNullType() {
    return env.getTypeUtils().getNullType();
  }

  public NoType getNoType(TypeKind kind) {
    return env.getTypeUtils().getNoType(kind);
  }

  public ArrayType getArrayType(TypeMirror componentType) {
    return env.getTypeUtils().getArrayType(componentType);
  }

  public WildcardType getWildcardType(TypeMirror extendsBound, TypeMirror superBound) {
    return env.getTypeUtils().getWildcardType(extendsBound, superBound);
  }

  public DeclaredType getDeclaredType(TypeElement typeElem, TypeMirror... typeArgs) {
    return env.getTypeUtils().getDeclaredType(typeElem, typeArgs);
  }

  public DeclaredType getDeclaredType(DeclaredType containing, TypeElement typeElem, TypeMirror... typeArgs) {
    return env.getTypeUtils().getDeclaredType(containing, typeElem, typeArgs);
  }

  public TypeMirror asMemberOf(DeclaredType containing, Element element) {
    return env.getTypeUtils().asMemberOf(containing, element);
  }

  public String getLiteralName(TypeMirror typeMirror) {
    TypeMirror erasedParameterTypeMirror = erasure(typeMirror);
    CharSequence typeLiteral = erasedParameterTypeMirror.toString();

    int index = Tools.indexOf(typeLiteral, '<', 0);
    if (index >= 0) {
      // In Eclipse java.util.List generates java.util.List<E>
      // so we must remove this to get a usable literal name
      return typeLiteral.subSequence(0, index).toString();
    } else {
      return typeLiteral.toString();
    }
  }

  // Elements implementation ******************************************************************************************

  public PackageElement getPackageElement(CharSequence name) {
    return env.getElementUtils().getPackageElement(name);
  }

  public TypeElement getTypeElement(CharSequence name) {
    return env.getElementUtils().getTypeElement(name);
  }

  public Map<? extends ExecutableElement, ? extends AnnotationValue> getElementValuesWithDefaults(AnnotationMirror a) {
    return env.getElementUtils().getElementValuesWithDefaults(a);
  }

  public String getDocComment(Element e) {
    return env.getElementUtils().getDocComment(e);
  }

  public boolean isDeprecated(Element e) {
    return env.getElementUtils().isDeprecated(e);
  }

  public javax.lang.model.element.Name getBinaryName(TypeElement type) {
    return env.getElementUtils().getBinaryName(type);
  }

  public PackageElement getPackageOf(Element type) {
    return env.getElementUtils().getPackageOf(type);
  }

  public List<? extends Element> getAllMembers(TypeElement type) {
    return env.getElementUtils().getAllMembers(type);
  }

  public List<? extends AnnotationMirror> getAllAnnotationMirrors(Element e) {
    return env.getElementUtils().getAllAnnotationMirrors(e);
  }

  public boolean hides(Element hider, Element hidden) {
    return env.getElementUtils().hides(hider, hidden);
  }

  public boolean overrides(ExecutableElement overrider, ExecutableElement overridden, TypeElement type) {
    return env.getElementUtils().overrides(overrider, overridden, type);
  }

  public String getConstantExpression(Object value) {
    return env.getElementUtils().getConstantExpression(value);
  }

  public void printElements(Writer w, Element... elements) {
    env.getElementUtils().printElements(w, elements);
  }

  public javax.lang.model.element.Name getName(CharSequence cs) {
    return env.getElementUtils().getName(cs);
  }

  // Filer implementation *********************************************************************************************

  public JavaFileObject createSourceFile(Name name, Element... originatingElements) throws IOException {
    return createSourceFile((CharSequence)name, originatingElements);
  }

  /**
   * @inheritDoc
   * @throws IllegalArgumentException if the originating element contains a null value
   */
  public JavaFileObject createSourceFile(CharSequence name, Element... originatingElements) throws IOException, IllegalArgumentException {
    // Gard against null element (java compiler is OK but ecj is not)
    for (Object originatingElement : originatingElements) {
      if (originatingElement == null) {
        throw new IllegalArgumentException("Originating elements contains a null element");
      }
    }
    log.info("Creating source file for name=" + name + " elements=" + Arrays.asList(originatingElements));
    return env.getFiler().createSourceFile(name, originatingElements);
  }

  /**
   * @inheritDoc
   * @throws IllegalArgumentException if the originating element contains a null value
   */
  public JavaFileObject createClassFile(CharSequence name, Element... originatingElements) throws IOException, IllegalArgumentException {
    // Gard against null element (java compiler is OK but ecj is not)
    for (Object originatingElement : originatingElements) {
      if (originatingElement == null) {
        throw new IllegalArgumentException("Originating elements contains a null element");
      }
    }
    log.info("Creating class file for name=" + name + " elements=" + Arrays.asList(originatingElements));
    return env.getFiler().createClassFile(name, originatingElements);
  }

  public FileObject createResource(JavaFileManager.Location location, Path.Absolute path, Element... originatingElements) throws IOException {
    return createResource(location, path.getDirs(), path.getSimpleName(), originatingElements);
  }

  public FileObject createResource(JavaFileManager.Location location, FileKey key, Element... originatingElements) throws IOException {
    return createResource(location, key.packageFQN, key.name, originatingElements);
  }

  public FileObject createResource(JavaFileManager.Location location, CharSequence pkg, CharSequence relativeName, Element... originatingElements) throws IOException {
    if (tool.getOverwriteReadingResource()) {
      Key key = new Key(location, pkg.toString(), relativeName.toString());
      FileObject resource = resources != null ? resources.get(key) : null;
      if (resource == null) {
        log.info("Creating resource file for location=" + location + " pkg=" + pkg + " relativeName=" + relativeName + " elements=" + Arrays.asList(originatingElements));
        resource = env.getFiler().createResource(location, pkg, relativeName, originatingElements);
        if (resources == null) {
          resources = new HashMap<Key, FileObject>();
        }
        resources.put(new Key(location, pkg.toString(), relativeName.toString()), resource);
      }
      return resource;
    } else {
      return env.getFiler().createResource(location, pkg, relativeName, originatingElements);
    }
  }

  public FileObject getResource(JavaFileManager.Location location, Path.Absolute path) throws IOException {
    return getResource(location, path.getDirs(), path.getSimpleName());
  }

  public FileObject getResource(JavaFileManager.Location location, FileKey key) throws IOException {
    return getResource(location, key.packageFQN, key.name);
  }

  public FileObject getResource(JavaFileManager.Location location, CharSequence pkg, CharSequence relativeName) throws IOException {
    Key key = new Key(location, pkg.toString(), relativeName.toString());
    FileObject resource = resources != null ? resources.get(key) : null;
    if (resource == null) {
      try {
        resource = env.getFiler().getResource(location, pkg, relativeName);
      }
      catch (IOException e) {
        // Likely to happen in ECJ
      }
      if (resource != null) {
        if (resource.getLastModified() > 0) {
          if (resources == null) {
            resources = new HashMap<Key, FileObject>();
          }
          resources.put(key, resource);
        } else {
          resource = null;
        }
      }
    }
    return resource;
  }

  // Logger implementation

  public void info(CharSequence msg) {
    log.info(msg);
  }

  public void info(CharSequence msg, Throwable t) {
    log.info(msg, t);
  }

  /**
   * A cache key.
   */
  private static final class Key {
    final JavaFileManager.Location location;
    final String pkg;
    final String relativeName;
    private Key(JavaFileManager.Location location, String pkg, String relativeName) {
      this.location = location;
      this.pkg = pkg;
      this.relativeName = relativeName;
    }

    @Override
    public int hashCode() {
      return 13 * (13 * location.hashCode() + pkg.hashCode()) + relativeName.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this) {
        return true;
      } else if (obj instanceof Key) {
        Key that = (Key)obj;
        return location.equals(that.location) && pkg.equals(that.pkg) && relativeName.equals(that.relativeName);
      } else {
        return false;
      }
    }
  }
}
