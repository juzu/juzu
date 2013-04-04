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

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class AnnotationChange {

  /** . */
  final AnnotationKey key;

  /** . */
  final AnnotationState removed;

  /** . */
  final AnnotationState added;

  public AnnotationChange(AnnotationKey key, AnnotationState removed, AnnotationState added) {
    this.key = key;
    this.removed = removed;
    this.added = added;
  }

  public AnnotationKey getKey() {
    return key;
  }

  public AnnotationState getRemoved() {
    return removed;
  }

  public AnnotationState getAdded() {
    return added;
  }
}
