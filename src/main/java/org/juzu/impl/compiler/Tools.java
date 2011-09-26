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

import java.util.regex.Pattern;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class Tools
{

   /** . */
   private static Pattern EMPTY_NO_RECURSE = Pattern.compile("[^.]+");

   /** . */
   private static Pattern EMPTY_RECURSE = Pattern.compile(".+");

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
            regex = packageName + "(\\..+).";
         }
         else
         {
            regex = packageName + "\\.[^.]+";
         }
         return Pattern.compile(regex);
      }
   }
}
