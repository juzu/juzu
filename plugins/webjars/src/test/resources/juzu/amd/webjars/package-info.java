@Application
@WebJars("jquery.js")

@Defines({
  @Define(name = "jquery", path="jquery.js", adapter="(function() { @{include} return jQuery.noConflict(true);})();"),
  @Define(
    name = "foo",
    path="foo.js",
    dependencies = {@Dependency(name = "jquery", alias = "$")}
  )
})
package juzu.amd.webjars;

import juzu.Application;
import juzu.plugin.amd.Define;
import juzu.plugin.amd.Defines;
import juzu.plugin.amd.Dependency;
import juzu.plugin.webjars.WebJars;
