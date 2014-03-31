package plugin.controller.valuetype.error.parse;

import juzu.impl.value.ValueType;
import java.util.Locale;

import java.util.Collections;

public class LocaleType extends ValueType<Locale> {

  @Override
  public Iterable<Class<?>> getTypes() {
    return Collections.<Class<?>>singleton(Locale.class);
  }

  @Override
  public Locale parse(String s) throws Exception {
    throw new java.text.ParseException("Normal behavior", 0);
  }

  @Override
  public String format(Locale value) {
    return value.toString();
  }
}