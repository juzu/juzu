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

package juzu.impl.compiler;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ProcessingException extends RuntimeException implements Iterable<Message> {

  /** . */
  private final List<Message> messages;

  /** . */
  private final Element element;

  /** . */
  private final AnnotationMirror annotation;

  public ProcessingException(MessageCode code, Object... arguments) {
    this(null, code, arguments);
  }

  public ProcessingException(Element element, MessageCode messageCode, Object... arguments) {
    this(element, null, messageCode, arguments);
  }

  public ProcessingException(List<Message> messages) {
    this(null, null, messages);
  }

  public ProcessingException(Element element, List<Message> messages) {
    this(element, null, messages);
  }

  public ProcessingException(Element element, AnnotationMirror annotation, MessageCode code, Object... arguments) {
    this(element, annotation, Collections.singletonList(new Message(code, arguments)));
  }

  public ProcessingException(Element element, AnnotationMirror annotation, List<Message> messages) {
    this.element = element;
    this.annotation = annotation;
    this.messages = messages;
  }

  public Iterator<Message> iterator() {
    return messages.iterator();
  }

  @Override
  public ProcessingException initCause(Throwable cause) {
    return (ProcessingException)super.initCause(cause);
  }

  public Element getElement() {
    return element;
  }

  public AnnotationMirror getAnnotation() {
    return annotation;
  }

  public List<Message> getMessages() {
    return messages;
  }
}
