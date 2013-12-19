@Modules({
    @Module(id = "jquery", path="jquery-1.7.1.js", adapter="(function() { @{include} return jQuery.noConflict(true);})();"),
    @Module(
      id = "foo",
      path="foo.js",
      dependencies = {@juzu.plugin.amd.Dependency(id = "jquery", alias = "$")}
    )
})
@juzu.Application
package plugin.amd.adapter;

import juzu.plugin.amd.Module;
import juzu.plugin.amd.Modules;