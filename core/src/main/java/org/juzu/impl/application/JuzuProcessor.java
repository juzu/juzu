package org.juzu.impl.application;

import org.juzu.impl.compiler.Processor;
import org.juzu.impl.template.TemplateProcessor;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
@javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion.RELEASE_6)
@javax.annotation.processing.SupportedAnnotationTypes({"*"})
public class JuzuProcessor extends Processor
{
   public JuzuProcessor()
   {
      super(new ApplicationProcessor(), new TemplateProcessor());
   }
}
