package juzu.templating.mustache;

import juzu.template.TemplateRenderContext;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class ContextMap extends AbstractMap<String, Object> {

  /** . */
  final TemplateRenderContext renderContext;

  ContextMap(TemplateRenderContext renderContext) {
    this.renderContext = renderContext;
  }

  @Override
  public boolean containsKey(Object key) {
    return get(key) != null;
  }

  @Override
  public Object get(Object key) {
    Object value = null;
    if (key instanceof String) {
      Map<String,?> attributes = renderContext.getAttributes();
      if (attributes != null) {
        value = attributes.get(key);
      }
      if (value == null) {
        value = renderContext.resolveBean((String)key);
      }
    }
    return value;
  }

  @Override
  public Set<Entry<String, Object>> entrySet() {
    // We don't care about listing and actually it would not be really possible with bean resolution
    return Collections.emptySet();
  }
}
