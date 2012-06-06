package juzu.impl.plugin.portlet;

import juzu.Scope;
import juzu.impl.metadata.BeanDescriptor;
import juzu.impl.metadata.Descriptor;
import juzu.impl.spi.request.portlet.PortletPreferencesProvider;
import juzu.impl.utils.Tools;

import javax.portlet.PortletPreferences;
import java.util.Collections;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class PortletDescriptor extends Descriptor {

  /** . */
  private static List<BeanDescriptor> DESCRIPTORS = Collections.unmodifiableList(Tools.list(
    new BeanDescriptor(PortletPreferences.class, Scope.REQUEST, null, PortletPreferencesProvider.class))
  );

  /** . */
  public static PortletDescriptor INSTANCE = new PortletDescriptor();

  private PortletDescriptor() {
  }

  @Override
  public Iterable<BeanDescriptor> getBeans() {
    return DESCRIPTORS;
  }
}
