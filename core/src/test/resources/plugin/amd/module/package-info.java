@Modules({
    @Module(@Asset(id = "Foo", value = "foo.js")),
    @Module(
      value = @Asset(id = "Bar", value = "bar.js", depends = {"Foo"}),
      dependencies = {@juzu.plugin.amd.Dependency(id = "Foo", alias = "foo")}
    )
})
@juzu.Application
package plugin.amd.module;

import juzu.plugin.amd.Module;
import juzu.plugin.amd.Modules;
import juzu.plugin.asset.Asset;