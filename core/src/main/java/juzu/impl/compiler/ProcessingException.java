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

package juzu.impl.compiler;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import java.util.Arrays;
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

  public ProcessingException(Element element, MessageCode code, Object... arguments) {
    this(element, null, code, arguments);
  }

  public ProcessingException(Element element, AnnotationMirror annotation, MessageCode code, Object... arguments) {
    this(element, annotation, new Message(code, arguments));
  }

  public ProcessingException(Element element, AnnotationMirror annotation, Message... messages) {
    this(element, annotation, Arrays.asList(messages));
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
  public synchronized ProcessingException initCause(Throwable cause) {
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
