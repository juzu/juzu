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
      List<String> chunks) {
    super(router);

    //
    if (chunks.size() != params.size() + 1) {
      throw new AssertionError("Was expecting chunk size " + chunks.size() + " to be equals to " + (params.size() + 1));
    }

    //
    String[] encodedChunks = new String[chunks.size()];
    for (int i = 0;i < chunks.size();i++) {
      encodedChunks[i] = PercentCodec.PATH_SEGMENT.encode(chunks.get(i));
    }

    //
    this.pattern = pattern;
    this.params = params.toArray(new PathParam[params.size()]);
    this.chunks = chunks.toArray(new String[chunks.size()]);
    this.encodedChunks = encodedChunks;
  }
}
