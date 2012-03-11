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

package org.juzu.impl.template.compiler;

import org.juzu.impl.template.ast.ASTNode;
import org.juzu.impl.utils.FQN;

import java.io.Serializable;
import java.util.LinkedHashSet;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class Template implements Serializable
{

   /** The origin path. */
   private final String originPath;

   /** . */
   private final ASTNode.Template ast;

   /** . */
   private final FQN fqn;

   /** . */
   private final String extension;

   /** . */
   private final String path;

   /** . */
   private final LinkedHashSet<String> parameters;

   /** The last modified date. */
   private long lastModified;

   public Template(
      String originPath,
      ASTNode.Template ast,
      FQN fqn,
      String extension,
      String path,
      long lastModified)
   {
      this.originPath = originPath;
      this.ast = ast;
      this.fqn = fqn;
      this.extension = extension;
      this.path = path;
      this.parameters = new LinkedHashSet<String>();
      this.lastModified = lastModified;
   }

   public String getOriginPath()
   {
      return originPath;
   }

   public String getPath()
   {
      return path;
   }

   public String getExtension()
   {
      return extension;
   }

   public ASTNode.Template getAST()
   {
      return ast;
   }

   public FQN getFQN()
   {
      return fqn;
   }

   public long getLastModified()
   {
      return lastModified;
   }

   public LinkedHashSet<String> getParameters()
   {
      return parameters;
   }
   
   public void addParameter(String parameterName)
   {
      parameters.add(parameterName);
   }
}
