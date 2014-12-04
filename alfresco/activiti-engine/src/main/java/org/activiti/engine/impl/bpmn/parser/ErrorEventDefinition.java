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

package org.activiti.engine.impl.bpmn.parser;

import java.io.Serializable;
import java.util.Comparator;

// TODO: Auto-generated Javadoc
/**
 * The Class ErrorEventDefinition.
 *
 * @author Daniel Meyer
 */
public class ErrorEventDefinition implements Serializable {
  
  /** The comparator. */
  public static Comparator<ErrorEventDefinition> comparator = new Comparator<ErrorEventDefinition>() {
    public int compare(ErrorEventDefinition o1, ErrorEventDefinition o2) {
      return o2.getPrecedence().compareTo(o1.getPrecedence());
    }    
  };

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** The handler activity id. */
  protected final String handlerActivityId;
  
  /** The error code. */
  protected String errorCode;
  
  /** The precedence. */
  protected Integer precedence =0;

  /**
   * Instantiates a new error event definition.
   *
   * @param handlerActivityId the handler activity id
   */
  public ErrorEventDefinition(String handlerActivityId) {
    this.handlerActivityId=handlerActivityId;
  }

  /**
   * Gets the error code.
   *
   * @return the error code
   */
  public String getErrorCode() {
    return errorCode;
  }

  /**
   * Sets the error code.
   *
   * @param errorCode the new error code
   */
  public void setErrorCode(String errorCode) {
    this.errorCode = errorCode;
  }

  /**
   * Gets the handler activity id.
   *
   * @return the handler activity id
   */
  public String getHandlerActivityId() {
    return handlerActivityId;
  }

  /**
   * Gets the precedence.
   *
   * @return the precedence
   */
  public Integer getPrecedence() {
    // handlers with error code take precedence over catchall-handlers
    return precedence + (errorCode != null ? 1 : 0);
  }

  /**
   * Sets the precedence.
   *
   * @param precedence the new precedence
   */
  public void setPrecedence(Integer precedence) {
    this.precedence = precedence;
  }

  /**
   * Catches.
   *
   * @param errorCode the error code
   * @return true, if successful
   */
  public boolean catches(String errorCode) {
    return errorCode == null || this.errorCode == null || this.errorCode.equals(errorCode) ;
  }

}
