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

package org.juzu.impl.template;

import junit.framework.TestCase;
import org.juzu.impl.compiler.FileKey;
import org.juzu.impl.compiler.VirtualContent;
import org.juzu.impl.compiler.CompilerContext;
import org.juzu.impl.spi.fs.ram.RAMDir;
import org.juzu.impl.spi.fs.ram.RAMFile;
import org.juzu.impl.spi.fs.ram.RAMFileSystem;
import org.juzu.impl.spi.fs.ram.RAMPath;
import org.juzu.template.Template;
import org.juzu.text.WriterPrinter;

import javax.tools.JavaFileObject;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.HashMap;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class TemplateProcessorTestCase extends TestCase
{

   public void testFoo() throws Exception
   {

      RAMFileSystem ramFS = new RAMFileSystem();
      RAMDir root = ramFS.getRoot();
      RAMDir foo = root.addDir("foo");
      RAMFile a = foo.addFile("A.java").update("package foo; public class A { @org.juzu.template.TemplateRef(\"B.gtmpl\") Object template; }");
      RAMFile b = foo.addFile("B.gtmpl").update("<% out.print('hello') %>");

      //
      final CompilerContext<RAMPath, RAMDir, RAMFile> compiler = new CompilerContext<RAMPath, RAMDir, RAMFile>(ramFS);
      compiler.addAnnotationProcessor(new TemplateProcessor());
      assertTrue(compiler.compile());

      //
      VirtualContent content = compiler.getClassOutput(FileKey.newResourceName("foo", "B.groovy"));
      assertNotNull(content);
      assertEquals(3, compiler.getClassOutputKeys().size());

      //
      assertEquals(1, compiler.getSourceOutputKeys().size());
      VirtualContent content2 = compiler.getSourceOutput(FileKey.newJavaName("foo.B", JavaFileObject.Kind.SOURCE));
      assertNotNull(content2);

      ClassLoader cl = new ClassLoader(Thread.currentThread().getContextClassLoader())
      {

         /** . */
         private Map<String, Class<?>> cache = new HashMap<String, Class<?>>();

         @Override
         protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException
         {
            Class<?> clazz = cache.get(name);
            if (clazz == null)
            {
               try
               {
                  clazz = super.loadClass(name, resolve);
               }
               catch (ClassNotFoundException e)
               {
                  try
                  {
                     FileKey key = FileKey.newJavaName(name, JavaFileObject.Kind.CLASS);
                     VirtualContent content = compiler.getClassOutput(key);
                     if (content != null)
                     {
                        byte[] bytes = (byte[])content.getValue();
                        clazz = defineClass(name, bytes, 0, bytes.length);
                        cache.put(name, clazz);
                     }
                  }
                  catch (IOException ioe)
                  {
                     e.printStackTrace();
                  }
               }
            }
            if (clazz == null)
            {
               throw new ClassNotFoundException("not found " + name);
            }
            else
            {
               return clazz;
            }
         }

         @Override
         protected URL findResource(String name)
         {
            try
            {
               int pos = name.lastIndexOf('/');
               FileKey key;
               if (pos == -1)
               {
                  key = FileKey.newResourceName("", name);
               }
               else
               {
                  String packageName = name.substring(0, pos).replace('/', '.');
                  String foo = name.substring(pos + 1);
                  key = FileKey.newResourceName(packageName, foo);
               }
               final VirtualContent content = compiler.getClassOutput(key);
               if (content != null)
               {
                  return new URL("foo", "foo", 0, name, new URLStreamHandler()
                  {
                     @Override
                     protected URLConnection openConnection(URL u) throws IOException
                     {
                        return new URLConnection(u)
                        {
                           @Override
                           public void connect() throws IOException
                           {
                           }
                           @Override
                           public InputStream getInputStream() throws IOException
                           {
                              return new ByteArrayInputStream(content.getValue().toString().getBytes());
                           }
                        };
                     }
                  });
               }
            }
            catch (IOException e)
            {
               e.printStackTrace();
            }
            return super.findResource(name);
         }
      };

      Class<?> aClass = cl.loadClass("foo.A");
      Class<?> bClass = cl.loadClass("foo.B");
      Template template = (Template)bClass.newInstance();
      StringWriter out = new StringWriter();
      template.render(new WriterPrinter(out), null, null);
      assertEquals("hello", out.toString());
   }

}
