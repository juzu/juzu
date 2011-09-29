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

import org.juzu.impl.utils.Content;

import javax.tools.SimpleJavaFileObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class VirtualJavaFileObject extends SimpleJavaFileObject
{

   /***/
   final FileKey key;

   VirtualJavaFileObject(FileKey key)
   {
      super(key.uri, key.kind);

      //
      this.key = key;
   }

   /**
    * File system.
    */
   static class FileSystem<P, D extends P, F extends P> extends VirtualJavaFileObject
   {

      /** . */
      private final F file;

      /** . */
      private final org.juzu.impl.spi.fs.FileSystem<P, D, F> fs;

      /** . */
      private CharSequence content;

      /** . */
      private long lastModified;

      FileSystem(org.juzu.impl.spi.fs.FileSystem<P, D, F> fs, F file, FileKey key) throws IOException
      {
         super(key);

         //
         this.fs = fs;
         this.file = file;
      }

      @Override
      public long getLastModified()
      {
         if (lastModified == 0)
         {
            try
            {
               lastModified = fs.getLastModified(file);
            }
            catch (IOException ignore)
            {
               // We return 0 as the javadoc say to do
            }
         }

         //
         return lastModified;
      }

      @Override
      public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException
      {
         long lastModified = fs.getLastModified(file);

         //
         if (content == null || this.lastModified < lastModified)
         {
            Content content = fs.getContent(file);
            this.content = content.getCharSequence();
            this.lastModified = content.getLastModified();
         }

         //
         return content;
      }
   }

   static class RandomAccess<C> extends VirtualJavaFileObject
   {

      /** . */
      protected Content<?> content;

      RandomAccess(FileKey key)
      {
         super(key);

         //
         this.content = null;
      }

      @Override
      public final long getLastModified()
      {
         return content == null ? 0 : content.getLastModified();
      }

      /**
       * Compiled class.
       */
      static class Binary extends RandomAccess<byte[]>
      {

         /** . */
         private ByteArrayOutputStream out;

         Binary(FileKey key) throws IOException
         {
            super(key);

            //
            this.out = null;
         }

         @Override
         public OutputStream openOutputStream() throws IOException
         {
            content = null;
            out = new ByteArrayOutputStream() {
               @Override
               public void close() throws IOException
               {
                  content = new Content.ByteArray(System.currentTimeMillis(), toByteArray());
                  out = null;
               }
            };
            return out;
         }

         @Override
         public InputStream openInputStream() throws IOException
         {
            if (content != null)
            {
               return content.getInputStream();
            }
            else
            {
               throw new IOException("No content");
            }
         }
      }

      /**
       * Generated class.
       */
      static class Text extends RandomAccess<CharSequence>
      {

         /** . */
         private StringWriter writer;

         Text(FileKey key) throws IOException
         {
            super(key);

            //
            this.writer = null;
         }

         @Override
         public Writer openWriter() throws IOException
         {
            content = null;
            writer = new StringWriter() {
               @Override
               public void close() throws IOException
               {
                  content = new Content.CharArray(System.currentTimeMillis(), writer.toString());
                  writer = null;
               }
            };
            return writer;
         }

         @Override
         public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException
         {
            if (content != null)
            {
               return content.getCharSequence();
            }
            else
            {
               throw new IOException("No content");
            }
         }
      }
   }
}
