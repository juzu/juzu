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

import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
class PatternRoute extends Route {

  /** . */
  final RERef pattern;

  /** . */
  final PathParam[] params;

  /** . */
  final String[] chunks;

  /** The encoded chunks (so we don't reencode them later). */
  final String[] encodedChunks;

  PatternRoute(
      Router router,
      RERef pattern,
      List<PathParam> params,
      List<String> chunks,
      int terminal) {
    super(router, terminal);

    //
    if (chunks.size() != params.size() + 1) {
      throw new AssertionError("Was expecting chunk size " + chunks.size() + " to be equals to " + (params.size() + 1));
    }

    //
    String[] encodedChunks = new String[chunks.size()];
    for (int i = 0;i < chunks.size();i++) {
      encodedChunks[i] = PercentCodec.PATH_SEGMENT.encodeSequence(chunks.get(i));
    }

    //
    this.pattern = pattern;
    this.params = params.toArray(new PathParam[params.size()]);
    this.chunks = chunks.toArray(new String[chunks.size()]);
    this.encodedChunks = encodedChunks;
  }
}
