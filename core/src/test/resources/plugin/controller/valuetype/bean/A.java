/*
 * Copyright 2013 eXo Platform SAS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package plugin.controller.valuetype.bean;

import juzu.Response;
import juzu.View;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import static juzu.impl.common.Tools.safeEquals;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class A {

  /** . */
  public static final long TEST_VALUE = System.currentTimeMillis();

  @View
  public Response.Content index() {
    Bean b = new Bean();
    b.s = "string";
    b.list = Arrays.asList("s1", "s2");
    b.integer = 4;
    b.listInteger = Arrays.asList(6, 7);
    b.integerArray = new Integer[]{0,1};
    b.integerPrimitive = 6;
    b.integerPrimitiveArray = new int[]{3,4};
    b._s = "string_";
    b._list = Arrays.asList("s3", "s4");
    b._integer = 5;
    b._listInteger = Arrays.asList(9, 10);
    b._integerArray = new Integer[]{1,2};
    b._integerPrimitive = 7;
    b._integerPrimitiveArray = new int[]{4,5};
    return Response.ok("" + A_.foo(b));
  }

  @View
  public Response.Content foo(Bean bean) throws Exception {
    if (bean == null) {
      return Response.ok("no bean");
    }
    if (!safeEquals(bean.s, "string")) {
      return Response.ok("fail s=" + bean.s);
    }
    if (!safeEquals(bean.list, Arrays.asList("s1", "s2"))) {
      return Response.ok("fail list=" + bean.list);
    }
    if (!safeEquals(bean.integer, 4)) {
      return Response.ok("fail integer=" + bean.integer);
    }
    if (!safeEquals(bean.listInteger, Arrays.asList(6, 7))) {
      return Response.ok("fail listInteger=" + bean.listInteger);
    }
    if (bean.integerArray == null) {
      return Response.ok("fail integerArray=null");
    } else if (!Arrays.equals(bean.integerArray, new Integer[]{0,1})) {
      return Response.ok("fail integerArray=" + Arrays.toString(bean.integerArray));
    }
    if (bean.integerPrimitive != 6) {
      return Response.ok("fail integerPrimitive=" + bean.integerPrimitive);
    }
    if (bean.integerPrimitiveArray == null) {
      return Response.ok("fail integerPrimitiveArray=null");
    } else if (!Arrays.equals(bean.integerPrimitiveArray, new int[]{3,4})) {
      return Response.ok("fail integerPrimitiveArray=" + Arrays.toString(bean.integerPrimitiveArray));
    }
    if (!safeEquals(bean._s, "string_")) {
      return Response.ok("fail _s=" + bean._s);
    }
    if (!safeEquals(bean._list, Arrays.asList("s3", "s4"))) {
      return Response.ok("fail _list=" + bean._list);
    }
    if (!safeEquals(bean._integer, 5)) {
      return Response.ok("fail _integer=" + bean._integer);
    }
    if (!safeEquals(bean._listInteger, Arrays.asList(9, 10))) {
      return Response.ok("fail _listInteger=" + bean._listInteger);
    }
    if (bean._integerArray == null) {
      return Response.ok("fail _integerArray=null");
    } else if (!Arrays.equals(bean._integerArray, new Integer[]{1,2})) {
      return Response.ok("fail _integerArray=" + Arrays.toString(bean._integerArray));
    }
    if (bean._integerPrimitive != 7) {
      return Response.ok("fail _integerPrimitive=" + bean._integerPrimitive);
    }
    if (bean._integerPrimitiveArray == null) {
      return Response.ok("fail _integerPrimitiveArray=null");
    } else if (!Arrays.equals(bean._integerPrimitiveArray, new int[]{4,5})) {
      return Response.ok("fail _integerPrimitiveArray=" + Arrays.toString(bean._integerPrimitiveArray));
    }
    return Response.ok("pass");
  }
}
