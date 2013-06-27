@Requires (value = {@Require(name = "Foo", path = "foo.js")})
@Defines(value = {@Define(name = "Bar", path = "bar.js", dependencies = {@Dependency(name = "Foo", alias = "foo")})})
@juzu.Application
package plugin.amd.mix;
import juzu.plugin.amd.*;