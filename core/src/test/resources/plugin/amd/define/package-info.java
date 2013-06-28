@juzu.plugin.amd.Defines({
    @juzu.plugin.amd.Define(id = "Foo", path="foo.js"),
    @juzu.plugin.amd.Define(
      id = "Bar",
      path="bar.js",
      dependencies = {@juzu.plugin.amd.Dependency(id = "Foo", alias = "foo")}
    )
})
@juzu.Application
package plugin.amd.define;