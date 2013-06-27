@Application
@WebJars("jquery.js")
@Assets(scripts = { @Script(id = "jquery", src="jquery.js") })
package juzu.webjars;

import juzu.Application;
import juzu.plugin.asset.Assets;
import juzu.plugin.asset.Script;
import juzu.plugin.webjars.WebJars;