package org.juzu.impl.plugin.portlet;

import org.juzu.Scope;
import org.juzu.impl.metadata.BeanDescriptor;
import org.juzu.impl.metadata.Descriptor;
import org.juzu.impl.spi.request.portlet.PortletPreferencesProvider;
import org.juzu.impl.utils.Tools;

import javax.portlet.PortletPreferences;
import java.util.Collections;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class PortletDescriptor extends Descriptor
{

   /** . */
   private static List<BeanDescriptor> DESCRIPTORS = Collections.unmodifiableList(Tools.list(
      new BeanDescriptor(PortletPreferences.class, Scope.REQUEST, null, PortletPreferencesProvider.class))
   );

   /** . */
   public static PortletDescriptor INSTANCE = new PortletDescriptor();

   private PortletDescriptor()
   {
   }

   @Override
   public Iterable<BeanDescriptor> getBeans()
   {
      return DESCRIPTORS;
   }
}
