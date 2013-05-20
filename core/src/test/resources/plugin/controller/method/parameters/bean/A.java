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

package plugin.controller.method.parameters.bean;

import juzu.Action;
import juzu.Response;

import java.io.IOException;
import java.util.Arrays;

/** @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a> */
public class A {
  private B bean;

  public A() {
    bean = new B();
    bean.setA("v");
    bean.setB(new String[]{"v1", "v2"});
    bean.setC(Arrays.asList("v1", "v2"));
    bean.d = "v";
    bean.e = new String[]{"v1", "v2"};
    bean.f = Arrays.asList("v1", "v2");
  }

  // ******************************************************************************************************************

  @juzu.View(id = "a")
  public Response.Content a() {
    return Response.ok(A_.mA(bean).toString());
  }

  @juzu.View
  public Response.Content mA(B foo) throws IOException {
    return Response.ok(foo.getA());
  }

  @juzu.View(id = "aAction")
  public Response.Content aAction() {
    return Response.ok(A_.mAAction(bean).toString());
  }

  @Action
  public Response.View mAAction(B foo) throws IOException {
    Response.View r = A_.mA(foo);
    return r;
  }

  // ******************************************************************************************************************

  @juzu.View(id = "b")
  public Response.Content b() {
    return Response.ok(A_.mB(bean).toString());
  }

  @juzu.View
  public Response.Content mB(B foo) throws IOException {
    return Response.ok(Arrays.<String>asList((String[])foo.getB()).toString());
  }

  @juzu.View(id = "bAction")
  public Response.Content bAction() {
    return Response.ok(A_.mBAction(bean).toString());
  }

  @Action
  public Response.View mBAction(B foo) throws IOException {
    Response.View r = A_.mB(foo);
    return r;
  }

  // ******************************************************************************************************************

  @juzu.View(id = "c")
  public Response.Content c() {
    return Response.ok(A_.mC(bean).toString());
  }

  @juzu.View
  public Response.Content mC(B foo) throws IOException {
    return Response.ok(foo.getC().toString());
  }

  @juzu.View(id = "cAction")
  public Response.Content cAction() {
    return Response.ok(A_.mCAction(bean).toString());
  }

  @Action
  public Response.View mCAction(B foo) throws IOException {
    Response.View r = A_.mC(foo);
    return r;
  }

  // ******************************************************************************************************************

  @juzu.View(id = "d")
  public Response.Content d() {
    return Response.ok(A_.mD(bean).toString());
  }

  @juzu.View
  public Response.Content mD(B foo) throws IOException {
    return Response.ok(foo.d);
  }

  @juzu.View(id = "dAction")
  public Response.Content dAction() {
    return Response.ok(A_.mDAction(bean).toString());
  }

  @Action
  public Response.View mDAction(B foo) throws IOException {
    Response.View r = A_.mD(foo);
    return r;
  }

  // ******************************************************************************************************************

  @juzu.View(id = "e")
  public Response.Content e() {
    return Response.ok(A_.mE(bean).toString());
  }

  @juzu.View
  public Response.Content mE(B foo) throws IOException {
    return Response.ok(Arrays.<String>asList((String[])foo.e).toString());
  }

  @juzu.View(id = "eAction")
  public Response.Content eAction() {
    return Response.ok(A_.mEAction(bean).toString());
  }

  @Action
  public Response.View mEAction(B foo) throws IOException {
    Response.View r = A_.mE(foo);
    return r;
  }

  // ******************************************************************************************************************

  @juzu.View(id = "f")
  public Response.Content f() {
    return Response.ok(A_.mF(bean).toString());
  }

  @juzu.View
  public Response.Content mF(B foo) throws IOException {
    return Response.ok(foo.f.toString());
  }

  @juzu.View(id = "fAction")
  public Response.Content fAction() {
    return Response.ok(A_.mFAction(bean).toString());
  }

  @Action
  public Response.View mFAction(B foo) throws IOException {
    Response.View r = A_.mF(foo);
    return r;
  }

  // ******************************************************************************************************************

  @juzu.View(id = "g")
  public Response.Content g() {
    return Response.ok(A_.mG("s_value", bean).toString());
  }

  @juzu.View
  public Response.Content mG(String s, B foo) throws IOException {
    return Response.ok(s + foo.getA());
  }

  @juzu.View(id = "gAction")
  public Response.Content gAction() {
    return Response.ok(A_.mGAction("s_value", bean).toString());
  }

  @Action
  public Response.View mGAction(String s, B foo) throws IOException {
    Response.View r = A_.mG(s, foo);
    return r;
  }

  // ******************************************************************************************************************

  @juzu.View(id = "h")
  public Response.Content h() {
    return Response.ok(A_.mH("s_value", bean).toString());
  }

  @juzu.View
  public Response.Content mH(String a, B foo) throws IOException {
    return Response.ok(a + foo.getA());
  }

  @juzu.View(id = "hAction")
  public Response.Content hAction() {
    return Response.ok(A_.mHAction("s_value", bean).toString());
  }

  @Action
  public Response.View mHAction(String a, B foo) throws IOException {
    Response.View r = A_.mH(a, foo);
    return r;
  }

  // ******************************************************************************************************************

  @juzu.View(id = "i")
  public Response.Content i() {
    return Response.ok(A_.mI("s_value", bean).toString());
  }

  @juzu.View
  public Response.Content mI(String d, B foo) throws IOException {
    return Response.ok(d + foo.d);
  }

  @juzu.View(id = "iAction")
  public Response.Content iAction() {
    return Response.ok(A_.mIAction("s_value", bean).toString());
  }

  @Action
  public Response.View mIAction(String d, B foo) throws IOException {
    Response.View r = A_.mI(d, foo);
    return r;
  }
}
