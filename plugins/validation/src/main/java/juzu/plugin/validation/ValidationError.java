/*
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
package juzu.plugin.validation;

import juzu.Response;

import javax.validation.ConstraintViolation;
import java.util.Set;

/**
 * A specialized subclass for bean validation errors.
 *
 * @author Julien Viet
 */
public class ValidationError extends Response.Error {

  /** . */
  private final Set<ConstraintViolation<Object>> violations;

  public ValidationError(Set<ConstraintViolation<Object>> violations) {
    super(violations.toString());

    //
    this.violations = violations;
  }

  public Set<ConstraintViolation<Object>> getViolations() {
    return violations;
  }

  @Override
  public String getMessage() {
    return super.getMessage();
  }

  @Override
  public String getHtmlMessage() {
    StringBuilder buffer = new StringBuilder();
    for (ConstraintViolation<?> violation : violations) {
      buffer.append("<section>");
      buffer.append("<p>Property ").append(violation.getPropertyPath()).append(" ").append(violation.getMessage()).append("</p>");
      buffer.append("</section>");
    }
    return buffer.toString();
  }
}
