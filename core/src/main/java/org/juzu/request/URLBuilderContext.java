package org.juzu.request;

import org.juzu.URLBuilder;
import org.juzu.application.Phase;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public interface URLBuilderContext
{

   URLBuilder createURLBuilder(Phase phase);

}
