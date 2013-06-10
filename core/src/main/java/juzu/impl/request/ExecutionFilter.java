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
package juzu.impl.request;

/**
 * Implementors of the execution filter are aware of the activities during the execution of a request.
 *
 * @author Julien Viet
 */
public interface ExecutionFilter {

  /**
   * Provide the opportunity to wrap a command within a new command. If the filter does not want to
   * perform any task, it can merely return the provided command.
   *
   *
   * @param command the command to wrap
   * @return the wrapped command
   */
  Runnable onCommand(Runnable command);

}
