/*
 * Copyright (C) 2012 eXo Platform SAS.
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

package juzu.impl.template.spi;

import java.io.IOException;
import java.io.Serializable;

/**
 * A provider for templating system.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @param <M> the template model
 */
public abstract class TemplateProvider<M extends Serializable> {

  /**
   * Returns the template source extension without the dot recognised by
   * this provider. For instance it should return <code>gtmpl</code>
   * for groovy templates.
   *
   * @return the source extension
   */
  public abstract String getSourceExtension();

  /**
   * Return the template stub type.
   *
   * @return the template stub class
   */
  public abstract Class<? extends TemplateStub> getTemplateStubType();

  /**
   * Parse the provided char sequence and return the corresponding template model.
   *
   * @param context the parse context
   * @param source the source to parse
   * @return the corresponding template model
   * @throws TemplateException any template related exception
   */
  public abstract M parse(
      ParseContext context,
      CharSequence source) throws TemplateException;

  /**
   * Process the template.
   *
   * @param context the process context
   * @param template  the template to process
   * @throws TemplateException any template related exception
   */
  public abstract void process(
      ProcessContext context,
      Template<M> template) throws TemplateException;

  /**
   * Provide an opportunity for emitting a file on the disk.
   *
   * @param context the emit context
   * @param template the template
   * @throws TemplateException any template related exception
   * @throws IOException any io exception
   */
  public abstract void emit(
      EmitContext context,
      Template<M> template) throws TemplateException, IOException;

}
