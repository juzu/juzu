package org.juzu.impl.template;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class OffsetTokenManager extends TemplateParserTokenManager
{

   public OffsetTokenManager(SimpleCharStream stream)
   {
      super(stream);
   }

   protected Token jjFillToken()
   {
      Token t = super.jjFillToken();
      t.beginOffset = ((OffsetCharStream)input_stream).beginOffset-1;
      t.endOffset = ((OffsetCharStream)input_stream).currentOffset;
      return t;
   }

   public OffsetCharStream getStream()
   {
      return (OffsetCharStream)input_stream;
   }

}
