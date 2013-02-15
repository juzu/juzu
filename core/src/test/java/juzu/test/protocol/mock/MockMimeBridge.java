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

package juzu.test.protocol.mock;

import juzu.Response;
import juzu.impl.plugin.application.Application;
import juzu.impl.bridge.spi.MimeBridge;
import juzu.impl.common.MethodHandle;
import juzu.io.AppendableStream;
import juzu.io.BinaryOutputStream;
import juzu.io.Stream;
import juzu.test.AbstractTestCase;
import org.junit.Assert;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class MockMimeBridge extends MockRequestBridge implements MimeBridge {

  public MockMimeBridge(Application application, MockClient client, MethodHandle target, Map<String, String[]> parameters) {
    super(application, client, target, parameters);
  }

  public String assertStringResult() {
    Response.Content<?> content = AbstractTestCase.assertInstanceOf(Response.Content.class, response);
    AbstractTestCase.assertEquals(Stream.Char.class, content.getKind());
    try {
      StringBuilder builder = new StringBuilder();
      ((Response.Content<Stream.Char>)content).send(new AppendableStream(builder));
      return builder.toString();
    }
    catch (IOException e) {
      throw AbstractTestCase.failure(e);
    }
  }

  public byte[] assertBinaryResult() {
    Response.Content<?> content = AbstractTestCase.assertInstanceOf(Response.Content.class, response);
    AbstractTestCase.assertEquals(Stream.Binary.class, content.getKind());
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      BinaryOutputStream bos = new BinaryOutputStream(baos);
      ((Response.Content<Stream.Binary>)content).send(bos);
      return baos.toByteArray();
    }
    catch (IOException e) {
      throw AbstractTestCase.failure(e);
    }
  }

  public String getMimeType() {
    if (response instanceof Response.Content<?>) {
      return ((Response.Content)response).getMimeType();
    } else {
      return null;
    }
  }

  public void assertOk() {
    assertStatus(200);
  }

  public void assertNotFound() {
    assertStatus(404);
  }

  public void assertStatus(int status) {
    Response.Content<?> content = AbstractTestCase.assertInstanceOf(Response.Content.class, response);
    Assert.assertNotNull(content.getStatus());
    Assert.assertEquals((Integer)status, content.getStatus());
  }
}
