@Application
@WebJars(@WebJar("jquery"))
@Modules({
  @Module(
    value = @Asset(id = "jquery", value = "jquery.js"),
    adapter="(function() { @{include} return jQuery.noConflict(true);})();"
  ),
  @Module(
    value = @Asset(id = "foo", value="foo.js", depends = "jquery"),
    aliases = {"$"}
  )
})
package juzu.amd;

import juzu.Application;
import juzu.plugin.amd.Module;
import juzu.plugin.amd.Modules;
import juzu.plugin.webjars.WebJar;
import juzu.plugin.webjars.WebJars;
import juzu.plugin.asset.Asset;
