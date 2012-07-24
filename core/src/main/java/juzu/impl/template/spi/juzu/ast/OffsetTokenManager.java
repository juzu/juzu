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

package juzu.impl.template.spi.juzu.ast;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class OffsetTokenManager extends TemplateParserTokenManager {

  public OffsetTokenManager(SimpleCharStream stream) {
    super(stream);
  }

  protected Token jjFillToken() {
    Token t = super.jjFillToken();
    t.beginOffset = ((OffsetCharStream)input_stream).beginOffset - 1;
    t.endOffset = ((OffsetCharStream)input_stream).currentOffset;
    return t;
  }

  public OffsetCharStream getStream() {
    return (OffsetCharStream)input_stream;
  }

}
