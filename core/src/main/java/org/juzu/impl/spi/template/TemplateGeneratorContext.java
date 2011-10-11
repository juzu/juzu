package org.juzu.impl.spi.template;

import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public interface TemplateGeneratorContext
{

   MethodInvocation resolveMethodInvocation(String name, Map<String, String> parameterMap);

}
