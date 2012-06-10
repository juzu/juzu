package juzu.impl.inject.spi.guice;

import com.google.inject.spi.InjectionListener;

import javax.annotation.PostConstruct;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class PostConstructInjectionListener implements InjectionListener<Object> {
  public void afterInjection(Object injectee) {
    for (Method method : injectee.getClass().getMethods()) {
      if (
        Modifier.isPublic(method.getModifiers()) &&
          !Modifier.isStatic(method.getModifiers()) &&
          method.getAnnotation(PostConstruct.class) != null) {
        try {
          method.invoke(injectee);
        }
        catch (IllegalAccessException e) {
          throw new UnsupportedOperationException("handle me gracefully", e);
        }
        catch (InvocationTargetException e) {
          throw new UnsupportedOperationException("handle me gracefully", e);
        }
      }
    }
  }
}
