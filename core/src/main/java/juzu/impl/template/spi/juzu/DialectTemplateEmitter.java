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

package juzu.impl.template.spi.juzu;

import juzu.impl.template.spi.juzu.ast.SectionType;
import juzu.impl.common.Location;

import java.util.List;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class DialectTemplateEmitter {

  public abstract void open();

  public abstract void close();

  public abstract void openScriptlet(Location beginPosition);

  public abstract void appendScriptlet(String scriptlet);

  public abstract void closeScriptlet();

  public abstract void openExpression(Location beginPosition);

  public abstract void appendExpression(String expr);

  public abstract void closeExpression();

  public abstract void appendText(String text);

  public abstract void appendLineBreak(SectionType currentType, Location position);

  public abstract void url(String typeName, String methodName, List<String> args);

  public abstract void message(String key);

  public abstract void openTag(String className, Map<String, String> args);

  public abstract void closeTag(String tagName, Map<String, String> args);

  public abstract void tag(String tagName, Map<String, String> args);

}
