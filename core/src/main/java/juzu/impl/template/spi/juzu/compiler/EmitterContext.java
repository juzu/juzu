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

  void open() {
    writer.open();
  }

  void close() {
    writer.close();
  }

  void openSection(SectionType sectionType, Location pos) {
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
        writer.openScriptlet(pos);
        break;
      case EXPR:
        writer.openExpression(pos);
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

  void closeSection() {
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
        writer.closeScriptlet();
        break;
      case EXPR:
        writer.closeExpression();
        break;
    }

    //
    this.currentType = null;
  }
}
