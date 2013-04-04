/*
 * Copyright 2013 eXo Platform SAS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
