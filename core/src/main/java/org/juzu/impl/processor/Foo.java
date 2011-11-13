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

package org.juzu.impl.processor;

import org.juzu.impl.compiler.CompilationException;

import javax.lang.model.element.Element;
import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class Foo implements Serializable
{

   /** . */
   public static final Pattern NAME_PATTERN = Pattern.compile("([^/].*/|)([^./]+)\\.([a-zA-Z]+)");

   /** The origin element. */
   private final ElementHandle origin;

   /** The package of the originating reference. */
   private final String originPackageFQN;

   /** The original path value (we always have path = folder + rawName + extension). */
   private final String path;

   /** The folder path built which may be empty. */
   private final String folder;

   /** The raw name. */
   private final String rawName;

   /** The extension. */
   private final String extension;

   public Foo(Foo that, String path)
   {
      this(that.origin, that.originPackageFQN, path);
   }

   public Foo(Element origin, String originPackageFQN, String path)
   {
      this(ElementHandle.create(origin), originPackageFQN, path);
   }

   private Foo(ElementHandle origin, String originPackageFQN, String path)
   {
      Matcher matcher = NAME_PATTERN.matcher(path);
      if (!matcher.matches())
      {
         throw new CompilationException(MainProcessor.get(origin), ErrorCode.ILLEGAL_PATH, path);
      }

      //
      this.origin = origin;
      this.originPackageFQN = originPackageFQN;
      this.path = path;
      this.folder = matcher.group(1);
      this.rawName = matcher.group(2);
      this.extension = matcher.group(3);
   }

   public ElementHandle getOrigin()
   {
      return origin;
   }

   public String getOriginPackageFQN()
   {
      return originPackageFQN;
   }

   public String getPath()
   {
      return path;
   }

   public String getFolder()
   {
      return folder;
   }

   public String getRawName()
   {
      return rawName;
   }

   public String getExtension()
   {
      return extension;
   }
}
