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

package org.juzu.impl.model.processor;

import org.juzu.impl.compiler.CompilationException;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.VariableElement;
import java.io.Serializable;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public interface ModelHandler extends Serializable
{

   public abstract void processControllerMethod(ExecutableElement methodElt, String annotationName, Map<String, Object> annotationValues) throws CompilationException;

   public abstract void processDeclarationTemplate(VariableElement variableElt, String annotationName, Map<String, Object> annotationValues) throws CompilationException;

   public abstract void processApplication(PackageElement packageElt, String annotationName, Map<String, Object> annotationValues) throws CompilationException;

   public abstract void postActivate(ProcessingContext env);

   public abstract void postProcess() throws CompilationException;

   public abstract void prePassivate();

}
