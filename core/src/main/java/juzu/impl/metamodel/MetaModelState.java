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

package juzu.impl.metamodel;

import juzu.impl.compiler.ProcessingContext;

import java.io.Serializable;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class MetaModelState<P extends MetaModelPlugin<M, P>, M extends MetaModel<P, M>> implements Serializable {

  /** . */
  final MetaModelContext<P, M> context;

  /** . */
  final M metaModel;

  public MetaModelState(Class<P> pluginType, M metaModel) {
    this.context = new MetaModelContext<P, M>(pluginType);
    this.metaModel = metaModel;
  }

  void init(ProcessingContext env) {
    context.init(env);
    context.add(metaModel);
  }
}
