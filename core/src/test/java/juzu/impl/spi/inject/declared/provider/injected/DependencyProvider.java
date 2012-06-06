package juzu.impl.spi.inject.declared.provider.injected;

import javax.inject.Inject;
import javax.inject.Provider;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class DependencyProvider implements Provider<Bean> {

  @Inject
  private Dependency dependency;

  public DependencyProvider() {
    System.out.println("FOO");
  }

  public Bean get() {
    return new Bean(dependency);
  }
}
