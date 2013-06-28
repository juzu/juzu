@juzu.plugin.amd.Defines({
    @juzu.plugin.amd.Define(id = "jquery", path="jquery-1.7.1.js", adapter="(function() { @{include} return jQuery.noConflict(true);})();"),
    @juzu.plugin.amd.Define(
      id = "foo",
      path="foo.js",
      dependencies = {@juzu.plugin.amd.Dependency(id = "jquery", alias = "$")}
    )
})
@juzu.Application
package plugin.amd.adapter;