package org.juzu.plugin.less;

import org.junit.Test;
import org.juzu.impl.compiler.Compiler;
import org.juzu.impl.spi.inject.InjectImplementation;
import org.juzu.test.AbstractInjectTestCase;
import org.juzu.test.CompilerAssert;

import java.io.File;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class LessPluginTestCase extends AbstractInjectTestCase
{

   public LessPluginTestCase(InjectImplementation di)
   {
      super(di);
   }

   @Test
   public void testCompile()  throws Exception
   {
      CompilerAssert<File,File> ca = compiler("plugin", "less");
      Compiler compiler = ca.assertCompile();
//      ca.getClassOutput().getPath();
   }
}
