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

package juzu.impl.plugin.router.metamodel;

import juzu.impl.common.JSON;
import juzu.impl.plugin.router.ParamDescriptor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RouteMetaModel implements Serializable {

  /** . */
  final String path;

  /** . */
  final String handle;

  /** . */
  final int priority;

  /** . */
  HashMap<String, ParamDescriptor> parameters;

  public RouteMetaModel(String path, String handle, int priority, HashMap<String, ParamDescriptor> parameters) {
    this.path = path;
    this.handle = handle;
    this.priority = priority;
    this.parameters = parameters;
  }

  public String getPath() {
    return path;
  }

  public JSON toJSON() {

    //
    JSON json = new JSON();

    //
    json.set("path", path);
    json.set("handle", handle);

    //
    if (parameters != null && parameters.size() > 0) {
      JSON b = new JSON();
      for (Map.Entry<String, ParamDescriptor> parameter : parameters.entrySet()) {
        ParamDescriptor value = parameter.getValue();
        b.set(parameter.getKey(), new JSON().
            set("pattern", value.getPattern()).
            set("preserve-path", value.getPreservePath()).
            set("capture-group", value.getCaptureGroup())
        );
      }
      json.set("parameters", b);
    }

    //
    return json;
  }
}
