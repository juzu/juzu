package org.juzu.template;

import org.juzu.template.TagHandler;

import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class TagFrame
{

   /** . */
   final TagHandler tag;

   /** . */
   final Map<String, String> args;

   TagFrame(TagHandler tag, Map<String, String> args)
   {
      this.tag = tag;
      this.args = args;
   }
}
