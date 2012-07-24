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

package juzu.plugin.less.impl.lesser;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class JSR223Context extends JSContext {

  /** . */
  private final ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");

  @Override
  public void put(String name, Object value) {
    engine.put(name, value);
  }

  @Override
  public Object eval(String script) throws Exception {
    return engine.eval(script);
  }

  @Override
  public Object invokeFunction(String name, Object... args) throws Exception {
    return ((Invocable)engine).invokeFunction(name, args);
  }
}
