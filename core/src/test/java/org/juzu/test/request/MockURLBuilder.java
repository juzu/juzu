package org.juzu.test.request;

import org.json.JSONException;
import org.json.JSONObject;
import org.juzu.URLBuilder;
import org.juzu.application.Phase;
import org.juzu.test.AbstractTestCase;

import java.util.HashMap;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class MockURLBuilder implements URLBuilder
{

   /** . */
   private final Phase phase;

   /** . */
   private final Map<String, String> parameters;

   public MockURLBuilder(Phase phase)
   {
      this.phase = phase;
      this.parameters = new HashMap<String, String>();
   }

   public URLBuilder setParameter(String name, String value)
   {
      if (name == null)
      {
         throw new NullPointerException();
      }
      if (value == null)
      {
         throw new NullPointerException();
      }
      parameters.put(name, value);
      return this;
   }

   @Override
   public String toString()
   {
      try
      {
         JSONObject url = new JSONObject();
         url.put("phase", phase);
         url.put("parameters", parameters);
         return url.toString();
      }
      catch (JSONException e)
      {
         throw AbstractTestCase.failure(e);
      }
   }
}
