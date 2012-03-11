package org.juzu.plugin.asset;

import org.juzu.Response;
import org.juzu.impl.application.ApplicationException;
import org.juzu.impl.application.metadata.ApplicationDescriptor;
import org.juzu.impl.request.LifeCyclePlugin;
import org.juzu.impl.request.Request;
import org.juzu.request.Phase;

import javax.inject.Inject;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class AssetPlugin extends LifeCyclePlugin
{

   /** . */
   private static final String[] EMPTY_STRING_ARRAY = new String[0];

   /** . */
   private final String[] scripts;

   /** . */
   private final String[] stylesheets;

   @Inject
   public AssetPlugin(ApplicationDescriptor desc)
   {
      String[] scripts;
      String[] stylesheets;
      Class<?> packageClass = desc.getPackageClass();
      Assets assets = packageClass.getAnnotation(Assets.class);
      if (assets != null)
      {
         Script[] scriptDecls = assets.scripts();
         if (scriptDecls.length > 0)
         {
            scripts = new String[scriptDecls.length];
            for (int i = 0;i < scriptDecls.length;i++)
            {
               scripts[i] = scriptDecls[i].src();
            }
         }
         else
         {
            scripts = EMPTY_STRING_ARRAY;
         }
         Stylesheet[] stylesheetDecls = assets.stylesheets();
         if (stylesheetDecls.length > 0)
         {
            stylesheets = new String[stylesheetDecls.length];
            for (int i = 0;i < stylesheetDecls.length;i++)
            {
               stylesheets[i] = stylesheetDecls[i].src();
            }
         }
         else
         {
            stylesheets = EMPTY_STRING_ARRAY;
         }
      }
      else
      {
         scripts = EMPTY_STRING_ARRAY;
         stylesheets = EMPTY_STRING_ARRAY;
      }

      //
      this.stylesheets = stylesheets;
      this.scripts = scripts;
   }

   @Override
   public void invoke(Request request) throws ApplicationException
   {
      request.invoke();

      //
      if (request.getContext().getPhase() == Phase.RENDER)
      {
         Response response = request.getResponse();
         if (response instanceof Response.Render && (scripts.length > 0 || stylesheets.length > 0))
         {
            Response.Render render = (Response.Render)response;
            for (String script : scripts)
            {
               if (script.startsWith("http://") || script.startsWith("https://") || script.startsWith("/"))
               {
                  render.addScript(script);
               }
               else 
               {
                  
                  render.addScript(request.getContext().getHttpContext().getContextPath() + "/" + script); 
               }
            }
            for (String stylesheet : stylesheets)
            {
               if (stylesheet.startsWith("http://") || stylesheet.startsWith("https://") || stylesheet.startsWith("/"))
               {
                  render.addStylesheet(stylesheet);
               }
               else
               {

                  render.addStylesheet(request.getContext().getHttpContext().getContextPath() + "/" + stylesheet);
               }
            }
         }
      }
   }
}
