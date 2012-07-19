package juzu.impl.controller.metamodel;

import juzu.AmbiguousResolutionException;
import juzu.impl.compiler.ElementHandle;
import juzu.impl.metamodel.Key;
import juzu.impl.metamodel.MetaModel;
import juzu.impl.metamodel.MetaModelObject;
import juzu.impl.common.FQN;
import juzu.impl.common.JSON;

import java.util.Iterator;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ControllersMetaModel extends MetaModelObject implements Iterable<ControllerMetaModel> {

  /** . */
  public final static Key<ControllersMetaModel> KEY = Key.of(ControllersMetaModel.class);

  /** . */
  FQN defaultController;

  /** . */
  Boolean escapeXML;

  @Override
  public JSON toJSON() {
    JSON json = new JSON();
    json.map("values", getChildren(ControllerMetaModel.class));
    return json;
  }

  public Iterator<ControllerMetaModel> iterator() {
    return getChildren(ControllerMetaModel.class).iterator();
  }

  public ControllerMetaModel get(ElementHandle.Class handle) {
    return getChild(Key.of(handle, ControllerMetaModel.class));
  }

  public void add(ControllerMetaModel controller) {
    addChild(Key.of(controller.handle, ControllerMetaModel.class), controller);
  }

  public void remove(ControllerMetaModel controller) {
    if (controller.controllers != this) {
      throw new IllegalArgumentException();
    }
    removeChild(Key.of(controller.handle, ControllerMetaModel.class));
  }

  public MethodMetaModel resolve(String typeName, String methodName, Set<String> parameterNames) throws AmbiguousResolutionException {
    try {
      ControllerMetaModelResolver resolver = new ControllerMetaModelResolver(this);
      return resolver.resolve(typeName, methodName, parameterNames);
    }
    catch (AmbiguousResolutionException e) {
      MetaModel.log.log("Could not resolve ambiguous method " + methodName + " " + parameterNames);
      return null;
    }
  }
}
