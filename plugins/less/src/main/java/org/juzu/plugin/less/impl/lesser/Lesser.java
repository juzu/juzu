package org.juzu.plugin.less.impl.lesser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class Lesser
{

   /** . */
   private static final ThreadLocal<LessContext> current = new ThreadLocal<LessContext>();

   /** . */
   private final JSContext engine;

   public class Loader
   {
      public String load(String name)
      {
         return current.get().load(name);
      }
   }

   public Lesser(JSContext jsContext) throws Exception
   {
      InputStream lessIn = getClass().getResourceAsStream("less.js");

      //
      ByteArrayOutputStream baos = append(lessIn, new ByteArrayOutputStream());

      //
      jsContext.put("loader", new Loader());
      jsContext.eval("load = function(name) { return '' + loader.load(name); }");
      jsContext.eval("failure = function(line, column, index, message, type, extract) { return new " + Failure.class.getName() + "(line, column, index, message, type, extract); }");
      jsContext.eval("compilation = function(stylesheet) { return new " + Compilation.class.getName() + "(stylesheet); }");
      jsContext.put("window", "{}");

      //
      jsContext.eval(baos.toString());

      //
      this.engine = jsContext;
   }

   public Result compile(LessContext context, String name) throws Exception
   {
      return compile(context, name, false);
   }

   public Result compile(LessContext context, String name, boolean compress) throws Exception
   {
      current.set(context);
      try
      {
         return (Result)engine.invokeFunction("parse", name, compress);
      }
      finally
      {
         current.set(null);
      }
   }

   static <O extends OutputStream> O append(InputStream in, O out) throws IOException
   {
      byte[] buffer = new byte[256];
      for (int l = in.read(buffer);l != -1;l = in.read(buffer))
      {
         out.write(buffer, 0, l);
      }
      return out;
   }
}
