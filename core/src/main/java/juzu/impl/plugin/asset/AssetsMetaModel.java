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
package juzu.impl.plugin.asset;

import juzu.impl.compiler.ElementHandle;
import juzu.impl.metamodel.AnnotationState;
import juzu.impl.metamodel.Key;
import juzu.impl.metamodel.MetaModelObject;

import java.util.HashMap;

/** @author Julien Viet */
public class AssetsMetaModel extends MetaModelObject {

  /** . */
  public final static Key<AssetsMetaModel> KEY = Key.of(AssetsMetaModel.class);

  /** . */
  final HashMap<ElementHandle.Package, AnnotationState> annotations = new HashMap<ElementHandle.Package, AnnotationState>();

}
