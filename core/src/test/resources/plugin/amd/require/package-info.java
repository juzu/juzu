@Requires (value = {@Require(id = "Foo", path = "foo.js"), @Require(id = "Bar", path = "bar.js")})
@juzu.Application
package plugin.amd.require;
import juzu.plugin.amd.*;