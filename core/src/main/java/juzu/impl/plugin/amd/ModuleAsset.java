package juzu.impl.plugin.amd;

import juzu.impl.plugin.asset.Asset;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author Julien Viet
 */
public class ModuleAsset extends Asset {

  /** . */
  private final List<String> aliases;

  /** . */
  private final String adapter;

  public ModuleAsset( Map<String, Serializable> asset, String adapter, List<String> aliases) {
    super("module", asset);

    //
    this.adapter = adapter;
    this.aliases = aliases;
  }

  @Override
  public InputStream filter(InputStream stream) throws IOException {

    if ((depends != null && aliases != null) || adapter != null) {
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
    } else {
      return stream;
    }
  }

  private void joinDependencies(StringBuilder sb) {
    if (depends != null && aliases != null) {
      int size = Math.min(depends.size(), aliases.size());
      for (int i = 0;i < size;i++) {
        if (i > 0) {
          sb.append(", ");
        }
        String depend = depends.get(i);
        sb.append("'").append(depend).append("'");
      }
    }
  }

  private void joinParams(StringBuilder sb) {
    if (depends != null && aliases != null) {
      int size = Math.min(depends.size(), aliases.size());
      for (int i = 0;i < size;i++) {
        if (i > 0) {
          sb.append(", ");
        }
        String alias = aliases.get(i);
        sb.append(alias);
      }
    }
  }
}
