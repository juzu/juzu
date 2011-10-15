package org.juzu.impl.request;

import org.juzu.URLBuilder;
import org.juzu.application.Phase;
import org.juzu.text.Printer;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public interface MimeBridge extends RequestBridge
{

   URLBuilder createURLBuilder(Phase phase);

   Printer getPrinter();

}
