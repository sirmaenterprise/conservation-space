/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.engine;


// TODO: Auto-generated Javadoc
/**
 * Runtime exception that is the superclass of all Activiti exceptions.
 * 
 * @author Tom Baeyens
 */
public class ActivitiException extends RuntimeException {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /**
   * Instantiates a new activiti exception.
   *
   * @param message the message
   * @param cause the cause
   */
  public ActivitiException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Instantiates a new activiti exception.
   *
   * @param message the message
   */
  public ActivitiException(String message) {
    super(message);
  }
}
