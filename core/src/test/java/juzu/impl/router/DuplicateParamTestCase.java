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

package juzu.impl.router;

import org.junit.Test;

import static juzu.impl.router.metadata.DescriptorBuilder.*;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class DuplicateParamTestCase extends AbstractControllerTestCase {

  @Test
  public void testPathParamDuplicatesRequestParam() throws Exception {
    try {
      router().add(
          route("/").with(requestParam("foo").named("a")).
              sub(route("/{foo}"))).build();
      fail();
    }
    catch (MalformedRouteException e) {
    }

    //
    try {
      router().add(
          route("/").with(requestParam("foo").named("a")).
              sub(route("/bar").sub(route("/{foo}")))).build();
      fail();
    }
    catch (MalformedRouteException e) {
    }
  }

  @Test
  public void testPathParamDuplicatesRouteParam() throws Exception {
    try {
      router().add(
          route("/").with(routeParam("foo").withValue("bar")).
              sub(route("/{foo}"))).build();
      fail();
    }
    catch (MalformedRouteException e) {
    }

    //
    try {
      router().add(
          route("/").with(routeParam("foo").withValue("bar")).
              sub(route("/bar").sub(route("/{foo}")))).build();
      fail();
    }
    catch (MalformedRouteException e) {
    }
  }

  @Test
  public void testPathParamDuplicatesPathParam() throws Exception {
    try {
      router().add(
          route("/{foo}").
              sub(route("/{foo}"))).build();
      fail();
    }
    catch (MalformedRouteException e) {
    }

    //
    try {
      router().add(
          route("/{foo}").
              sub(route("/bar").sub(route("/{foo}")))).build();
      fail();
    }
    catch (MalformedRouteException e) {
    }
  }

  @Test
  public void testRequestParamDuplicatesRequestParam() throws Exception {
    try {
      router().add(
          route("/").with(requestParam("foo").named("a")).
              sub(route("/bar").with(routeParam("foo").withValue("b")))).build();
      fail();
    }
    catch (MalformedRouteException e) {
    }

    //
    try {
      router().add(
          route("/").with(requestParam("foo").named("a")).
              sub(route("/bar").sub(route("/foo").with(routeParam("foo").withValue("b"))))).build();
      fail();
    }
    catch (MalformedRouteException e) {
    }
  }

  @Test
  public void testRequestParamDuplicatesRouteParam() throws Exception {
    try {
      router().add(
          route("/").with(routeParam("foo").withValue("bar")).
              sub(route("/bar").with(routeParam("foo").withValue("b")))).build();
      fail();
    }
    catch (MalformedRouteException e) {
    }

    //
    try {
      router().add(
          route("/").with(routeParam("foo").withValue("bar")).
              sub(route("/bar").sub(route("/foo").with(routeParam("foo").withValue("b"))))).build();
      fail();
    }
    catch (MalformedRouteException e) {
    }
  }

  @Test
  public void testRequestParamDuplicatesPathParam() throws Exception {
    try {
      router().add(
          route("/{foo}").
              sub(route("/bar").with(routeParam("foo").withValue("b")))).build();
      fail();
    }
    catch (MalformedRouteException e) {
    }

    //
    try {
      router().add(
          route("/{foo}").
              sub(route("/bar").sub(route("/foo").with(routeParam("foo").withValue("b"))))).build();
      fail();
    }
    catch (MalformedRouteException e) {
    }
  }

  @Test
  public void testRouteParamDuplicatesRequestParam() throws Exception {
    try {
      router().add(
          route("/").with(requestParam("foo").named("a")).
              sub(route("/bar").with(routeParam("foo").withValue("b")))).build();
      fail();
    }
    catch (MalformedRouteException e) {
    }

    //
    try {
      router().add(
          route("/").with(requestParam("foo").named("a")).
              sub(route("/bar").sub(route("/foo").with(routeParam("foo").withValue("b"))))).build();
      fail();
    }
    catch (MalformedRouteException e) {
    }
  }

  @Test
  public void testRouteParamDuplicatesRouteParam() throws Exception {
    try {
      router().add(
          route("/").with(routeParam("foo").withValue("bar")).
              sub(route("/bar").with(routeParam("foo").withValue("b")))).build();
      fail();
    }
    catch (MalformedRouteException e) {
    }

    //
    try {
      router().add(
          route("/").with(routeParam("foo").withValue("bar")).
              sub(route("/bar").sub(route("/foo").with(routeParam("foo").withValue("b"))))).build();
      fail();
    }
    catch (MalformedRouteException e) {
    }
  }

  @Test
  public void testRouteParamDuplicatesPathParam() throws Exception {
    try {
      router().add(
          route("/{foo}").
              sub(route("/bar").with(routeParam("foo").withValue("b")))).build();
      fail();
    }
    catch (MalformedRouteException e) {
    }

    //
    try {
      router().add(
          route("/{foo}").
              sub(route("/bar").sub(route("/foo").with(routeParam("foo").withValue("b"))))).build();
      fail();
    }
    catch (MalformedRouteException e) {
    }
  }
}
