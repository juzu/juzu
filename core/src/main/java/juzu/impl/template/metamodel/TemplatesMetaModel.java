package juzu.impl.template.metamodel;

import juzu.impl.application.metamodel.ApplicationMetaModel;
import juzu.impl.compiler.ElementHandle;
import juzu.impl.metamodel.Key;
import juzu.impl.metamodel.MetaModelObject;
import juzu.impl.utils.JSON;
import juzu.impl.utils.Path;
import juzu.impl.utils.QN;

import java.util.Iterator;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class TemplatesMetaModel extends MetaModelObject implements Iterable<TemplateMetaModel> {

  /** . */
  public final static Key<TemplatesMetaModel> KEY = Key.of(TemplatesMetaModel.class);

  /** . */
  ApplicationMetaModel application;

  /** . */
  private QN qn;

  /** . */
  TemplateResolver resolver;

  @Override
  public JSON toJSON() {
    JSON json = new JSON();
    json.map("values", getChildren(TemplateMetaModel.class));
    json.set("qn", qn);
    return json;
  }

  public Path.Absolute resolve(Path path) {
    if (path instanceof Path.Absolute) {
      return (Path.Absolute)path;
    }
    else {
      return Path.Absolute.create(qn.append(path.getQN()), path.getRawName(), path.getExt());
    }
  }

  public ApplicationMetaModel getApplication() {
    return application;
  }

  public QN getQN() {
    return qn;
  }

  public TemplateMetaModel get(Path path) {
    return getChild(Key.of(path, TemplateMetaModel.class));
  }

  public Iterator<TemplateMetaModel> iterator() {
    return getChildren(TemplateMetaModel.class).iterator();
  }

  public TemplateRefMetaModel add(ElementHandle.Field handle, Path path) {
    TemplateRefMetaModel ref = addChild(Key.of(handle, TemplateRefMetaModel.class), new TemplateRefMetaModel(handle, path));

    //
    TemplateMetaModel template = getChild(Key.of(path, TemplateMetaModel.class));
    if (template == null) {
      template = addChild(Key.of(path, TemplateMetaModel.class), new TemplateMetaModel(path));
    }

    //
    ref.addChild(TemplateMetaModel.KEY, template);
    return ref;
  }

  public void remove(TemplateMetaModel template) {
    if (template.templates != this) {
      throw new IllegalArgumentException();
    }
    removeChild(Key.of(template.path, TemplateMetaModel.class));
  }

  @Override
  protected void postAttach(MetaModelObject parent) {
    if (parent instanceof ApplicationMetaModel) {
      this.application = (ApplicationMetaModel)parent;
      this.qn = application.getFQN().getPackageName().append("templates");
      this.resolver = new TemplateResolver(application);
    }
  }
}
