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

package org.activiti.engine.delegate;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.bpmn.parser.Error;


// TODO: Auto-generated Javadoc
/**
 * Special exception that can be used to throw a BPMN Error from.
 *
 * {@link JavaDelegate}s.
 * 
 * This should only be used for business faults, which shall be handled by a
 * Boundary Error Event or Error Event Sub-Process modeled in the process
 * definition. Technical errors should be represented by other exception types.
 * 
 * This class represents an actual instance of a BPMN Error, whereas
 * {@link Error} represents an Error definition.
 * @author Falko Menge
 */
public class BpmnError extends ActivitiException {
  
  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** The error code. */
  private String errorCode;

  /**
   * Instantiates a new bpmn error.
   *
   * @param errorCode the error code
   */
  public BpmnError(String errorCode) {
    super("");
    setErrorCode(errorCode);
  }
          
  /**
   * Instantiates a new bpmn error.
   *
   * @param errorCode the error code
   * @param message the message
   */
  public BpmnError(String errorCode, String message) {
    super(message + " (errorCode='" + errorCode + "')");
    setErrorCode(errorCode);
  }

  /**
   * Sets the error code.
   *
   * @param errorCode the new error code
   */
  protected void setErrorCode(String errorCode) {
    if (errorCode == null) {
      throw new ActivitiException("Error Code must not be null.");
    }
    if (errorCode.length() < 1) {
      throw new ActivitiException("Error Code must not be empty.");
    }
    this.errorCode = errorCode;
  }

  /**
   * Gets the error code.
   *
   * @return the error code
   */
  public String getErrorCode() {
    return errorCode;
  }

  /* (non-Javadoc)
   * @see java.lang.Throwable#toString()
   */
  public String toString() {
    return super.toString() + " (errorCode='" + errorCode + "')";
  }

}
