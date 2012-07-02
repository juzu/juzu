package juzu.impl.bridge;

import juzu.impl.application.ApplicationRuntime;
import juzu.impl.asset.AssetServer;
import juzu.impl.compiler.CompilationError;
import juzu.impl.fs.spi.ReadFileSystem;
import juzu.impl.fs.spi.classloader.ClassLoaderFileSystem;
import juzu.impl.utils.DevClassLoader;
import juzu.impl.utils.Logger;

import java.util.Collection;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class Bridge {

  /** . */
  public Logger log;

  /** . */
  public AssetServer server;

  /** . */
  public BridgeConfig config;

  /** . */
  public ReadFileSystem classes;

  /** . */
  public ReadFileSystem<?> resources;

  /** . */
  public ReadFileSystem<?> sourcePath;

  /** . */
  public ClassLoader classLoader;

  /** . */
  public ApplicationRuntime runtime;

  public Collection<CompilationError> boot() throws Exception {

    if (runtime == null) {
      if (config.prod) {
        ApplicationRuntime.Static<String, String> ss = new ApplicationRuntime.Static<String, String>(log);
        ss.setClasses(classes);
        ss.setClassLoader(Thread.currentThread().getContextClassLoader());

        //
        runtime = ss;
      }
      else {
        ClassLoaderFileSystem classPath = new ClassLoaderFileSystem(new DevClassLoader(Thread.currentThread().getContextClassLoader()));
        ApplicationRuntime.Dynamic dynamic = new ApplicationRuntime.Dynamic<String, String>(log);
        dynamic.init(classPath, sourcePath);

        //
        runtime = dynamic;
      }

      // Configure the runtime
      runtime.setResources(resources);
      runtime.setInjectImplementation(config.injectImpl);
      runtime.setName(config.appName);
      runtime.setAssetServer(server);
    }

    return runtime.boot();
  }
}
