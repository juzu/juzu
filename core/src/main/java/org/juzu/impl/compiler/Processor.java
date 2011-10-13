package org.juzu.impl.compiler;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
@javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion.RELEASE_6)
@javax.annotation.processing.SupportedAnnotationTypes({"*"})
public class Processor extends AbstractProcessor
{

   /** . */
   final List<ProcessorPlugin> plugins;

   /** . */
   ProcessingEnvironment processingEnv;

   /** . */
   RoundEnvironment roundEnv;

   public Processor(List<ProcessorPlugin> plugins)
   {
      this.plugins = plugins;
   }

   public Processor(ProcessorPlugin... plugins)
   {
      this.plugins = Arrays.asList(plugins);
   }

   @Override
   public void init(ProcessingEnvironment processingEnv)
   {
      super.init(processingEnv);

      //
      this.processingEnv = processingEnv;

      //
      for (ProcessorPlugin plugin : plugins)
      {
         plugin.processor = this;
      }

      //
      for (ProcessorPlugin plugin : plugins)
      {
         plugin.init();
      }
   }

   @Override
   public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
   {
      this.roundEnv = roundEnv;

      try
      {
         // Process
         for (ProcessorPlugin plugin : plugins)
         {
            plugin.process();
         }

         // Over
         if (roundEnv.processingOver())
         {
            for (ProcessorPlugin plugin : plugins)
            {
               plugin.over();
            }
         }
      }
      catch (Exception e)
      {
         if (e instanceof CompilationException)
         {
            CompilationException ce = (CompilationException)e;
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, ce.getMessage(), ce.getElement());
         }
         else
         {
            String msg = e.getMessage();
            if (msg == null)
            {
               msg = "Exception : " + e.getClass().getName();
            }
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, msg);
         }
      }
      finally
      {
         this.roundEnv = null;
      }

      //
      return true;
   }
}
