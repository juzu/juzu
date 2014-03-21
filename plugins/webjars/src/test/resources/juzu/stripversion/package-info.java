@Application
@WebJars(@WebJar(value = "jquery", stripVersion = true))
@Scripts(@Script("jquery/jquery.js"))
@Assets("*")
package juzu.stripversion;

import juzu.Application;
import juzu.plugin.asset.Script;
import juzu.plugin.asset.Scripts;
import juzu.plugin.asset.Assets;
import juzu.plugin.webjars.WebJars;
import juzu.plugin.webjars.WebJar;