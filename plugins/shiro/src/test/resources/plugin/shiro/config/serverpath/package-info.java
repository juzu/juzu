@juzu.Application
@Shiro(config = @Configuration(value = "/WEB-INF/shiro.ini", location = AssetLocation.SERVER))
package plugin.shiro.config.serverpath;

import juzu.plugin.shiro.*;
import juzu.asset.AssetLocation;