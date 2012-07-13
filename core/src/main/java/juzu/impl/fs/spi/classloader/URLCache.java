package juzu.impl.fs.spi.classloader;

import juzu.impl.common.Tools;
import juzu.impl.common.Trie;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class URLCache {

  /** . */
  private final Trie<String, URL> root;

  public URLCache() {
    this.root = new Trie<String, URL>();
  }

  void add(ClassLoader classLoader) throws IOException {
    // Get the root class path files
    for (Enumeration<URL> i = classLoader.getResources("");i.hasMoreElements();) {
      URL url = i.nextElement();
      if (url.getProtocol().equals("file")) {
        try {
          File f = new File(url.toURI());
          if (f.isDirectory()) {
            add(root, f);
          }
          else {
            // WTF ?
          }
        }
        catch (URISyntaxException e) {
          throw new IOException(e);
        }
      }
    }

    //
    ArrayList<URL> items = Collections.list(classLoader.getResources("META-INF/MANIFEST.MF"));
    for (URL item : items) {
      if ("jar".equals(item.getProtocol())) {
        String path = item.getPath();
        int pos = path.indexOf("!/");
        URL url = new URL(path.substring(0, pos));
        add(root, url);
      }
    }
  }

  void add(Class<?> clazz) throws IOException {
    URL inject = clazz.getClassLoader().getResource("javax/inject/Inject.class");
    if (inject != null) {
      if ("jar".equals(inject.getProtocol())) {
        String path = inject.getPath();
        int pos = path.indexOf("!/");
        URL url = new URL(path.substring(0, pos));
        add(root, url);
      }
    }
  }

  Trie<String, URL> get(Iterable<String> names) throws IOException {
    return root.get(names.iterator());
  }

  private static void add(Trie<String, URL> root, URL url) throws IOException {
    if ("file".equals(url.getProtocol())) {
      // The fast way (but that requires a File object)
      try {
        File f = new File(url.toURI());
        ZipFile jarFile = new ZipFile(f);
        for (Enumeration<? extends ZipEntry> en = jarFile.entries();en.hasMoreElements();) {
          ZipEntry entry = en.nextElement();
          add(root, url, entry);
        }
      }
      catch (URISyntaxException e1) {
        throw new IOException("Could not access jar file " + url, e1);
      }
    }
    else {
      // The slow way
      ZipInputStream in = new ZipInputStream(url.openStream());
      try {
        for (ZipEntry jarEntry = in.getNextEntry();jarEntry != null;jarEntry = in.getNextEntry()) {
          add(root, url, jarEntry);
        }
      }
      finally {
        Tools.safeClose(in);
      }
    }
  }

  private static void add(Trie<String, URL> trie, File f) throws IOException {
    for (File file : f.listFiles()) {
      Trie<String, URL> child = trie.add(file.getName());
      if (file.isDirectory()) {
        add(child, file);
      }
      else {
        child.value(file.toURI().toURL());
      }
    }
  }

  private static void add(Trie<String, URL> root, URL baseURL, ZipEntry entry) throws IOException {
    String name = entry.getName();
    if (!entry.isDirectory()) {
      String[] names = Tools.split(name, '/');
      Trie<String, URL> trie = root.add(names);
      URL entryURL = new URL("jar:" + baseURL + "!/" + name);
      trie.value(entryURL);
    }
  }
}
