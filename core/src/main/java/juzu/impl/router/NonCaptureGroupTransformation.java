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
