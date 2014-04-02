package plugin.controller.valuetype.error.parse;

import juzu.impl.value.ValueType;

import java.lang.reflect.AnnotatedElement;
import java.util.Locale;

import java.util.Collections;

public class LocaleType extends ValueType<Locale> {

  @Override
  public Iterable<Class<?>> getTypes() {
    return Collections.<Class<?>>singleton(Locale.class);
  }

  @Override
  public Locale parse(AnnotatedElement element, String s) throws Exception {
    throw new java.text.ParseException("Normal behavior", 0);
  }

  @Override
  public String format(AnnotatedElement element, Locale value) {
    return value.toString();
  }
}