package plugin.controller.valuetype.custom;

import juzu.impl.value.ValueType;

import java.util.Collections;

public class FooType extends ValueType<Foo> {

  @Override
  public Iterable<Class<?>> getTypes() {
    return Collections.<Class<?>>singleton(Foo.class);
  }

  @Override
  public Foo parse(String s) {
    return new Foo(s);
  }

  @Override
  public String format(Foo value) {
    return value.value;
  }
}