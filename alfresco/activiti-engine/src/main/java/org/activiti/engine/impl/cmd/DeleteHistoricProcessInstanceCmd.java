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

package org.activiti.engine.impl.cmd;

import java.io.Serializable;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;

// TODO: Auto-generated Javadoc
/**
 * The Class DeleteHistoricProcessInstanceCmd.
 *
 * @author Frederik Heremans
 */
public class DeleteHistoricProcessInstanceCmd implements Command<Object>, Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;
  
  /** The process instance id. */
  protected String processInstanceId;

  /**
   * Instantiates a new delete historic process instance cmd.
   *
   * @param processInstanceId the process instance id
   */
  public DeleteHistoricProcessInstanceCmd(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.interceptor.Command#execute(org.activiti.engine.impl.interceptor.CommandContext)
   */
  public Object execute(CommandContext commandContext) {
    if (processInstanceId == null) {
      throw new ActivitiException("processInstanceId is null");
    }
    // Check if process instance is still running
    HistoricProcessInstance instance = commandContext
      .getHistoricProcessInstanceManager()
      .findHistoricProcessInstance(processInstanceId);
    
    if(instance == null) {
      throw new ActivitiException("No historic process instance found with id: " + processInstanceId);
    }
    if(instance.getEndTime() == null) {
      throw new ActivitiException("Process instance is still running, cannot delete historic process instance: " + processInstanceId);
    }
    
    commandContext
      .getHistoricProcessInstanceManager()
      .deleteHistoricProcessInstanceById(processInstanceId);
    
    return null;
  }

}
