/*
 * Copyright (C) 2012 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package plugin.controller.method.parameters.bean;

import juzu.Action;
import juzu.Controller;
import juzu.Response;
import juzu.View;

import java.io.IOException;
import java.util.Arrays;

/** @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a> */
public class A extends Controller {
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

  @View(id = "a")
  public Response.Content a() {
    return Response.render($A.mA(bean).toString());
  }

  @View
  public Response.Content mA(B foo) throws IOException {
    return Response.ok(foo.getA());
  }

  @View(id = "aAction")
  public Response.Content aAction() {
    return Response.ok($A.mAAction(bean).toString());
  }

  @Action
  public Response.Update mAAction(B foo) throws IOException {
    Response.Update r = $A.mA(foo);
    return r;
  }

  // ******************************************************************************************************************

  @View(id = "b")
  public Response.Content b() {
    return Response.render($A.mB(bean).toString());
  }

  @View
  public Response.Content mB(B foo) throws IOException {
    return Response.ok(Arrays.<String>asList((String[])foo.getB()).toString());
  }

  @View(id = "bAction")
  public Response.Content bAction() {
    return Response.ok($A.mBAction(bean).toString());
  }

  @Action
  public Response.Update mBAction(B foo) throws IOException {
    Response.Update r = $A.mB(foo);
    return r;
  }

  // ******************************************************************************************************************

  @View(id = "c")
  public Response.Content c() {
    return Response.render($A.mC(bean).toString());
  }

  @View
  public Response.Content mC(B foo) throws IOException {
    return Response.ok(foo.getC().toString());
  }

  @View(id = "cAction")
  public Response.Content cAction() {
    return Response.ok($A.mCAction(bean).toString());
  }

  @Action
  public Response.Update mCAction(B foo) throws IOException {
    Response.Update r = $A.mC(foo);
    return r;
  }

  // ******************************************************************************************************************

  @View(id = "d")
  public Response.Content d() {
    return Response.render($A.mD(bean).toString());
  }

  @View
  public Response.Content mD(B foo) throws IOException {
    return Response.ok(foo.d);
  }

  @View(id = "dAction")
  public Response.Content dAction() {
    return Response.ok($A.mDAction(bean).toString());
  }

  @Action
  public Response.Update mDAction(B foo) throws IOException {
    Response.Update r = $A.mD(foo);
    return r;
  }

  // ******************************************************************************************************************

  @View(id = "e")
  public Response.Content e() {
    return Response.render($A.mE(bean).toString());
  }

  @View
  public Response.Content mE(B foo) throws IOException {
    return Response.ok(Arrays.<String>asList((String[])foo.e).toString());
  }

  @View(id = "eAction")
  public Response.Content eAction() {
    return Response.ok($A.mEAction(bean).toString());
  }

  @Action
  public Response.Update mEAction(B foo) throws IOException {
    Response.Update r = $A.mE(foo);
    return r;
  }

  // ******************************************************************************************************************

  @View(id = "f")
  public Response.Content f() {
    return Response.render($A.mF(bean).toString());
  }

  @View
  public Response.Content mF(B foo) throws IOException {
    return Response.ok(foo.f.toString());
  }

  @View(id = "fAction")
  public Response.Content fAction() {
    return Response.ok($A.mFAction(bean).toString());
  }

  @Action
  public Response.Update mFAction(B foo) throws IOException {
    Response.Update r = $A.mF(foo);
    return r;
  }

  // ******************************************************************************************************************

  @View(id = "g")
  public Response.Content g() {
    return Response.render($A.mG("s_value", bean).toString());
  }

  @View
  public Response.Content mG(String s, B foo) throws IOException {
    return Response.ok(s + foo.getA());
  }

  @View(id = "gAction")
  public Response.Content gAction() {
    return Response.ok($A.mGAction("s_value", bean).toString());
  }

  @Action
  public Response.Update mGAction(String s, B foo) throws IOException {
    Response.Update r = $A.mG(s, foo);
    return r;
  }

  // ******************************************************************************************************************

  @View(id = "h")
  public Response.Content h() {
    return Response.render($A.mH("s_value", bean).toString());
  }

  @View
  public Response.Content mH(String a, B foo) throws IOException {
    return Response.ok(a + foo.getA());
  }

  @View(id = "hAction")
  public Response.Content hAction() {
    return Response.ok($A.mHAction("s_value", bean).toString());
  }

  @Action
  public Response.Update mHAction(String a, B foo) throws IOException {
    Response.Update r = $A.mH(a, foo);
    return r;
  }

  // ******************************************************************************************************************

  @View(id = "i")
  public Response.Content i() {
    return Response.render($A.mI("s_value", bean).toString());
  }

  @View
  public Response.Content mI(String d, B foo) throws IOException {
    return Response.ok(d + foo.d);
  }

  @View(id = "iAction")
  public Response.Content iAction() {
    return Response.ok($A.mIAction("s_value", bean).toString());
  }

  @Action
  public Response.Update mIAction(String d, B foo) throws IOException {
    Response.Update r = $A.mI(d, foo);
    return r;
  }
}
