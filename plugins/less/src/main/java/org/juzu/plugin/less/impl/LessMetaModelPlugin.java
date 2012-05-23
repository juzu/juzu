package org.juzu.plugin.less.impl;

import org.juzu.impl.application.metamodel.ApplicationMetaModel;
import org.juzu.impl.compiler.AnnotationData;
import org.juzu.impl.compiler.ElementHandle;
import org.juzu.impl.compiler.ErrorCode;
import org.juzu.impl.compiler.ProcessingContext;
import org.juzu.impl.metamodel.MetaModelPlugin;
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
   public static final ErrorCode LESS_COMPILATION_ERROR = new ErrorCode("LESS_COMPILATION_ERROR", "There is an error in your .less file in %1$s");

   /** . */
   private final HashMap<ElementHandle.Package, AnnotationData> enabledMap = new HashMap<ElementHandle.Package, AnnotationData>();

   @Override
   public void processAnnotation(ApplicationMetaModel application, Element element, String fqn, AnnotationData data)
   {
      if (fqn.equals(Less.class.getName()))
      {
         enabledMap.put(application.getHandle(), data);
      }
   }

   @Override
   public void preDestroy(ApplicationMetaModel application)
   {
      enabledMap.remove(application.getHandle());
   }

   @Override
   public void prePassivate(ApplicationMetaModel model)
   {
      AnnotationData annotation = enabledMap.remove(model.getHandle());
      Boolean minify = (Boolean)annotation.get("minify");
      List<String> resources = (List<String>)annotation.get("value");
      if (resources != null && resources.size() > 0)
      {
         ProcessingContext env = model.model.env;

         // For now we use the hardcoded assets package
         QN pkg = model.getFQN().getPackageName().append("assets");

         //
         CompilerLessContext clc = new CompilerLessContext(env, pkg);

         //
         for (String resource : resources)
         {
            Path path = Path.parse(resource);

            //
            Path.Absolute to = Path.Absolute.create(pkg.append(path.getQN()), path.getRawName(), "css");

            //
            Lesser lesser;
            Result result;
            try
            {
               lesser = new Lesser(new JSR223Context());
               result = lesser.parse(clc, resource, Boolean.TRUE.equals(minify));
            }
            catch (Exception e)
            {
               throw new UnsupportedOperationException(e);
            }

            //
            if (result instanceof Compilation)
            {
               try
               {
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
                  throw new UnsupportedOperationException(e);
               }
            }
            else
            {
               Failure failure = (Failure)result;
               throw LESS_COMPILATION_ERROR.failure(resource, env.get(model.getHandle()));
            }
         }
      }
   }
}
