package org.juzu.impl.request;

import org.juzu.Response;
import org.juzu.application.Phase;

import java.util.List;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public final class ActionContext extends RequestContext<ActionBridge>
{

   public ActionContext(ClassLoader classLoader, ActionBridge bridge)
   {
      super(classLoader, bridge);
   }

   @Override
   public Phase getPhase()
   {
      return Phase.ACTION;
   }

   public void map(Response response, ControllerMethod method)
   {
      response.setParameter("op", method.getName());
   }

   public Response createResponse(ControllerMethod method)
   {
      Response response = createResponse();
      map(response, method);
      return response;
   }

   public Response createResponse(ControllerMethod method, Object arg)
   {
      Response response = createResponse();
      map(response, method);
      List<ControllerParameter> argumentParameters = method.getArgumentParameters();
      if (arg != null)
      {
         response.setParameter(argumentParameters.get(0).getName(), arg.toString());
      }
      return response;
   }

   public Response createResponse(ControllerMethod method, Object[] args)
   {
      Response response = createResponse();
      map(response, method);
      List<ControllerParameter> argumentParameters = method.getArgumentParameters();
      for (int i = 0;i < argumentParameters.size();i++)
      {
         Object value = args[i];
         if (value != null)
         {
            ControllerParameter argParameter = argumentParameters.get(i);
            response.setParameter(argParameter.getName(), value.toString());
         }
      }
      return response;
   }

   @Override
   public Map<Object, Object> getContext(Scope scope)
   {
      switch (scope)
      {
         case FLASH:
            return bridge.getFlashContext();
         case ACTION:
         case REQUEST:
            return bridge.getRequestContext();
         case RENDER:
         case RESOURCE:
         case MIME:
            return null;
         case SESSION:
            return bridge.getSessionContext();
         case IDENTITY:
            return bridge.getIdentityContext();
         default:
            throw new AssertionError();
      }
   }

   public Response createResponse()
   {
      return bridge.createResponse();
   }
}
