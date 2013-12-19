@Modules({
    @Module(id = "Foo", path="foo.js"),
    @Module(
      id = "Bar",
      path="bar.js",
      dependencies = {@juzu.plugin.amd.Dependency(id = "Foo", alias = "foo")}
    )
})
@juzu.Application
package plugin.amd.module;

import juzu.plugin.amd.Module;
import juzu.plugin.amd.Modules;