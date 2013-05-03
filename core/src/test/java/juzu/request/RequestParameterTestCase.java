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
package juzu.request;

import juzu.test.AbstractTestCase;
import org.junit.Test;

import java.util.AbstractMap;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RequestParameterTestCase extends AbstractTestCase {

  @Test
  public void testCreateNPE() {
    try {
      RequestParameter.create(null);
      fail();
    }
    catch (NullPointerException e) {
    }
    try {
      RequestParameter.create(null, "a");
      fail();
    }
    catch (NullPointerException e) {
    }
    try {
      RequestParameter.create("a", (String)null);
      fail();
    }
    catch (NullPointerException e) {
    }
    try {
      RequestParameter.create(null, "a", "a");
      fail();
    }
    catch (NullPointerException e) {
    }
    try {
      RequestParameter.create("a", "a", null);
      fail();
    }
    catch (NullPointerException e) {
    }
    try {
      RequestParameter.create(null, new String[]{"a"});
      fail();
    }
    catch (NullPointerException e) {
    }
    try {
      RequestParameter.create("a", (String[])null);
      fail();
    }
    catch (NullPointerException e) {
    }
  }

  @Test
  public void testCreateIAE() {
    try {
      RequestParameter.create("a", new String[0]);
      fail();
    }
    catch (IllegalArgumentException e) {
    }
    try {
      RequestParameter.create("a", new String[]{null});
      fail();
    }
    catch (IllegalArgumentException e) {
    }
    try {
      RequestParameter.create(new AbstractMap.SimpleEntry<String, String[]>(null, new String[]{"a"}));
      fail();
    }
    catch (IllegalArgumentException e) {
    }
    try {
      RequestParameter.create(new AbstractMap.SimpleEntry<String, String[]>("a", null));
      fail();
    }
    catch (IllegalArgumentException e) {
    }
    try {
      RequestParameter.create(new AbstractMap.SimpleEntry<String, String[]>("", new String[0]));
      fail();
    }
    catch (IllegalArgumentException e) {
    }
    try {
      RequestParameter.create(new AbstractMap.SimpleEntry<String, String[]>("", new String[]{null}));
      fail();
    }
    catch (IllegalArgumentException e) {
    }
  }
}
