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

import org.juzu.impl.utils.Tools;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
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
import java.util.Date;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class BaseProcessor extends AbstractProcessor
{

   /** . */
   private static final String lineSep = System.getProperty("line.separator");

   /** . */
   private final static ThreadLocal<StringBuilder> log = new ThreadLocal<StringBuilder>();

   /** . */
   private static final ThreadLocal<DateFormat> format = new ThreadLocal<DateFormat>()
   {
      @Override
      protected DateFormat initialValue()
      {
         return new SimpleDateFormat("h:mm:ss:SSS");
      }
   };

   public static void log(CharSequence msg)
   {
      String s = format.get().format(new Date());
      log.get().append("[").append(s).append("] ").append(msg).append(lineSep);
   }

   @Override
   public final void init(ProcessingEnvironment processingEnv)
   {
      super.init(processingEnv);

      //
      this.log.set(new StringBuilder());

      //
      doInit(processingEnv);
   }

   protected void doInit(ProcessingEnvironment processingEnv)
   {

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
            msg = new StringBuilder("[").append(ce.getCode().name()).append("](");
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
         log(writer.getBuffer());

         // Report to javac
         processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, msg, element);
      }
      finally
      {
         if (roundEnv.processingOver())
         {
            String t = log.get().toString();
            log.set(null);

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
