package juzu.impl.utils;

import java.io.Serializable;
import java.util.Iterator;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class Path implements Serializable, Iterable<String>
{

   public static Path create(boolean absolute, QN qn, String name, String extension)
   {
      return absolute ? new Absolute(null, new FQN(qn, name), extension) : new Relative(null, new FQN(qn, name), extension);
   }

   public static Path parse(String path) throws NullPointerException, IllegalArgumentException
   {
      boolean absolute = path.length() > 0 && path.charAt(0) == '/';
      String[] dirs = parse(path, 0, 0);
      int len = dirs.length - 2;
      QN qn;
      if (len == 0)
      {
         qn = QN.EMPTY;
      }
      else if (len == 1)
      {
         qn = new QN(dirs[0], dirs, 1);
      }
      else
      {
         StringBuilder sb = new StringBuilder();
         for (int i = 0;i < len;i++)
         {
            if (i > 0)
            {
               sb.append('.');
            }
            sb.append(dirs[i]);
         }
         qn = new QN(sb.toString(), dirs, len);
      }
      FQN fqn = new FQN(qn, dirs[len]);
      return absolute ? new Absolute(path, fqn, dirs[dirs.length - 1]) : new Relative(path, fqn, dirs[dirs.length - 1]);
   }

   private static String[] parse(String path, int from, int size)
   {
      int len = path.length();
      if (from < len)
      {
         int pos = path.indexOf('/', from);
         if (pos == -1)
         {
            int cur = len - 1;
            while (cur >= from)
            {
               char c = path.charAt(cur);
               if (c == '.')
               {
                  if (cur - from < 1)
                  {
                     throw new IllegalArgumentException();
                  }
                  if (len - cur < 2)
                  {
                     throw new IllegalArgumentException();
                  }
                  String[] ret = new String[size + 2];
                  ret[size] = path.substring(from, cur);
                  ret[size + 1] = path.substring(cur + 1);
                  return ret;
               }
               else
               {
                  cur--;
               }
            }
            String[] ret = new String[size + 2];
            ret[size] = path.substring(from);
            return ret;
         }
         else if (from == pos)
         {
            return parse(path, from + 1, size);
         }
         else
         {
            String[] ret = parse(path, pos + 1, size + 1);
            ret[size] = path.substring(from, pos);
            return ret;
         }
      }
      else
      {
         String[] ret = new String[size + 2];
         ret[size] = "";
         return ret;
      }
   }

   /** . */
   protected final FQN fqn;

   /** . */
   private String canonical;

   /** . */
   private String value;

   /** . */
   private final String ext;

   /** . */
   private String name;

   private Path(String value, FQN fqn, String ext)
   {
      this.fqn = fqn;
      this.canonical = null;
      this.value = value;
      this.ext = ext;
      this.name = null;
   }

   public Iterator<String> iterator()
   {
      return fqn.iterator();
   }

   public String getValue()
   {
      if (value == null)
      {
         return getCanonical();
      }
      else
      {
         return value;
      }
   }

   public abstract boolean isAbsolute();

   public QN getQN()
   {
      return fqn.getPackageName();
   }

   public FQN getFQN()
   {
      return fqn;
   }

   public String getRawName()
   {
      return fqn.getSimpleName();
   }

   public String getExt()
   {
      return ext;
   }

   public String getName()
   {
      if (name == null)
      {
         if (ext != null)
         {
            name = fqn.getSimpleName() + "." + ext;
         }
         else
         {
            name = fqn.getSimpleName();
         }
      }
      return name;
   }

   public abstract Path as(String ext);

   public String getCanonical()
   {
      if (canonical == null)
      {
         StringBuilder sb = new StringBuilder();
         if (isAbsolute())
         {
            sb.append('/');
         }
         for (int i = 0;i < fqn.size();i++)
         {
            if (i > 0)
            {
               sb.append('/');
            }
            sb.append(fqn.get(i));
         }
         if (ext != null)
         {
            sb.append('.').append(ext);
         }
         canonical = sb.toString();
      }
      return canonical;
   }

   @Override
   public int hashCode()
   {
      return fqn.hashCode() ^ (ext != null ? ext.hashCode() : 0);
   }

   @Override
   public boolean equals(Object obj)
   {
      if (obj == this)
      {
         return true;
      }
      if (obj.getClass()== getClass())
      {
         Path that = (Path)obj;
         return fqn.equals(that.fqn) && Tools.safeEquals(ext, that.ext);
      }
      return false;
   }

   @Override
   public String toString()
   {
      return "Path[absolute=" + isAbsolute() + ",fqn=" + fqn + ",extension" + ext +  "]";
   }

   public static class Absolute extends Path
   {

      public static Absolute create(QN qn, String rawName, String ext)
      {
         return new Absolute(null, new FQN(qn, rawName), ext);
      }

      private Absolute(String value, FQN fqn, String extension)
      {
         super(value, fqn, extension);
      }

      @Override
      public Absolute as(String ext)
      {
         return new Absolute(null, fqn, ext);
      }

      @Override
      public boolean isAbsolute()
      {
         return true;
      }
   }

   public static class Relative extends Path
   {

      public static Relative create(QN qn, String name, String extension)
      {
         return new Relative(null, new FQN(qn, name), extension);
      }

      private Relative(String value, FQN fqn, String extension)
      {
         super(value, fqn, extension);
      }

      @Override
      public Relative as(String ext)
      {
         return new Relative(null, fqn, ext);
      }

      @Override
      public boolean isAbsolute()
      {
         return false;
      }
   }
}
