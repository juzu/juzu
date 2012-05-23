package org.juzu.plugin.less.impl;

import org.juzu.impl.application.metamodel.ApplicationMetaModel;
import org.juzu.impl.compiler.AnnotationData;
import org.juzu.impl.compiler.BaseProcessor;
import org.juzu.impl.compiler.ElementHandle;
import org.juzu.impl.compiler.ErrorCode;
import org.juzu.impl.compiler.ProcessingContext;
import org.juzu.impl.metamodel.MetaModelPlugin;
import org.juzu.impl.utils.Logger;
import org.juzu.impl.utils.Path;
import org.juzu.impl.utils.QN;
import org.juzu.impl.utils.Tools;
import org.juzu.plugin.less.Less;
import org.juzu.plugin.less.impl.lesser.Compilation;
import org.juzu.plugin.less.impl.lesser.Failure;
import org.juzu.plugin.less.impl.lesser.JSR223Context;
import org.juzu.plugin.less.impl.lesser.Lesser;
import org.juzu.plugin.less.impl.lesser.Result;

import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class LessMetaModelPlugin extends MetaModelPlugin
{

   /** . */
   public static final ErrorCode COMPILATION_ERROR = new ErrorCode("LESS_COMPILATION_ERROR", "There is an error in your .less file in %1$s");

   /** . */
   public static final ErrorCode MALFORMED_PATH = new ErrorCode("LESS_MALFORMED_PATH", "The resource path %1$s is malformed");

   /** . */
   static final Logger log = BaseProcessor.getLogger(LessMetaModelPlugin.class);

   /** . */
   private final HashMap<ElementHandle.Package, AnnotationData> enabledMap = new HashMap<ElementHandle.Package, AnnotationData>();

   @Override
   public void processAnnotation(ApplicationMetaModel application, Element element, String fqn, AnnotationData data)
   {
      if (fqn.equals(Less.class.getName()))
      {
         ElementHandle.Package pkg = application.getHandle();
         log.log("Recording less annotation for package " + pkg.getQN());
         enabledMap.put(pkg, data);
      }
   }

   @Override
   public void preDestroy(ApplicationMetaModel application)
   {
      ElementHandle.Package pkg = application.getHandle();
      log.log("Removing less annotation for package " + pkg.getQN());
      AnnotationData data = enabledMap.remove(application.getHandle());
      log.log("Removed less annotation for package " + pkg.getQN() + ": " + data);
   }

   @Override
   public void prePassivate(ApplicationMetaModel model)
   {
      AnnotationData annotation = enabledMap.remove(model.getHandle());
      if (annotation != null)
      {
         ElementHandle.Package pkg = model.getHandle();
         ProcessingContext env = model.model.env;
         PackageElement pkgElt = env.get(model.getHandle());
         Boolean minify = (Boolean)annotation.get("minify");
         List<String> resources = (List<String>)annotation.get("value");

         //
         log.log("Handling less annotation for package " + pkg.getQN() + ": minify=" + minify + " resources=" + resources);

         //
         if (resources != null && resources.size() > 0)
         {

            // For now we use the hardcoded assets package
            QN assetPkg = model.getFQN().getPackageName().append("assets");

            //
            CompilerLessContext clc = new CompilerLessContext(env, assetPkg);

            //
            for (String resource : resources)
            {
               log.log("Processing declared resource " + resource);

               //
               Path path;
               try
               {
                  path = Path.parse(resource);
               }
               catch (IllegalArgumentException e)
               {
                  throw MALFORMED_PATH.failure(pkgElt, resource).initCause(e);
               }

               //
               Path.Absolute to = Path.Absolute.create(assetPkg.append(path.getQN()), path.getRawName(), "css");
               log.log("Resource " + resource + " destination resolved to " + to);

               //
               Lesser lesser;
               Result result;
               try
               {
                  lesser = new Lesser(new JSR223Context());
                  result = lesser.compile(clc, resource, Boolean.TRUE.equals(minify));
               }
               catch (Exception e)
               {
                  log.log("Unexpected exception", e);
                  throw new UnsupportedOperationException(e);
               }

               //
               if (result instanceof Compilation)
               {
                  try
                  {
                     log.log("Resource " + resource + " compiled about to write on disk as " + to);
                     Compilation compilation = (Compilation)result;
                     FileObject fo = env.createResource(StandardLocation.CLASS_OUTPUT, to);
                     Writer writer = fo.openWriter();
                     try
                     {
                        writer.write(compilation.getValue());
                     }
                     finally
                     {
                        Tools.safeClose(writer);
                     }
                  }
                  catch (IOException e)
                  {
                     log.log("Resource " + to + " could not be written on disk", e);
                  }
               }
               else
               {
                  Failure failure = (Failure)result;
                  log.log("Resource " + resource + " for package " + pkgElt + " could not be compiled: " + failure);
                  throw COMPILATION_ERROR.failure(pkgElt, resource);
               }
            }
         }
      }
   }
}
