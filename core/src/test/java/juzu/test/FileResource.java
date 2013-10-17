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
package juzu.test;

import juzu.impl.common.Content;
import juzu.impl.fs.spi.ReadWriteFileSystem;

/** @author Julien Viet */
public class FileResource<I> {

  /** . */
  final ReadWriteFileSystem<I> sourcePath;

  /** . */
  final I path;

  public FileResource(ReadWriteFileSystem<I> sourcePath, I path) {
    this.sourcePath = sourcePath;
    this.path = path;
  }

  public String assertContent() {
    try {
      Content content = sourcePath.getContent(path).getObject();
      return content.getCharSequence().toString();
    }
    catch (Exception e) {
      throw AbstractTestCase.failure(e);
    }
  }

  public void assertTouch() {
    try {
      Content content = sourcePath.getContent(path).getObject();
      sourcePath.setContent(path, content);
    }
    catch (Exception e) {
      throw AbstractTestCase.failure(e);
    }
  }

  public void assertSave(String content) {
    try {
      sourcePath.setContent(path, new Content(content));
    }
    catch (Exception e) {
      throw AbstractTestCase.failure(e);
    }
  }

  public void assertRemove() {
    try {
      sourcePath.removePath(path);
    }
    catch (Exception e) {
      throw AbstractTestCase.failure(e);
    }
  }
}
