package juzu.impl.plugin;

import juzu.impl.application.metamodel.ApplicationMetaModelPlugin;
import juzu.impl.metadata.BeanDescriptor;
import juzu.impl.metadata.Descriptor;
import juzu.impl.request.RequestLifeCycle;
import juzu.impl.utils.JSON;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Set;

/**
 * Base class for a plugin.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public abstract class Plugin {

  /** The plugin name. */
  private final String name;

  protected Plugin(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public Set<Class<? extends Annotation>> getAnnotationTypes() {
    return Collections.emptySet();
  }

  /**
   * Returns the application meta model plugin type.
   *
   * @return the application meta model plugin type
   */
  public ApplicationMetaModelPlugin newApplicationMetaModelPlugin() {
    return new ApplicationMetaModelPlugin();
  }

  /**
   * Returns the plugin descriptor.
   *
   * @param loader the loader
   * @param config the plugin config
   * @return the descriptor
   * @throws Exception any exception
   */
  public Descriptor loadDescriptor(ClassLoader loader, JSON config) throws Exception {
    return new Descriptor() {
      @Override
      public Iterable<BeanDescriptor> getBeans() {
        Class<? extends RequestLifeCycle> lifeCycleClass = getLifeCycleClass();
        if (lifeCycleClass != null) {
          return Collections.singletonList(new BeanDescriptor(lifeCycleClass, null, null, null));
        }
        else {
          return Collections.emptyList();
        }
      }
    };
  }

  public Class<? extends RequestLifeCycle> getLifeCycleClass() {
    return null;
  }
}
