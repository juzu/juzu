@Application
@WebJars(@WebJar(id = "jquery", version = "2.0.0"))
@Defines({
  @Define(id = "jquery", path="jquery.js", adapter="(function() { @{include} return jQuery.noConflict(true);})();"),
  @Define(
    id = "foo",
    path="foo.js",
    dependencies = {@Dependency(id = "jquery", alias = "$")}
  )
})
package juzu.amd.webjars;

import juzu.Application;
import juzu.plugin.amd.Define;
import juzu.plugin.amd.Defines;
import juzu.plugin.amd.Dependency;
import juzu.plugin.webjars.WebJar;
import juzu.plugin.webjars.WebJars;
