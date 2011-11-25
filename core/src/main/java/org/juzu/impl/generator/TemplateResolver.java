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

package org.juzu.impl.generator;

import org.juzu.impl.compiler.BaseProcessor;
import org.juzu.impl.utils.Content;
import org.juzu.impl.utils.FQN;
import org.juzu.impl.utils.Tools;

import javax.annotation.processing.Filer;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.nio.charset.Charset;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class TemplateResolver
{

   /**
    * We need two locations as the {@link javax.tools.StandardLocation#SOURCE_PATH} is not supported in eclipse ide
    * (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=341298), however the {@link javax.tools.StandardLocation#CLASS_OUTPUT}
    * seems to work fairly well.
    */
   private static final StandardLocation[] locations = { StandardLocation.SOURCE_PATH, StandardLocation.CLASS_OUTPUT};

   /** . */
   private final Filer filer;

   TemplateResolver(Filer filer)
   {
      this.filer = filer;
   }

   Content resolve(FQN fqn, String extension)
   {
      for (StandardLocation location : locations)
      {
         String pkg = fqn.getPackageName().getValue();
         String relativeName = fqn.getSimpleName() + "." + extension;
         try
         {
            BaseProcessor.log("Attempt to obtain template " + pkg + " " + relativeName + " from " + location.getName());
            FileObject resource = filer.getResource(location, pkg, relativeName);
            byte[] bytes = Tools.bytes(resource.openInputStream());
            return new Content(resource.getLastModified(), bytes, Charset.defaultCharset());
         }
         catch (Exception e)
         {
            BaseProcessor.log("Could not get template " + pkg + " " + relativeName + " from " + location.getName() + ":" + e.getMessage());
         }
      }

      //
      return null;
   }
}
