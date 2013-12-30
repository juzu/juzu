@Modules({
    @Module(
        value = @Asset(id = "jquery", value = "jquery-1.7.1.js"),
        adapter="(function() { @{include} return jQuery.noConflict(true);})();"),
    @Module(
      value = @Asset(id = "foo", value = "foo.js", depends = {"jquery"}),
      aliases = {"$"}
    )
})
@juzu.Application
@WithAssets("foo")
package plugin.amd.adapter;

import juzu.plugin.amd.Module;
import juzu.plugin.amd.Modules;
import juzu.plugin.asset.Asset;
import juzu.plugin.asset.WithAssets;