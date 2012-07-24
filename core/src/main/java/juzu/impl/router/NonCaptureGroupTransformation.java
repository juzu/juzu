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

package juzu.impl.router;

import juzu.impl.router.regex.GroupType;
import juzu.impl.router.regex.RENode;
import juzu.impl.router.regex.REVisitor;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
class NonCaptureGroupTransformation extends REVisitor<RuntimeException> {

  NonCaptureGroupTransformation() {
  }

  @Override
  protected void visit(RENode.Disjunction disjunction) throws RuntimeException {
    super.visit(disjunction);

    // Wrap the content of the top disjunction with a capturing group
    if (disjunction.getParent() == null) {
      if (disjunction.hasAlternative()) {
        RENode.Alternative alternative = disjunction.getAlternative();
        disjunction.clearAlternative();
        if (disjunction.hasNext()) {
          RENode.Disjunction next = disjunction.getNext();
          disjunction.clearNext();
          RENode.Alternative group = new RENode.Alternative(
              new RENode.Group(
                  new RENode.Disjunction(alternative, next), GroupType.CAPTURING_GROUP));
          disjunction.setAlternative(group);
        }
        else {
          RENode.Alternative group = new RENode.Alternative(
              new RENode.Group(
                  new RENode.Disjunction(alternative), GroupType.CAPTURING_GROUP));
          disjunction.setAlternative(group);
        }
      }
      else {
        if (disjunction.hasNext()) {
          RENode.Disjunction next = disjunction.getNext();
          disjunction.clearNext();
          RENode.Alternative group = new RENode.Alternative(
              new RENode.Group(
                  new RENode.Disjunction(next), GroupType.CAPTURING_GROUP));
          disjunction.setAlternative(group);
        }
        else {
          RENode.Alternative group = new RENode.Alternative(
              new RENode.Group(
                  new RENode.Disjunction(), GroupType.CAPTURING_GROUP));
          disjunction.setAlternative(group);
        }
      }
    }
  }

  @Override
  protected void visit(RENode.Group expr) throws RuntimeException {
    // We make any capturing group as non capturing
    if (expr.getType() == GroupType.CAPTURING_GROUP) {
      expr.setType(GroupType.NON_CAPTURING_GROUP);
    }

    //
    super.visit(expr);
  }
}
