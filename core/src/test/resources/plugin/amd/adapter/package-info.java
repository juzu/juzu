@Modules({
    @Module(
        value = @Asset(id = "jquery", value = "jquery-1.7.1.js", depends = {}),
        adapter="(function() { @{include} return jQuery.noConflict(true);})();"),
    @Module(
      value = @Asset(id = "foo", value = "foo.js", depends = {"jquery"}),
      dependencies = {@juzu.plugin.amd.Dependency(id = "jquery", alias = "$")}
    )
})
@juzu.Application
package plugin.amd.adapter;

import juzu.plugin.amd.Module;
import juzu.plugin.amd.Modules;
import juzu.plugin.asset.Asset;