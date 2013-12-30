package juzu.impl.plugin.amd;

import juzu.impl.plugin.asset.Asset;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Julien Viet
 */
public class ModuleAsset extends Asset {

  /** . */
  private final String adapter;

  /** . */
  private final LinkedHashMap<String, String> a;

  public ModuleAsset( Map<String, Serializable> asset, String adapter, List<String> aliases) {
    super("module", asset);

    //
    LinkedHashMap<String, String> a;
    if (depends != null && aliases != null) {
      a = new LinkedHashMap<String, String>();
      int size = Math.min(depends.size(), aliases.size());
      for (int i = 0;i < size;i++) {
        a.put(depends.get(i), aliases.get(i));
      }
    } else {
      a = null;
    }

    //
    depends.add("juzu.amd");

    //
    this.adapter = adapter;
    this.a = a;
  }

  @Override
  public InputStream filter(InputStream stream) throws IOException {

    if (a != null || adapter != null) {
      StringBuilder sb = new StringBuilder();

      // The define call
      sb.append("\ndefine('").append(id).append("', [");
      joinDependencies(sb);
      sb.append("], function(");
      joinParams(sb);
      sb.append(") {");

      // Redeclare here define
      // Note : this only work with
      // define(id,dependencies,factory)
      // it does not work with
      // define(?id,?dependencies,factory)
      // because we use 'arguments[2]'
      // so it should be done and tested
      sb.append("var define = function() {");
      sb.append("return arguments[2].apply(this, [");
      joinParams(sb);
      sb.append("]);");
      sb.append("};");

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
    if (a != null) {
      for (Iterator<String> i = a.keySet().iterator();i.hasNext();) {
        sb.append("'").append(i.next()).append("'");
        if (i.hasNext()) {
          sb.append(", ");
        }
      }
    }
  }

  private void joinParams(StringBuilder sb) {
    if (a != null) {
      for (Iterator<String> i = a.values().iterator();i.hasNext();) {
        sb.append(i.next());
        if (i.hasNext()) {
          sb.append(", ");
        }
      }
    }
  }
}
