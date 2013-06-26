@juzu.plugin.amd.Defines({
    @juzu.plugin.amd.Define(name = "jquery", path="/webjars!jquery.js", adapter="(function() { @{include} return jQuery.noConflict(true);})();"),
    @juzu.plugin.amd.Define(
      name = "foo",
      path="foo.js",
      dependencies = {@juzu.plugin.amd.Dependency(name = "jquery", alias = "$")}
    )
})
@juzu.Application
package plugin.amd.webjars;