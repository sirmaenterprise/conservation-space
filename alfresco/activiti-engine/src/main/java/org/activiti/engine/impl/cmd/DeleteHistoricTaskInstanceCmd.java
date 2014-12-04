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
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;

// TODO: Auto-generated Javadoc
/**
 * The Class DeleteHistoricTaskInstanceCmd.
 *
 * @author Tom Baeyens
 */
public class DeleteHistoricTaskInstanceCmd implements Command<Object>, Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;
  
  /** The task id. */
  protected String taskId;

  /**
   * Instantiates a new delete historic task instance cmd.
   *
   * @param taskId the task id
   */
  public DeleteHistoricTaskInstanceCmd(String taskId) {
    this.taskId = taskId;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.interceptor.Command#execute(org.activiti.engine.impl.interceptor.CommandContext)
   */
  public Object execute(CommandContext commandContext) {

    if (taskId == null) {
      throw new ActivitiException("taskId is null");
    }
    commandContext
      .getHistoricTaskInstanceManager()
      .deleteHistoricTaskInstanceById(taskId);
    return null;
  }

}
