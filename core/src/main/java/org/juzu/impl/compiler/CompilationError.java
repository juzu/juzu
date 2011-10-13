package org.juzu.impl.compiler;

import org.juzu.utils.Location;

import java.io.File;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class CompilationError
{

   /** . */
   private final String source;

   /** The optional source file. */
   private final File sourceFile;

   /** . */
   private final Location location;

   /** . */
   private final String message;

   public CompilationError(String source, File sourceFile, Location location, String message)
   {
      this.source = source;
      this.sourceFile = sourceFile;
      this.location = location;
      this.message = message;
   }

   public String getSource()
   {
      return source;
   }

   public Location getLocation()
   {
      return location;
   }

   public String getMessage()
   {
      return message;
   }

   public File getSourceFile()
   {
      return sourceFile;
   }

   @Override
   public String toString()
   {
      return "CompilationError[source=" + source +  ",message=" + message + ",location=" + location + "]";
   }
}
