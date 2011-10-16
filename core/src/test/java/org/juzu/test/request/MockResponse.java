package org.juzu.test.request;

import org.juzu.Response;
import org.juzu.impl.utils.Builder;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class MockResponse implements Response
{

   /** . */
   private final Builder.Map<String, String> parameters;

   public MockResponse()
   {
      this.parameters = new Builder.Map<String, String>();
   }

   public void setParameter(String parameterName, String parameterValue)
   {
      if (parameterName == null)
      {
         throw new NullPointerException();
      }
      if (parameterValue == null)
      {
         throw new NullPointerException();
      }
      parameters.put(parameterName, parameterValue);
   }
}
