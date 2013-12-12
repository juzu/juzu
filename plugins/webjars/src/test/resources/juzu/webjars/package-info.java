@Application
@WebJars(@WebJar(id = "jquery", version = "2.0.0"))
@Assets(@Asset("jquery.js"))
@WithAssets
package juzu.webjars;

import juzu.Application;
import juzu.plugin.asset.Asset;
import juzu.plugin.asset.Assets;
import juzu.plugin.asset.WithAssets;
import juzu.plugin.webjars.WebJars;
import juzu.plugin.webjars.WebJar;