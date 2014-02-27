package juzu.impl.plugin.amd;

import juzu.impl.common.Tools;
import juzu.impl.plugin.asset.Asset;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.StringWriter;
import java.net.URLConnection;
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
  private final LinkedHashMap<String, String> dependencyMappings;

  public ModuleAsset( Map<String, Serializable> asset, String adapter, List<String> aliases) {
    super("module", asset);

    //
    LinkedHashMap<String, String> dependencyMappings;
    if (depends != null && aliases != null) {
      dependencyMappings = new LinkedHashMap<String, String>();
      int size = Math.min(depends.size(), aliases.size());
      for (int i = 0;i < size;i++) {
        dependencyMappings.put(depends.get(i), aliases.get(i));
      }
    } else {
      dependencyMappings = null;
    }

    //
    depends.add("juzu.amd");

    //
    this.adapter = adapter;
    this.dependencyMappings = dependencyMappings;
  }

  @Override
  public InputStream open(URLConnection resource) throws IOException {

    InputStream stream = resource.getInputStream();

    if (dependencyMappings != null || adapter != null) {
      StringWriter buffer = new StringWriter();

      // The define call
      buffer.append("\ndefine('").append(id).append("', [");
      if (dependencyMappings != null) {
        joinDependencies(buffer);
      }
      buffer.append("], function(");
      if (dependencyMappings != null) {
        joinParams(buffer);
      }
      buffer.append(") {");

      // Redeclare here define
      // Note : this only work with
      // define(id,dependencies,factory)
      // it does not work with
      // define(?id,?dependencies,factory)
      // because we use 'arguments[2]'
      // so it should be done and tested
      buffer.append("var define = function() {");
      buffer.append("return arguments[2].apply(this, [");
      if (dependencyMappings != null) {
        joinParams(buffer);
      }
      buffer.append("]);");
      buffer.append("};");

      buffer.append("\nreturn ");

      int idx;
      if (adapter != null && !adapter.isEmpty()) {
        idx = adapter.indexOf("@{include}");
      } else {
        idx = -1;
      }

      if (idx != -1) {
        buffer.append(adapter.substring(0, idx)).append("\n");
      }
      Tools.copy(new NormalizeJSReader(new InputStreamReader(stream)), buffer);
      if (idx != -1) {
        buffer.append(adapter.substring(idx + "@{include}".length(), adapter.length()));
      }

      //
      buffer.append("\n});");

      //
      return new ByteArrayInputStream(buffer.toString().getBytes());
    } else {
      return stream;
    }
  }

  private void joinDependencies(Appendable sb) throws IOException {
    for (Iterator<String> i = dependencyMappings.keySet().iterator();i.hasNext();) {
      sb.append("'").append(i.next()).append("'");
      if (i.hasNext()) {
        sb.append(", ");
      }
    }
  }

  private void joinParams(Appendable sb) throws IOException {
    for (Iterator<String> i = dependencyMappings.values().iterator();i.hasNext();) {
      sb.append(i.next());
      if (i.hasNext()) {
        sb.append(", ");
      }
    }
  }
}
