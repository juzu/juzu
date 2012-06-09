package juzu.impl.application.metamodel;

import juzu.Application;
import juzu.impl.compiler.AnnotationData;
import juzu.impl.metamodel.EventQueue;
import juzu.impl.metamodel.MetaModel;
import juzu.impl.metamodel.MetaModelPlugin;
import juzu.impl.utils.JSON;
import juzu.impl.utils.Tools;

import javax.lang.model.element.Element;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ApplicationsMetaModelPlugin extends MetaModelPlugin {

  /** . */
  private final LinkedHashMap<String, ApplicationMetaModelPlugin> plugins;

  /** . */
  private final HashSet<Class<? extends Annotation>> annotationTypes;

  public ApplicationsMetaModelPlugin() {
    super("applications");

    //
    HashSet<Class<? extends Annotation>> annotationTypes = new HashSet<Class<? extends Annotation>>();
    annotationTypes.add(Application.class);
    LinkedHashMap<String, ApplicationMetaModelPlugin> plugins = new LinkedHashMap<String, ApplicationMetaModelPlugin>();
    StringBuilder msg = new StringBuilder("Using plugins:");
    for (ApplicationMetaModelPlugin plugin : Tools.list(ServiceLoader.load(ApplicationMetaModelPlugin.class, ApplicationMetaModelPlugin.class.getClassLoader()))) {
      annotationTypes.addAll(plugin.getAnnotationTypes());
      msg.append(" ").append(plugin.getName());
      plugins.put(plugin.getName(), plugin);
    }
    MetaModel.log.log(msg);

    //
    this.plugins = plugins;
    this.annotationTypes = annotationTypes;
  }

  @Override
  public Set<Class<? extends Annotation>> getAnnotationTypes() {
    return annotationTypes;
  }

  @Override
  public JSON toJSON(MetaModel metaModel) {
    return metaModel.getChild(ApplicationsMetaModel.KEY).toJSON();
  }

  @Override
  public void init(MetaModel metaModel) {
    ApplicationsMetaModel application = new ApplicationsMetaModel();

    //
    metaModel.addChild(ApplicationsMetaModel.KEY, application);

    // Add plugins
    for (Map.Entry<String, ApplicationMetaModelPlugin> entry : plugins.entrySet()) {
      application.addPlugin(entry.getKey(), entry.getValue());
    }
  }

  @Override
  public void postActivate(MetaModel metaModel) {
    metaModel.getChild(ApplicationsMetaModel.KEY).postActivate(metaModel);
  }

  @Override
  public void processAnnotation(MetaModel metaModel, Element element, String fqn, AnnotationData data) {
    metaModel.getChild(ApplicationsMetaModel.KEY).processAnnotation(metaModel, element, fqn, data);
  }

  @Override
  public void postProcessAnnotations(MetaModel metaModel) {
    metaModel.getChild(ApplicationsMetaModel.KEY).postProcessAnnotations(metaModel);
  }

  @Override
  public void processEvents(MetaModel metaModel, EventQueue queue) {
    metaModel.getChild(ApplicationsMetaModel.KEY).processEvents(metaModel, queue);
  }

  @Override
  public void postProcessEvents(MetaModel metaModel) {
    metaModel.getChild(ApplicationsMetaModel.KEY).postProcessEvents(metaModel);
  }

  @Override
  public void prePassivate(MetaModel metaModel) {
    metaModel.getChild(ApplicationsMetaModel.KEY).prePassivate(metaModel);
  }


/*
   public ApplicationMetaModel addApplication(String packageName, String applicationName)
   {
      return applications.add(ElementHandle.Package.create(QN.parse(packageName)), applicationName);
   }
*/
}
