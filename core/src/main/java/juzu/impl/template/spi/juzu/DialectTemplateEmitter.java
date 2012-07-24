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

package juzu.impl.template.spi.juzu;

import juzu.impl.template.spi.juzu.ast.SectionType;
import juzu.impl.common.Location;

import java.util.List;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class DialectTemplateEmitter {

  public abstract void startScriptlet(Location beginPosition);

  public abstract void appendScriptlet(String scriptlet);

  public abstract void endScriptlet();

  public abstract void startExpression(Location beginPosition);

  public abstract void appendExpression(String expr);

  public abstract void endExpression();

  public abstract void appendText(String text);

  public abstract void appendLineBreak(SectionType currentType, Location position);

  public abstract void url(String typeName, String methodName, List<String> args);

  public abstract void openTag(String className, Map<String, String> args);

  public abstract void closeTag(String tagName, Map<String, String> args);

  public abstract void tag(String tagName, Map<String, String> args);

}
