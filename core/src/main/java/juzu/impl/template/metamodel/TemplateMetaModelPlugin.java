package juzu.impl.template.metamodel;

import juzu.impl.application.metamodel.ApplicationMetaModel;
import juzu.impl.application.metamodel.ApplicationMetaModelPlugin;
import juzu.impl.application.metamodel.ApplicationsMetaModel;
import juzu.impl.compiler.AnnotationData;
import juzu.impl.compiler.ProcessingException;
import juzu.impl.compiler.ElementHandle;
import juzu.impl.metamodel.MetaModel;
import juzu.impl.metamodel.MetaModelProcessor;
import juzu.impl.template.spi.TemplateProvider;
import juzu.impl.template.spi.Template;
import juzu.impl.common.JSON;
import juzu.impl.common.Path;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class TemplateMetaModelPlugin extends ApplicationMetaModelPlugin {

  /** . */
  public static final Pattern PATH_PATTERN = Pattern.compile("([^/].*/|)([^./]+)\\.([a-zA-Z]+)");

  /** . */
  Map<String, TemplateProvider> providers;

  public TemplateMetaModelPlugin() {
    super("template");
  }

  @Override
  public Set<Class<? extends Annotation>> getAnnotationTypes() {
    return Collections.<Class<? extends Annotation>>singleton(juzu.Path.class);
  }

  @Override
  public void init(ApplicationsMetaModel applications) {
  }

  @Override
  public void postActivateApplicationsMetaModel(ApplicationsMetaModel applications) {
    // Discover the template providers
    Iterable<TemplateProvider> loader = applications.model.env.loadServices(TemplateProvider.class);
    Map<String, TemplateProvider> providers = new HashMap<String, TemplateProvider>();
    for (TemplateProvider provider : loader) {
      providers.put(provider.getSourceExtension(), provider);
    }

    //
    this.providers = providers;
  }

  @Override
  public void postConstruct(ApplicationMetaModel application) {
    TemplatesMetaModel templates = new TemplatesMetaModel();
    templates.plugin = this;
    application.addChild(TemplatesMetaModel.KEY, templates);
  }

  @Override
  public void processAnnotation(ApplicationMetaModel application, Element element, String fqn, AnnotationData data) throws ProcessingException {
    if (fqn.equals("juzu.Path")) {
      if (element instanceof VariableElement) {
        VariableElement variableElt = (VariableElement)element;
        MetaModel.log.log("Processing template declaration " + variableElt.getEnclosingElement() + "#" + variableElt);

        //
        TemplatesMetaModel at = application.getChild(TemplatesMetaModel.KEY);

        //
        Path path = Path.parse((String)data.get("value"));
        ElementHandle.Field handle = ElementHandle.Field.create(variableElt);
        at.add(handle, path);
      }
      else if (element instanceof TypeElement) {
        // We ignore it on purpose
      }
      else {
        throw MetaModelProcessor.ANNOTATION_UNSUPPORTED.failure(element);
      }
    }
  }

  @Override
  public void postActivate(ApplicationMetaModel application) {
    application.getChild(TemplatesMetaModel.KEY).plugin = this;
  }

  @Override
  public void prePassivate(ApplicationMetaModel application) {
    MetaModel.log.log("Passivating template resolver for " + application.getHandle());
    TemplatesMetaModel metaModel = application.getChild(TemplatesMetaModel.KEY);
    metaModel.resolver.prePassivate();
    metaModel.plugin = null;
  }

  @Override
  public void prePassivate(ApplicationsMetaModel applications) {
    MetaModel.log.log("Passivating templates");
    this.providers = null;
  }

  @Override
  public void postProcessEvents(ApplicationMetaModel application) {
    MetaModel.log.log("Processing templates of " + application.getHandle());
    application.getChild(TemplatesMetaModel.KEY).resolver.process(this, application.model.env);
  }

  @Override
  public JSON getDescriptor(ApplicationMetaModel application) {
    JSON config = new JSON();
    ArrayList<String> list = new ArrayList<String>();
    TemplatesMetaModel metaModel = application.getChild(TemplatesMetaModel.KEY);
    for (Template template : metaModel.resolver.getTemplates()) {
      Path resolved = metaModel.resolve(template.getPath());
      list.add(resolved.getFQN().getName());
    }
    config.map("templates", list);
    config.set("package", metaModel.getQN().toString());
    return config;
  }
}
