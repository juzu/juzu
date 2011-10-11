package org.juzu.impl.apt;

import org.juzu.impl.application.ApplicationProcessor;
import org.juzu.impl.template.TemplateProcessor;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
@javax.annotation.processing.SupportedAnnotationTypes({"*"})
public class JuzuProcessor extends Processor
{
   public JuzuProcessor()
   {
      super(new ApplicationProcessor(), new TemplateProcessor());
   }
}
