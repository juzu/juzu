package plugin.controller.valuetype.custom;

import juzu.impl.value.ValueType;

import java.lang.reflect.AnnotatedElement;
import java.util.Collections;

public class FooType extends ValueType<Foo> {

  @Override
  public Iterable<Class<?>> getTypes() {
    return Collections.<Class<?>>singleton(Foo.class);
  }

  @Override
  public Foo parse(AnnotatedElement element, String s) {
    return new Foo(s);
  }

  @Override
  public String format(AnnotatedElement element, Foo value) {
    return value.value;
  }
}