package juzu.test.protocol.portlet;

import juzu.impl.application.ApplicationRuntime;
import juzu.test.AbstractWebTestCase;
import juzu.test.protocol.mock.MockApplication;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.DependencyResolvers;
import org.jboss.shrinkwrap.resolver.api.maven.MavenDependencyResolver;
import org.junit.runner.RunWith;

import java.net.URL;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
@RunWith(Arquillian.class)
public abstract class AbstractPortletTestCase extends AbstractWebTestCase {

  @Deployment(testable = false)
  public static WebArchive createDeployment() {
    URL servletXML = AbstractPortletTestCase.class.getResource("web.xml");
    URL portletXML = AbstractPortletTestCase.class.getResource("portlet.xml");
    return ShrinkWrap.create(WebArchive.class, "juzu.war").
    addAsLibraries(DependencyResolvers.
      use(MavenDependencyResolver.class).
      loadEffectivePom("pom.xml")
      .artifacts("javax.servlet:jstl", "taglibs:standard").
      resolveAsFiles()).
    setWebXML(servletXML).
    addAsWebInfResource(portletXML, "portlet.xml");
  }

  @Override
  protected void doDeploy(MockApplication<?> application) {
    ApplicationRuntime.Provided.set(application.getRuntime());
  }

  @Override
  protected void doUndeploy(MockApplication<?> application) {
  }
}
