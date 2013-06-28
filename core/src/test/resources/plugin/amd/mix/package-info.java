@Requires (value = {@Require(id = "Foo", path = "foo.js")})
@Defines(value = {@Define(id = "Bar", path = "bar.js", dependencies = {@Dependency(id = "Foo", alias = "foo")})})
@juzu.Application
package plugin.amd.mix;
import juzu.plugin.amd.*;