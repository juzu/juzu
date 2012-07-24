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

package juzu.impl.template.spi.juzu.compiler;

import juzu.impl.template.spi.juzu.DialectTemplateEmitter;
import juzu.impl.template.spi.juzu.ast.SectionType;
import juzu.impl.common.Location;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class EmitterContext {

  /** . */
  private SectionType currentType = null;

  /** . */
  private StringBuilder accumulatedText = new StringBuilder();

  /** . */
  final DialectTemplateEmitter writer;

  EmitterContext(DialectTemplateEmitter writer) {
    this.writer = writer;
  }

  void begin(SectionType sectionType, Location pos) {
    if (sectionType == null) {
      throw new NullPointerException();
    }
    if (pos == null) {
      throw new NullPointerException();
    }
    if (currentType != null) {
      throw new IllegalStateException();
    }
    this.currentType = sectionType;

    //
    switch (currentType) {
      case STRING:
        break;
      case SCRIPTLET:
        writer.startScriptlet(pos);
        break;
      case EXPR:
        writer.startExpression(pos);
        break;
    }
  }

  void appendText(String text) {
    switch (currentType) {
      case STRING:
        accumulatedText.append(text);
        break;
      case SCRIPTLET:
        writer.appendScriptlet(text);
        break;
      case EXPR:
        writer.appendExpression(text);
        break;
    }
  }

  void appendLineBreak(Location position) {
    switch (currentType) {
      case STRING:
        accumulatedText.append("\n");
        break;
      case SCRIPTLET:
        writer.appendLineBreak(currentType, position);
        break;
      case EXPR:
        writer.appendLineBreak(currentType, position);
        break;
    }
  }

  void end() {
    if (currentType == null) {
      throw new IllegalStateException();
    }

    //
    switch (currentType) {
      case STRING:
        if (accumulatedText.length() > 0) {
          writer.appendText(accumulatedText.toString());
          accumulatedText.setLength(0);
        }
        break;
      case SCRIPTLET:
        writer.endScriptlet();
        break;
      case EXPR:
        writer.endExpression();
        break;
    }

    //
    this.currentType = null;
  }
}
