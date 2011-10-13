package org.juzu.impl.request;

import org.juzu.Response;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public interface ActionBridge extends RequestBridge
{

   Response createResponse();

}
