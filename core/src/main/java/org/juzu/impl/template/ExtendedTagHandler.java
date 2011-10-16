package org.juzu.impl.template;

import org.juzu.template.TagHandler;

import java.io.IOException;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class ExtendedTagHandler extends TagHandler
{

   public void process(ASTNode.Tag tag)
   {
   }

   public void compile(TemplateCompilationContext context, Map<String, String> args) throws IOException
   {
   }
}
