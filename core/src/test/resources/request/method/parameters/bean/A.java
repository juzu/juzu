/*
 * Copyright (C) 2011 eXo Platform SAS.
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

package request.method.parameters.bean;

import org.juzu.Action;
import org.juzu.Controller;
import org.juzu.Response;
import org.juzu.View;

import java.io.IOException;
import java.util.Arrays;

/** @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a> */
public class A extends Controller
{
   private B bean;

   public A()
   {
      bean = new B();
      bean.setA("v");
      bean.setB(new String[]{"v1", "v2"});
      bean.setC(Arrays.asList("v1", "v2"));
      bean.d = "v";
      bean.e = new String[]{"v1","v2"};
      bean.f = Arrays.asList("v1","v2");
   }

  @View(id = "a")
   public Response.Content a()
   {
      return Response.render(A_.mAURL(bean).toString());
   }

   @View(id = "b")
   public Response.Content b()
   {
      return Response.render(A_.mBURL(bean).toString());
   }

   @View(id = "c")
   public Response.Content c()
   {
      return Response.render(A_.mCURL(bean).toString());
   }

   @View(id = "d")
   public Response.Content d()
   {
      return Response.render(A_.mDURL(bean).toString());
   }

   @View(id = "e")
   public Response.Content e()
   {
      return Response.render(A_.mEURL(bean).toString());
   }

   @View(id = "f")
   public Response.Content f()
   {
      return Response.render(A_.mFURL(bean).toString());
   }

   @View(id = "aAction")
   public Response.Content aAction()
   {
      return Response.content(A_.mAActionURL(bean).toString());
   }

   @View(id = "bAction")
   public Response.Content bAction()
   {
      return Response.content(A_.mBActionURL(bean).toString());
   }

   @View(id = "cAction")
   public Response.Content cAction()
   {
      return Response.content(A_.mCActionURL(bean).toString());
   }
   @View(id = "dAction")
   public Response.Content dAction()
   {
      return Response.content(A_.mDActionURL(bean).toString());
   }

   @View(id = "eAction")
   public Response.Content eAction()
   {
      return Response.content(A_.mEActionURL(bean).toString());
   }

   @View(id = "fAction")
   public Response.Content fAction()
   {
      return Response.content(A_.mFActionURL(bean).toString());
   }

   @View
   public Response.Content mA(B foo) throws IOException
   {
      return Response.content(foo.getA());
   }

   @View
   public Response.Content mB(B foo) throws IOException
   {
      return Response.content(Arrays.<String>asList((String[]) foo.getB()).toString());
   }

   @View
   public Response.Content mC(B foo) throws IOException
   {
      return Response.content(foo.getC().toString());
   }

   @View
   public Response.Content mD(B foo) throws IOException
   {
      return Response.content(foo.d);
   }

   @View
   public Response.Content mE(B foo) throws IOException
   {
      return Response.content(Arrays.<String>asList((String[]) foo.e).toString());
   }

   @View
   public Response.Content mF(B foo) throws IOException
   {
      return Response.content(foo.f.toString());
   }

   // Actions
   @Action
   public Response.Update mAAction(B foo) throws IOException
   {
      Response.Update r =  A_.mA(foo);
      return r;
   }

   @Action
   public Response.Update mBAction(B foo) throws IOException
   {
      Response.Update r =  A_.mB(foo);
      return r;
   }

   @Action
   public Response.Update mCAction(B foo) throws IOException
   {
      Response.Update r =  A_.mC(foo);
      return r;
   }

   @Action
   public Response.Update mDAction(B foo) throws IOException
   {
      Response.Update r =  A_.mD(foo);
      return r;
   }

   @Action
   public Response.Update mEAction(B foo) throws IOException
   {
      Response.Update r =  A_.mE(foo);
      return r;
   }

   @Action
   public Response.Update mFAction(B foo) throws IOException
   {
      Response.Update r =  A_.mF(foo);
      return r;
   }

}
