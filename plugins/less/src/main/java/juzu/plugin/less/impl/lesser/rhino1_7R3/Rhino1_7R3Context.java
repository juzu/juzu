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

package juzu.plugin.less.impl.lesser.rhino1_7R3;

import juzu.plugin.less.impl.lesser.JSContext;
import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.optimizer.ClassCompiler;
import org.mozilla.javascript.tools.shell.Global;

import java.util.HashMap;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class Rhino1_7R3Context extends JSContext
{

  /** . */
  private final Context ctx;

  /** . */
  private final Global global;

  /** . */
  private Scriptable scope;

  /** . */
  private Map<String, Class<?>> classCache;

  /** . */
  private Map<String, byte[]> codeCache;

  private final ClassLoader cl = new ClassLoader(Rhino1_7R3Context.class.getClassLoader())
  {
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException
    {
      Class<?> found = classCache.get(name);
      if (found == null)
      {
        byte[] code = codeCache.get(name);
        if (code != null)
        {
          found = defineClass(name, code, 0, code.length);
          classCache.put(name, found);
        }
      }
      if (found == null)
      {
        // Will throw CNFE
        found = super.findClass(name);
      }
      return found;
    }
  };

  public Rhino1_7R3Context()
  {
    Context ctx = Context.enter();
    try
    {
      ctx.setOptimizationLevel(1);
      ctx.setLanguageVersion(Context.VERSION_1_7);

      //
      Global global = new Global();
      global.init(ctx);

      this.global = global;
      this.ctx = ctx;
      this.scope = ctx.initStandardObjects(global);
      this.classCache = new HashMap<String, Class<?>>();
      this.codeCache = new HashMap<String, byte[]>();
    }
    finally
    {
      Context.exit();
    }
  }

  @Override
  public void put(String name, Object value)
  {
    scope.put(name, scope, value);
  }

  /** . */
  private int count = 0;

  @Override
  public Object eval(String script) throws Exception
  {
    Class<?> clazz = classCache.get(script);
    if (clazz == null)
    {
      String name = "script" + count++;
      CompilerEnvirons env = new CompilerEnvirons();
      env.setOptimizationLevel(0);
      ClassCompiler classCompiler = new ClassCompiler(env);
      Object[] ret = classCompiler.compileToClassFiles(script, name + ".js", 1, name);
      codeCache.put(name, (byte[])ret[1]);
      classCache.put(name, clazz = cl.loadClass(name));
    }
    Script sc = (Script)clazz.newInstance();
    Context.enter();
    Object o;
    try
    {
      o = sc.exec(ctx, scope);
      if (o instanceof NativeJavaObject)
      {
        o = ((NativeJavaObject)o).unwrap();
      }
    }
    finally
    {
      Context.exit();
    }
    return o;
  }

  @Override
  public Object invokeFunction(String name, Object... args) throws Exception
  {
    StringBuffer sb = new StringBuffer(name).append('(');
    for (int i = 0;i < args.length;i++)
    {
      Object arg = args[i];
      if (i > 0)
      {
        sb.append(',');
      }
      if (arg instanceof String)
      {
        sb.append('"').append(arg).append('"');
      }
      else
      {
        sb.append(arg);
      }
    }
    sb.append(')');
    Context.enter();
    Object o;
    try
    {
      o = ctx.evaluateString(scope, sb.toString(), "env.rhino.js", 1, null);
    }
    finally
    {
      Context.exit();
    }
    if (o instanceof NativeJavaObject)
    {
      o = ((NativeJavaObject)o).unwrap();
    }
    return o;
  }
}
