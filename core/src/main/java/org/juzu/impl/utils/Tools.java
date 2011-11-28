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

import javax.lang.model.element.Element;
import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class Tools
{

   /** . */
   public static Pattern EMPTY_NO_RECURSE = Pattern.compile("");

   /** . */
   public static Pattern EMPTY_RECURSE = Pattern.compile(".*");

   public static Pattern getPackageMatcher(String packageName, boolean recurse)
   {

      // PackageName       -> Identifier
      // PackageName       -> PackageName . Identifier
      // Identifier        -> IdentifierChars but not a Keyword or BooleanLiteral or NullLiteral
      // IdentifierChars   -> JavaLetter
      // IdentifierChars   -> IdentifierChars JavaLetterOrDigit
      // JavaLetter        -> any Unicode character that is a Java letter
      // JavaLetterOrDigit -> any Unicode character that is a Java letter-or-digit

      if (packageName.length() == 0)
      {
         return recurse ? EMPTY_RECURSE : EMPTY_NO_RECURSE;
      }
      else
      {
         String regex;
         if (recurse)
         {
            regex = Pattern.quote(packageName) + "(\\..*)?";
         }
         else
         {
            regex = Pattern.quote(packageName);
         }
         return Pattern.compile(regex);
      }
   }

   public static void escape(CharSequence s, StringBuilder appendable)
   {
      for (int i = 0;i < s.length();i++)
      {
         char c = s.charAt(i);
         if (c == '\n')
         {
            appendable.append("\\n");
         }
         else if (c == '\'')
         {
            appendable.append("\\\'");
         }
         else
         {
            appendable.append(c);
         }
      }
   }

   public static boolean safeEquals(Object o1, Object o2)
   {
      return o1 == null ? o2 == null : (o2 != null && o1.equals(o2));
   }

   public static void safeClose(Closeable closeable)
   {
      if (closeable != null)
      {
         try
         {
            closeable.close();
         }
         catch (IOException ignore)
         {
         }
      }
   }

   public static Method safeGetMethod(Class<?> type, String name, Class<?>... parameterTypes)
   {
      try
      {
         return type.getDeclaredMethod(name, parameterTypes);
      }
      catch (NoSuchMethodException e)
      {
         return null;
      }
   }

   public static <T> List<T> safeUnmodifiableList(T... list)
   {
      return safeUnmodifiableList(Arrays.asList(list));
   }

   public static <T> List<T> safeUnmodifiableList(List<T> list)
   {
      if (list == null || list.isEmpty())
      {
         return Collections.emptyList();
      }
      else
      {
         return Collections.unmodifiableList(new ArrayList<T>(list));
      }
   }

   public static byte[] bytes(InputStream in) throws IOException
   {
      try
      {
         ByteArrayOutputStream baos = new ByteArrayOutputStream(in.available());
         copy(in, baos);
         return baos.toByteArray();
      }
      finally
      {
         safeClose(in);
      }
   }

   public static void write(String content, File f) throws IOException
   {
      FileOutputStream out = new FileOutputStream(f);
      try
      {
         copy(new ByteArrayInputStream(content.getBytes()), out);
      }
      finally
      {
         safeClose(out);
      }
   }

   public static String read(File f) throws IOException
   {
      return read(new FileInputStream(f));
   }

   public static String read(InputStream in) throws IOException
   {
      return read(in, "UTF-8");
   }

   public static String read(InputStream in, String charsetName) throws IOException
   {
      try
      {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         copy(in, baos);
         return baos.toString();
      }
      finally
      {
         safeClose(in);
      }
   }

   public static void copy(InputStream in, OutputStream out) throws IOException
   {
      byte[] buffer = new byte[256];
      for (int l;(l = in.read(buffer)) != -1;)
      {
         out.write(buffer, 0, l);
      }
   }

   public static String unquote(String s) throws NullPointerException
   {
      if (s == null)
      {
         throw new NullPointerException("Can't unquote null string");
      }
      if (s.length() > 1)
      {
         char c1 = s.charAt(0);
         char c2 = s.charAt(s.length() - 1);
         if ((c1 == '\'' || c1 == '"') && c1 == c2)
         {
            return s.substring(1, s.length() - 1);
         }
      }
      return s;
   }

   public static String join(char separator, String... names)
   {
      switch (names.length)
      {
         case 0:
            return "";
         case 1:
            return names[0];
         default:
            StringBuilder sb = new StringBuilder();
            for (String name : names)
            {
               if (sb.length() > 0)
               {
                  sb.append(separator);
               }
               sb.append(name);
            }
            return sb.toString();
      }
   }

   public static String getImport(Class<?> clazz)
   {
      if (clazz.isLocalClass() || clazz.isAnonymousClass())
      {
         throw new IllegalArgumentException("Cannot use local or anonymous class");
      }
      else if (clazz.isMemberClass())
      {
         StringBuilder sb = new StringBuilder();
         while (clazz.isMemberClass())
         {
            sb.insert(0, clazz.getSimpleName());
            sb.insert(0, '.');
            clazz = clazz.getEnclosingClass();
         }
         sb.insert(0, clazz.getSimpleName());
         String pkg = clazz.getPackage().getName();
         if (pkg.length() > 0)
         {
            sb.insert(0, '.');
            sb.insert(0, pkg);
         }
         return sb.toString();
      }
      else
      {
         return clazz.getName();
      }
   }

   public static <E> HashSet<E> set(E... elements)
   {
      HashSet<E> set = new HashSet<E>(elements.length);
      Collections.addAll(set, elements);
      return set;
   }

   public static <E> ArrayList<E> list(Iterable<E> elements)
   {
      ArrayList<E> list = new ArrayList<E>();
      for (E elt : elements)
      {
         list.add(elt);
      }
      return list;
   }

   public static <T> T unserialize(Class<T> expectedType, File f) throws IOException, ClassNotFoundException
   {
      return unserialize(expectedType, new FileInputStream(f));
   }

   public static <T> T unserialize(Class<T> expectedType, InputStream in) throws IOException, ClassNotFoundException
   {
      try
      {
         ObjectInputStream ois = new ObjectInputStream(in);
         Object o = ois.readObject();
         return expectedType.cast(o);
      }
      finally
      {
         safeClose(in);
      }
   }

   /**
    * Parses a date formatted as ISO 8601.
    *
    * @param date the date
    * @return the time in millis corresponding to the date
    */
   public static long parseISO8601(String date)
   {
      return DatatypeConverter.parseDateTime(date).getTimeInMillis();
   }

   /**
    * Format the time millis as an ISO 8601 date.
    *
    * @param timeMillis the time to format
    * @return the ISO 8601 corresponding dat
    */
   public static String formatISO8601(long timeMillis)
   {
      Calendar c = Calendar.getInstance();
      c.setTimeInMillis(timeMillis);
      return DatatypeConverter.printDateTime(c);
   }

   public static long handle(Element te)
   {
      long hash = 0;
      for (Element enclosed : te.getEnclosedElements())
      {
         hash = 31 * hash + handle(enclosed);
      }
      hash = 31 * hash + te.getSimpleName().toString().hashCode();
      return hash;
   }
}
