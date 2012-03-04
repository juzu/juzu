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

package org.juzu.impl.compiler;

import org.juzu.impl.utils.ErrorCode;
import org.juzu.impl.utils.Logger;
import org.juzu.impl.utils.Tools;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class BaseProcessor extends AbstractProcessor
{

   /** . */
   private static final String lineSep = System.getProperty("line.separator");

   /** . */
   private final static ThreadLocal<StringBuilder> currentLog = new ThreadLocal<StringBuilder>();

   /** . */
   private static final ThreadLocal<DateFormat> format = new ThreadLocal<DateFormat>()
   {
      @Override
      protected DateFormat initialValue()
      {
         return new SimpleDateFormat("h:mm:ss:SSS");
      }
   };

   /** . */
   private static final Map<String, Logger> loggers = new HashMap<String, Logger>();

   /** . */
   private static final Logger logger = getLogger(BaseProcessor.class);

   public static Logger getLogger(Class<?> type)
   {
      String key = type.getName();
      final String name = type.getSimpleName();
      Logger logger = loggers.get(key);
      if (logger == null)
      {
         logger = new Logger()
         {
            public void log(CharSequence msg)
            {
               BaseProcessor.log(name, msg);
            }
            public void log(CharSequence msg, Throwable t)
            {
               BaseProcessor.log(name, msg, t);
            }
         };
         loggers.put(key, logger);
      }
      return logger;
   }

   private static void log(String name, CharSequence msg)
   {
      String s = format.get().format(new Date());
      StringBuilder sb = currentLog.get();
      if (sb != null)
      {
         sb.append(s).append(" ").append("[").append(name).append("] ").append(msg).append(lineSep);
      }
   }

   private static void log(String name, CharSequence msg, Throwable t)
   {
      StringWriter buffer = new StringWriter();
      t.printStackTrace(new PrintWriter(buffer));
      log(name, msg);
      StringBuilder sb = currentLog.get();
      if (sb != null)
      {
         sb.append(buffer);
      }
   }

   /** Controls how error are reported. */
   private boolean formalErrorReporting;

   protected BaseProcessor()
   {
      this.formalErrorReporting = false;
   }

   public boolean getFormalErrorReporting()
   {
      return formalErrorReporting;
   }

   public void setFormalErrorReporting(boolean formalErrorReporting)
   {
      this.formalErrorReporting = formalErrorReporting;
   }

   @Override
   public final void init(ProcessingEnvironment processingEnv)
   {
      super.init(processingEnv);

      //
      this.currentLog.set(new StringBuilder());

      //
      doInit(processingEnv);
   }

   @Override
   public final Set<String> getSupportedAnnotationTypes()
   {
      return Collections.singleton("*");
   }

   @Override
   public final SourceVersion getSupportedSourceVersion()
   {
      return javax.lang.model.SourceVersion.RELEASE_6;
   }

   /**
    * Perform the processor initialization.
    *
    * @param processingEnv the processing environment
    */
   protected void doInit(ProcessingEnvironment processingEnv)
   {
   }

   /**
    * Decode the error key and return a corresponding error code object. If no error can be decoded
    * for the specified key, null is returned.
    *
    * @param key the error key
    * @return the corresponding error
    */
   protected ErrorCode decode(String key)
   {
      return null;
   }

   @Override
   public final boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
   {
      try
      {
         doProcess(annotations, roundEnv);
      }
      catch (Exception e)
      {
         StringBuilder msg;
         Element element;
         if (e instanceof CompilationException)
         {
            CompilationException ce = (CompilationException)e;
            if (formalErrorReporting)
            {
               msg = new StringBuilder("[").append(ce.getCode().getKey()).append("](");
               Object[] args = ce.getArguments();
               for (int i = 0;i < args.length;i++)
               {
                  if (i > 0)
                  {
                     msg.append(',');
                  }
                  msg.append(String.valueOf(args[i]));
               }
               msg.append(")");
            }
            else
            {
               msg = new StringBuilder();
               new Formatter(msg).format(Locale.getDefault(), ce.getCode().getMessage(), ce.getArguments()).flush();
            }
            element = ce.getElement();
         }
         else
         {
            if (e.getMessage() == null)
            {
               msg = new StringBuilder("Exception : ").append(e.getClass().getName());
            }
            else
            {
               msg = new StringBuilder(e.getMessage());
            }
            element = null;
         }

         // Log error
         StringWriter writer = new StringWriter();
         writer.append(msg).append("\n");
         e.printStackTrace(new PrintWriter(writer));
         logger.log(writer.getBuffer());

         // Report to javac
         processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, msg, element);
      }
      finally
      {
         if (roundEnv.processingOver())
         {
            String t = currentLog.get().toString();
            currentLog.set(null);

            //
            if  (t.length() > 0)
            {
               String s = null;
               InputStream in = null;
               try
               {
                  FileObject file = processingEnv.getFiler().getResource(StandardLocation.SOURCE_OUTPUT, "org.juzu", "processor.log");
                  in = file.openInputStream();
                  s = Tools.read(in, "UTF-8");
               }
               catch (Exception ignore)
               {
               }
               finally
               {
                  Tools.safeClose(in);
               }
               OutputStream out = null;
               try
               {
                  FileObject file = processingEnv.getFiler().createResource(StandardLocation.SOURCE_OUTPUT, "org.juzu", "processor.log");
                  out = file.openOutputStream();
                  if (s != null)
                  {
                     out.write(s.getBytes("UTF-8"));
                  }
                  out.write(t.getBytes("UTF-8"));
               }
               catch (Exception ignore)
               {
               }
               finally
               {
                  Tools.safeClose(out);
               }
            }
         }
      }

      //
      return false;
   }

   protected abstract void doProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws CompilationException;
}
