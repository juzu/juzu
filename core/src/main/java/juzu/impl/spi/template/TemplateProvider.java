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

package juzu.impl.spi.template;

import java.io.Serializable;

/**
 * A provider for templating system.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @param <A> the template abstract syntax tree
 */
public abstract class TemplateProvider<A extends Serializable> {

  /**
   * Returns the template source extension without the dot recognised by
   * this provider. For instance it should return <code>gtmpl</code>
   * for groovy templates.
   *
   * @return the source extension
   */
  public abstract String getSourceExtension();

  /**
   * Returns the template target extension without the dot, this value
   * will be used only when the provider emits a file, i.e the method
   * {@link #emit(EmitContext, java.io.Serializable)} does not return null.
   *
   * @return the target extension
   */
  public abstract String getTargetExtension();

  /**
   * Return the template stub type.
   *
   * @return the template stub class
   */
  public abstract Class<? extends TemplateStub> getTemplateStubType();

  /**
   * Parse the provided char sequence and return the corresponding Abstract
   * Syntax Tree.
   *
   * @param s the sequence t parse
   * @return the corresponding Abstract Syntax Tree
   * @throws TemplateException any template related exception
   */
  public abstract A parse(CharSequence s) throws TemplateException;

  /**
   * Process the template.
   *
   * @param context the process context
   * @param template  the template to process
   * @throws TemplateException any template related exception
   */
  public abstract void process(ProcessContext context,
                               Template<A> template) throws TemplateException;

  /**
   * Provide an opportunity for emitting a file on the disk for the
   * {@link #getTargetExtension()}. When no file should be created,
   * null must be returned.
   *
   * @param context the emit context
   * @param ast the Abstract Syntax Tree
   * @return the emitted char sequence
   */
  public abstract CharSequence emit(EmitContext context, A ast);

}
