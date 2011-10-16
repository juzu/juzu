package org.juzu.impl.tags;

import org.juzu.impl.spi.template.TemplateStub;
import org.juzu.impl.template.ASTNode;
import org.juzu.impl.template.ExtendedTagHandler;
import org.juzu.impl.template.TemplateCompilationContext;
import org.juzu.template.Body;
import org.juzu.template.TemplateRenderContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class DecorateTag extends ExtendedTagHandler
{

   /** . */
   static final ThreadLocal<Body> current = new ThreadLocal<Body>();

   @Override
   public void process(ASTNode.Tag tag)
   {
      ASTNode current =  tag;
      while (true)
      {
         if (current instanceof ASTNode.Block)
         {
            current = ((ASTNode.Block)current).getParent();
         }
         else
         {
            break;
         }
      }

      //
      ASTNode.Template template = (ASTNode.Template)current;
      for (ASTNode.Block child : new ArrayList<ASTNode.Block<?>>(template.getChildren()))
      {
         if (child != tag)
         {
            tag.addChild(child);
         }
      }

      //
      if (tag.getParent() != template)
      {
         template.addChild(tag);
      }
   }

   @Override
   public void compile(TemplateCompilationContext context, Map<String, String> args) throws IOException
   {
      String path = args.get("path");
      context.resolveTemplate(path);
   }

   @Override
   public void render(TemplateRenderContext context, Body body, Map<String, String> args) throws IOException
   {
      current.set(body);
      try
      {
         String path = args.get("path");
         TemplateStub template = context.resolveTemplate(path);
         template.render(context);
      }
      finally
      {
         current.set(null);
      }
   }
}
