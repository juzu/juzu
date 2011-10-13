package org.juzu.impl.request;

import org.juzu.Response;
import org.juzu.application.Phase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class ActionContext extends RequestContext
{

   public ActionContext(ClassLoader classLoader)
   {
      super(classLoader);
   }

   @Override
   public Phase getPhase()
   {
      return Phase.ACTION;
   }

   public void map(Response response, ControllerMethod method)
   {
      List<ControllerParameter> annotationParameters = method.getAnnotationParameters();
      for (int i = 0;i < annotationParameters.size();i++)
      {
         ControllerParameter annotationParameter = annotationParameters.get(i);
         response.setParameter(annotationParameter.getName(), annotationParameter.getValue());
      }
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

   public abstract Response createResponse();
}
