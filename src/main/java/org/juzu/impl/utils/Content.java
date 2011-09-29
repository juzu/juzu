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

package org.juzu.impl.utils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class Content<V>
{

   /** . */
   private long lastModified;

   public Content(long lastModified)
   {
      this.lastModified = lastModified;
   }

   public long getLastModified()
   {
      return lastModified;
   }

   public abstract InputStream getInputStream();

   public abstract CharSequence getCharSequence();

   public abstract V getValue();

   public static class CharArray extends Content<CharSequence>
   {

      /** . */
      private final CharSequence value;

      /** . */
      private final Charset charset;

      public CharArray(long lastModified, CharSequence value)
      {
         this(lastModified, value, Charset.defaultCharset());
      }

      public CharArray(long lastModified, CharSequence value, Charset charset)
      {
         super(lastModified);

         //
         this.value = value;
         this.charset = charset;
      }

      @Override
      public InputStream getInputStream()
      {
         return new ByteArrayInputStream(charset.encode(CharBuffer.wrap(value)).array());
      }

      @Override
      public CharSequence getCharSequence()
      {
         return value;
      }

      @Override
      public CharSequence getValue()
      {
         return value;
      }
   }

   public static class ByteArray extends Content<byte[]>
   {

      /** . */
      private final byte[] bytes;

      /** . */
      private Charset charset;

      public ByteArray(long lastModified, byte[] bytes, Charset charset)
      {
         super(lastModified);

         //
         this.bytes = bytes;
         this.charset = charset;
      }

      public ByteArray(long lastModified, byte[] bytes)
      {
         this(lastModified, bytes, Charset.defaultCharset());
      }

      @Override
      public InputStream getInputStream()
      {
         return new ByteArrayInputStream(bytes);
      }

      @Override
      public CharSequence getCharSequence()
      {
         return new String(bytes, charset);
      }

      @Override
      public byte[] getValue()
      {
         return bytes.clone();
      }
   }
}
