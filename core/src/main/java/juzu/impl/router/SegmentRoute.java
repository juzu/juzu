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

package juzu.impl.router;

import juzu.impl.common.PercentCodec;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
class SegmentRoute extends Route {

  /** . */
  final String name;

  /** . */
  final String encodedName;

  SegmentRoute(Router router, String name, int terminal) {
    super(router, terminal);

    //
    if (name.length() == 0) {
      throw new AssertionError();
    }

    //
    this.name = name;
    this.encodedName = PercentCodec.PATH_SEGMENT.encode(name);
  }
}
