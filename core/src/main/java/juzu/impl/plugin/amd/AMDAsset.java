package juzu.impl.plugin.amd;

import juzu.impl.plugin.asset.Asset;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Julien Viet
 */
public class AMDAsset extends Asset {

  /** . */
  private final Map<String, String> aliases;

  /** . */
  private final String adapter;

  public AMDAsset(String id, String type, String value, List<String> depends, String location, String adapter, Map<String, String> aliases) {
    super(id, type, value, depends, location);

    //
    this.adapter = adapter;
    this.aliases = aliases;
  }

  @Override
  public InputStream filter(InputStream stream) throws IOException {

    StringBuilder sb = new StringBuilder();
    sb.append("\ndefine('").append(id).append("', [");
    joinDependencies(sb);

    sb.append("], function(");
    joinParams(sb);

    sb.append(") {\nvar require = Wrapper.require, requirejs = Wrapper.require,define = Wrapper.define;");
    sb.append("\nWrapper.define.names=[");
    joinDependencies(sb);
    sb.append("];");
    sb.append("\nWrapper.define.deps=[");
    joinParams(sb);
    sb.append("];");
    sb.append("\nreturn ");

    int idx = -1;
    if (adapter != null && !adapter.isEmpty()) {
      idx = adapter.indexOf("@{include}");
    }

    // start of adapter
    if (idx != -1) {
      sb.append(adapter.substring(0, idx)).append("\n");
    }

    NormalizeJSReader reader = new NormalizeJSReader(new InputStreamReader(stream));
    char[] buffer = new char[512];
    while (true) {
      int i = reader.read(buffer);
      if (i == 0) {
        continue;
      }
      if (i == -1) {
        break;
      }
      sb.append(buffer, 0, i);
    }

    // end of adapter
    if (idx != -1) {
      sb.append(adapter.substring(idx + "@{include}".length(), adapter.length()));
    }

    //
    sb.append("\n});");

    //
    return new ByteArrayInputStream(sb.toString().getBytes());
  }

  private void joinDependencies(StringBuilder sb) {
    for (Iterator<String> i = depends.iterator();i.hasNext();) {
      String depend = i.next();
      sb.append("'").append(depend).append("'");
      if (i.hasNext())
        sb.append(", ");
    }
  }

  private void joinParams(StringBuilder sb) {
    for (Iterator<String> i = depends.iterator();i.hasNext();) {
      String depend = i.next();
      String alias = aliases.get(depend);
      if (alias == null) {
        alias = depend;
      }
      sb.append(alias);
      if (i.hasNext())
        sb.append(", ");
    }
  }
}
