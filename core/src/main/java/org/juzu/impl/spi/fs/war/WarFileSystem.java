package org.juzu.impl.spi.fs.war;

import org.juzu.impl.spi.fs.ReadFileSystem;
import org.juzu.impl.utils.Content;
import org.juzu.impl.utils.Tools;

import javax.portlet.PortletContext;
import javax.servlet.ServletContext;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class WarFileSystem extends ReadFileSystem<String>
{

   /** . */
   private final String mountPoint;

   public WarFileSystem(String mountPoint) throws NullPointerException
   {
      if (mountPoint == null)
      {
         throw new NullPointerException("No null mount point accepted");
      }
      if (!mountPoint.startsWith("/") || !mountPoint.endsWith("/"))
      {
         throw new IllegalArgumentException("Invalid mount point " + mountPoint);
      }

      //
      this.mountPoint = mountPoint.substring(0, mountPoint.length() - 1);
   }

   @Override
   public boolean equals(String left, String right)
   {
      return left.equals(right);
   }

   @Override
   public String getRoot() throws IOException
   {
      return "/";
   }

   @Override
   public String getParent(String path) throws IOException
   {
      // It's a directory, remove the trailing '/'
      if (path.endsWith("/"))
      {
         path = path.substring(0, path.length() - 1);
      }

      // Get index of last '/'
      int index = path.lastIndexOf('/');

      //
      if (index == -1)
      {
         return null;
      }
      else
      {
      // Return the parent that ends with a '/'
      return path.substring(0, index + 1);
      }
   }

   @Override
   public String getName(String path) throws IOException
   {
      // It's a directory, remove the trailing '/'
      if (path.endsWith("/"))
      {
         path = path.substring(0, path.length() - 1);
      }

      // Get index of last '/'
      int index = path.lastIndexOf('/');

      // Return name
      return path.substring(index + 1);
   }

   @Override
   public Iterator<String> getChildren(String dir) throws IOException
   {
      return getResourcePaths(dir).iterator();
   }

   @Override
   public String getChild(String dir, String name) throws IOException
   {
      for (Iterator<String> i = getChildren(dir);i.hasNext();)
      {
         String child = i.next();
         String childName = getName(child);
         if (childName.equals(name))
         {
            return child;
         }
      }
      return null;
   }

   @Override
   public boolean isDir(String path) throws IOException
   {
      return path.endsWith("/");
   }

   @Override
   public boolean isFile(String path) throws IOException
   {
      return !isDir(path);
   }

   @Override
   public Content<?> getContent(String file) throws IOException
   {
      URL url = getResource(file);
      if (url != null)
      {
         URLConnection conn = url.openConnection();
         long lastModified = conn.getLastModified();
         InputStream in = conn.getInputStream();
         try
         {
            ByteArrayOutputStream content = new ByteArrayOutputStream();
            byte[] buffer = new byte[256];
            for (int l = in.read(buffer);l != -1;l = in.read(buffer))
            {
               content.write(buffer, 0, l);
            }
            return new Content.ByteArray(lastModified, content.toByteArray());
         }
         finally
         {
            Tools.safeClose(in);
         }
      }
      else
      {
         throw new UnsupportedOperationException("handle me gracefully");
      }
   }

   @Override
   public long getLastModified(String path) throws IOException
   {
      URL url = getResource(path);
      URLConnection conn = url.openConnection();
      return conn.getLastModified();
   }

   @Override
   public URL getURL(String path) throws IOException
   {
      return getResource(path);
   }

   protected abstract Set<String> doGetResourcePaths(String path) throws IOException;

   protected abstract URL doGetResource(String path) throws IOException;

   protected abstract String doGetRealPath(String path) throws IOException;

   private Collection<String> getResourcePaths(String path) throws IOException
   {
      Set<String> resourcePaths = doGetResourcePaths(mountPoint + path);
      if (resourcePaths != null)
      {
         ArrayList<String> tmp = new ArrayList<String>(resourcePaths.size());
         for (String resourcePath : resourcePaths)
         {
            tmp.add(resourcePath.substring(mountPoint.length()));
         }
         return tmp;
      }
      else
      {
         return Collections.emptyList();
      }
   }

   @Override
   public File getFile(String path) throws IOException
   {
      String realPath = doGetRealPath(mountPoint + path);
      return  realPath == null ? null : new File(realPath);
   }

   private URL getResource(String path) throws IOException
   {
      return doGetResource(mountPoint + path);
   }

   public static WarFileSystem create(ServletContext context)
   {
      return create(context, "/");
   }

   public static WarFileSystem create(final ServletContext servletContext, String mountPoint)
   {
      return new WarFileSystem(mountPoint)
      {
         @Override
         protected Set<String> doGetResourcePaths(String path) throws IOException
         {
            return servletContext.getResourcePaths(path);
         }
         @Override
         protected URL doGetResource(String path) throws IOException
         {
            return servletContext.getResource(path);
         }
         @Override
         protected String doGetRealPath(String path) throws IOException
         {
            return servletContext.getRealPath(path);
         }
      };
   }

   public static WarFileSystem create(PortletContext portletContext)
   {
      return create(portletContext, "/");
   }

   public static WarFileSystem create(final PortletContext portletContext, String mountPoint)
   {
      return new WarFileSystem(mountPoint)
      {
         @Override
         protected Set<String> doGetResourcePaths(String path) throws IOException
         {
            return portletContext.getResourcePaths(path);
         }
         @Override
         protected URL doGetResource(String path) throws IOException
         {
            return portletContext.getResource(path);
         }
         @Override
         protected String doGetRealPath(String path) throws IOException
         {
            return portletContext.getRealPath(path);
         }
      };
   }
}
