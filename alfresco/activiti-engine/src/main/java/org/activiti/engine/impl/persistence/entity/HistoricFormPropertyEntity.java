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

package org.activiti.engine.impl.persistence.entity;

import org.activiti.engine.history.HistoricFormProperty;
import org.activiti.engine.impl.history.handler.ActivityInstanceEndHandler;
import org.activiti.engine.impl.util.ClockUtil;


// TODO: Auto-generated Javadoc
/**
 * The Class HistoricFormPropertyEntity.
 *
 * @author Tom Baeyens
 */
public class HistoricFormPropertyEntity extends HistoricDetailEntity implements HistoricFormProperty {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;
  
  /** The property id. */
  protected String propertyId;
  
  /** The property value. */
  protected String propertyValue;
  
  /**
   * Instantiates a new historic form property entity.
   */
  public HistoricFormPropertyEntity() {
  }

  /**
   * Instantiates a new historic form property entity.
   *
   * @param execution the execution
   * @param propertyId the property id
   * @param propertyValue the property value
   */
  public HistoricFormPropertyEntity(ExecutionEntity execution, String propertyId, String propertyValue) {
    this(execution, propertyId, propertyValue, null);
  }
  
  /**
   * Instantiates a new historic form property entity.
   *
   * @param execution the execution
   * @param propertyId the property id
   * @param propertyValue the property value
   * @param taskId the task id
   */
  public HistoricFormPropertyEntity(ExecutionEntity execution, String propertyId, String propertyValue, String taskId) {
    this.processInstanceId = execution.getProcessInstanceId();
    this.executionId = execution.getId();
    this.taskId = taskId;
    this.propertyId = propertyId;
    this.propertyValue = propertyValue;
    this.time = ClockUtil.getCurrentTime();

    HistoricActivityInstanceEntity historicActivityInstance = ActivityInstanceEndHandler.findActivityInstance(execution);
    if (historicActivityInstance!=null) {
      this.activityInstanceId = historicActivityInstance.getId();
    }
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricFormProperty#getPropertyId()
   */
  public String getPropertyId() {
    return propertyId;
  }
  
  /**
   * Sets the property id.
   *
   * @param propertyId the new property id
   */
  public void setPropertyId(String propertyId) {
    this.propertyId = propertyId;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricFormProperty#getPropertyValue()
   */
  public String getPropertyValue() {
    return propertyValue;
  }
  
  /**
   * Sets the property value.
   *
   * @param propertyValue the new property value
   */
  public void setPropertyValue(String propertyValue) {
    this.propertyValue = propertyValue;
  }
}
