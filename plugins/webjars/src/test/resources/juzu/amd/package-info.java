@Application
@WebJars(@WebJar("jquery"))
@Modules({
  @Module(id = "jquery", path="jquery.js", adapter="(function() { @{include} return jQuery.noConflict(true);})();"),
  @Module(
    id = "foo",
    path="foo.js",
    dependencies = {@Dependency(id = "jquery", alias = "$")}
  )
})
package juzu.amd;

import juzu.Application;
import juzu.plugin.amd.Module;
import juzu.plugin.amd.Modules;
import juzu.plugin.amd.Dependency;
import juzu.plugin.webjars.WebJar;
import juzu.plugin.webjars.WebJars;
