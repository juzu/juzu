@Defines(value = {@Define(id = "Foo", path = "foo.js")})
@Modules(value = {@Module(id = "Bar", path = "bar.js", dependencies = {@Dependency(id = "Foo", alias = "foo")})})
@juzu.Application
package plugin.amd.mix;
import juzu.plugin.amd.*;