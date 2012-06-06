package juzu.impl.compiler;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class CompilerConfig {

  /** . */
  private static final Map<String, String> EMPTY = Collections.emptyMap();

  /** . */
  private Map<String, String> processorOptions = EMPTY;

  /** . */
  private boolean force;

  public CompilerConfig withProcessorOption(String optionName, String optionValue) {
    if (optionValue != null) {
      if (processorOptions == EMPTY) {
        processorOptions = new HashMap<String, String>();
      }
      processorOptions.put(optionName, optionValue);
    }
    else {
      if (processorOptions != EMPTY) {
        processorOptions.remove(optionName);
      }
    }
    return this;
  }

  public Iterable<String> getProcessorOptionNames() {
    return processorOptions.keySet();
  }

  public String getProcessorOptionValue(String optionName) {
    return processorOptions.get(optionName);
  }

  public boolean getForce() {
    return force;
  }

  public CompilerConfig force(boolean force) {
    this.force = force;
    return this;
  }
}
