@Application
@WebJars(@WebJar("jquery"))
@Modules({
  @Module(
    id = "jquery",
    value = "jquery/1.10.2/jquery.js",
    adapter="(function() { @{include} return jQuery.noConflict(true);})();"
  ),
  @Module(
    id = "foo",
    value="foo.js",
    depends = "jquery",
    aliases = {"$"}
  )
})
@Assets("foo")
package juzu.amd;

import juzu.Application;
import juzu.plugin.amd.Module;
import juzu.plugin.amd.Modules;
import juzu.plugin.webjars.WebJar;
import juzu.plugin.webjars.WebJars;
import juzu.plugin.asset.Assets;
