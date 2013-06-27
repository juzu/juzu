@Requires (value = {@Require(name = "Foo", path = "foo.js"), @Require(name = "Bar", path = "bar.js")})
@juzu.Application
package plugin.amd.require;
import juzu.plugin.amd.*;