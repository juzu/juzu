package juzu.impl.utils;

import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public interface ParameterMap extends Map<String, String[]>
{

   ParameterMap EMPTY = new EmptyParameterMap();

   /**
    * <p>Set a parameter on the URL that will be built by this builder. This method replaces the parameter with the given
    * name . A parameter value of <code>null</code> indicates that this parameter should be removed.</p>
    *
    * @param name the parameter name
    * @param value the parameter value
    * @throws NullPointerException if the name parameter is null
    */
   void setParameter(String name, String value) throws NullPointerException;

   /**
    * Set a parameter. This method replaces the parameter with the given name . An zero length parameter value
    * indicates that this parameter should be removed.
    *
    * The inserted value is cloned before its insertion in the map.
    *
    * @param name the parameter name
    * @param value the parameter value
    * @throws NullPointerException if the name parameter or the value parameter is null
    * @throws IllegalArgumentException if any component of the value is null
    */
   void setParameter(String name, String[] value) throws NullPointerException, IllegalArgumentException;

   /**
    * Set all parameters contained in the map. This method replaces the parameter with the given name . A parameter value
    * of with a zero length value array indicates that this parameter should be removed.
    *
    * Inserted values are cloned.
    *
    * @param parameters the parameters
    * @throws NullPointerException if the parameters argument is null
    * @throws IllegalArgumentException if any key, if any value in the map is null or contains a null element
    */
   void setParameters(Map<String, String[]> parameters) throws NullPointerException, IllegalArgumentException;

   /**
    * Redefines equals to implement equality on the String[] type.
    * 
    * @see Map#equals(Object)
    */
   boolean equals(Object o);
}
