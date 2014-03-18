/*
 * Copyright 2013 eXo Platform SAS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package juzu.impl.plugin.router;

import juzu.impl.common.JSON;
import juzu.impl.common.MethodHandle;
import juzu.impl.plugin.PluginDescriptor;
import juzu.impl.router.PathParam;
import juzu.impl.router.Route;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RouteDescriptor extends PluginDescriptor {

  /** . */
  private final String path;

  /** . */
  public final MethodHandle handle;

  /** . */
  public final HashMap<String, ParamDescriptor> parameters;

  public RouteDescriptor(JSON config) {

    HashMap<String, ParamDescriptor> parameters = null;
    JSON foo = config.getJSON("parameters");
    if (foo != null) {
      parameters = new HashMap<String, ParamDescriptor>();
      for (String name : foo.names()) {
        String pattern = foo.getJSON(name).getString("pattern");
        Boolean preservePath = foo.getJSON(name).getBoolean("preserve-path");
        Boolean captureGroup = foo.getJSON(name).getBoolean("capture-group");
        parameters.put(name, new ParamDescriptor(pattern, preservePath, captureGroup));
      }
    }

    //
    this.path = config.getString("path");
    this.handle = MethodHandle.parse(config.getString("handle"));
    this.parameters = parameters;
  }

  public String getPath() {
    return path;
  }

  public Route popupate(Route parent) {
    Map<String, PathParam.Builder> parameters;
    if (this.parameters != null && this.parameters.size() > 0) {
      parameters = new HashMap<String, PathParam.Builder>(this.parameters.size());
      for (Map.Entry<String, ParamDescriptor> parameter : this.parameters.entrySet()) {
        ParamDescriptor paramDescriptor = parameter.getValue();
        PathParam.Builder builder = PathParam.matching(paramDescriptor.pattern);
        if (paramDescriptor.preservePath != null) {
          builder.setPreservePath(paramDescriptor.preservePath);
        }
        if (paramDescriptor.captureGroup != null) {
          builder.setCaptureGroup(paramDescriptor.captureGroup);
        }
        parameters.put(parameter.getKey(), builder);
      }
    } else {
      parameters = Collections.emptyMap();
    }
    return parent.append(path, parameters);
  }
}
