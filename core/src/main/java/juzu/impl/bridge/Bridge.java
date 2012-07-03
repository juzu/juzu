package juzu.impl.bridge;

import juzu.Response;
import juzu.impl.application.ApplicationException;
import juzu.impl.application.ApplicationRuntime;
import juzu.impl.asset.AssetServer;
import juzu.impl.bridge.spi.ActionBridge;
import juzu.impl.bridge.spi.RenderBridge;
import juzu.impl.bridge.spi.ResourceBridge;
import juzu.impl.compiler.CompilationError;
import juzu.impl.fs.spi.ReadFileSystem;
import juzu.impl.fs.spi.classloader.ClassLoaderFileSystem;
import juzu.impl.utils.DevClassLoader;
import juzu.impl.utils.Logger;
import juzu.impl.utils.Tools;
import juzu.impl.utils.TrimmingException;
import juzu.portlet.JuzuPortlet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

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

  public void processAction(final ActionBridge requestBridge) throws Throwable {
    try {
      TrimmingException.invoke(new TrimmingException.Callback() {
        public void call() throws Throwable {
          try {
            runtime.getContext().invoke(requestBridge);
          }
          catch (ApplicationException e) {
            // For now we do that until we find something better specially for the dev mode
            throw e.getCause();
          }
        }
      });
    }
    catch (TrimmingException e) {
      throw e.getSource();
    }
    finally {
      requestBridge.close();
    }
  }

  public void render(final RenderBridge requestBridge) throws Throwable {

    //
    Collection<CompilationError> errors = boot();

    //
    if (errors == null || errors.isEmpty()) {

      //
      if (errors != null) {
        requestBridge.purgeSession();
      }


      //
      try {
        TrimmingException.invoke(new TrimmingException.Callback() {
          public void call() throws Throwable {
            try {
              runtime.getContext().invoke(requestBridge);
            }
            catch (ApplicationException e) {
              throw e.getCause();
            }
          }
        });
      }
      catch (TrimmingException e) {
        if (config.prod) {
          throw e.getSource();
        }
        else {
          StringWriter writer = new StringWriter();
          PrintWriter printer = new PrintWriter(writer);
          renderThrowable(printer, e);
          requestBridge.setResponse(Response.ok(writer.getBuffer()));
        }
      } finally {
        requestBridge.close();
      }
    }
    else {
      try {
        StringWriter writer = new StringWriter();
        PrintWriter printer = new PrintWriter(writer);
        renderErrors(printer, errors);
        requestBridge.setResponse(Response.ok(writer.getBuffer()));
      }
      finally {
        requestBridge.close();
      }
    }
  }

  public void serveResource(final ResourceBridge requestBridge) throws Throwable{
    try {
      TrimmingException.invoke(new TrimmingException.Callback() {
        public void call() throws Throwable {
          try {
            runtime.getContext().invoke(requestBridge);
          }
          catch (ApplicationException e) {
            throw e.getCause();
          }
        }
      });
    }
    catch (TrimmingException e) {

      //
      logThrowable(e);

      // Internal server error
      Response response;
      if (!config.prod) {
        StringWriter writer = new StringWriter();
        PrintWriter printer = new PrintWriter(writer);
        printer.print("<html>\n");
        printer.print("<head>\n");
        printer.print("</head>\n");
        printer.print("<body>\n");
        renderThrowable(printer, e);
        printer.print("</body>\n");
        response = Response.content(500, writer.getBuffer());
      } else {
        response = Response.content(500, "todo");
      }

      // Set response
      requestBridge.setResponse(response);
    } finally {
      requestBridge.close();
    }
  }

  private void logThrowable(Throwable t) {
    log.log(t.getMessage(), t);
  }

  private void renderThrowable(PrintWriter writer, Throwable t) throws IOException {
    // Trim the stack trace to remove stuff we don't want to see
    int size = 0;
    StackTraceElement[] trace = t.getStackTrace();
    for (StackTraceElement element : trace) {
      if (element.getClassName().equals(JuzuPortlet.class.getName())) {
        break;
      }
      else {
        size++;
      }
    }
    StackTraceElement[] ourTrace = new StackTraceElement[size];
    System.arraycopy(trace, 0, ourTrace, 0, ourTrace.length);
    t.setStackTrace(ourTrace);

    //
    sendJuzuCSS(writer);

    // We hack a bit
    final AtomicBoolean open = new AtomicBoolean(false);
    PrintWriter formatter = new PrintWriter(writer) {
      @Override
      public void println(Object x) {
        if (open.get()) {
          super.append("</ul></pre>");
        }
        super.append("<div class=\"juzu-message\">");
        super.append(String.valueOf(x));
        super.append("</div>");
        open.set(false);
      }

      @Override
      public void println(String x) {
        if (!open.get()) {
          super.append("<pre><ul>");
          open.set(true);
        }
        super.append("<li><span>");
        super.append(x);
        super.append("</span></li>");
      }

      @Override
      public void println() {
        // Do nothing
      }
    };

    //
    writer.append("<div class=\"juzu\">");
    writer.append("<div class=\"juzu-box\">");

    // We hack a bit with our formatter
    t.printStackTrace(formatter);

    //
    if (open.get()) {
      writer.append("</ul></pre>");
    }

    //
    writer.append("</div>");
    writer.append("</div>");
  }

  private void sendJuzuCSS(PrintWriter writer) throws IOException {
    // Get CSS
    URL cssURL = JuzuPortlet.class.getResource("juzu.css");
    String css = Tools.read(cssURL);
    css = css.replace("\"", "\\\"");
    css = css.replace("'", "\\'");
    css = css.replace("\n", "\\n");

    //
    writer.append("<script type='text/javascript'>\n");
    writer.append("var styleElement = document.createElement('style');\n");
    writer.append("var css = '");
    writer.append(css);
    writer.append("';\n");
    writer.append("styleElement.type = 'text/css';\n");
    writer.append("if (styleElement.styleSheet) {;\n");
    writer.append("styleElement.styleSheet.cssText = css;\n");
    writer.append("} else {\n");
    writer.append("styleElement.appendChild(document.createTextNode(css));\n");
    writer.append("}\n");
    writer.append("document.getElementsByTagName(\"head\")[0].appendChild(styleElement);\n");
    writer.append("</script>\n");
  }

  private void renderErrors(PrintWriter writer, Collection<CompilationError> errors) throws IOException {
    sendJuzuCSS(writer);

    //
    writer.append("<div class=\"juzu\">");
    for (CompilationError error : errors) {
      writer.append("<div class=\"juzu-box\">");
      writer.append("<div class=\"juzu-message\">").append(error.getMessage()).append("</div>");

      // Display the source code
      File source = error.getSourceFile();
      if (source != null) {
        int line = error.getLocation().getLine();
        int from = line - 2;
        int to = line + 3;
        BufferedReader reader = new BufferedReader(new FileReader(source));
        int count = 1;
        writer.append("<pre><ol start=\"").append(String.valueOf(from)).append("\">");
        for (String s = reader.readLine();s != null;s = reader.readLine()) {
          if (count >= from && count < to) {
            if (count == line) {
              writer.append("<li><span class=\"error\">").append(s).append("</span></li>");
            }
            else {
              writer.append("<li><span>").append(s).append("</span></li>");
            }
          }
          count++;
        }
        writer.append("</ol></pre>");
      }
      writer.append("</div>");
    }
    writer.append("</div>");
  }
}
