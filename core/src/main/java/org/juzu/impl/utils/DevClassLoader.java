package org.juzu.impl.utils;

import java.net.URL;

/**
 * <p></p>The <code>DevClassLoader</code> blacklists any class from found in the <code>/WEB-INF/classes</code> folder and
 * instead throws a {@link ClassNotFoundException} to the caller, forcing the caller to load the class by itself.</p>
 *
 * <p>At the moment it only supports unpacked war files.</p>
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class DevClassLoader extends ClassLoader
{

   public DevClassLoader(ClassLoader parent)
   {
      super(parent);
   }

   @Override
   protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException
   {
      Class<?> found = super.loadClass(name, resolve);

      //
      if (found.getClassLoader() == super.getParent())
      {
         String classPath = name.replace('.', '/') + ".class";
         URL url = getResource(classPath);

         // Unwrap until we get the file location
         String protocol = url.getProtocol();
         if ("file".equals(protocol))
         {
            String path = url.getPath();
            if (path.endsWith("/WEB-INF/classes/" + classPath))
            {
               throw new ClassNotFoundException();
            }
         }
         else if ("jar".equals(protocol))
         {
            String path = url.getPath();
            int index = path.indexOf("!/");
            String nested = path.substring(0, index);
            if (nested.endsWith(".jar"))
            {
               // Return found
            }
            else
            {
               throw new UnsupportedOperationException("handle me gracefully " + url);
            }
         }
         else
         {
            throw new UnsupportedOperationException("handle me gracefully " + url);
         }
      }

      //
      return found;
   }
}
