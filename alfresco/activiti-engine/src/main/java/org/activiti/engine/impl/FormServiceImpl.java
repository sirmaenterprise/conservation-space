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

package org.activiti.engine.impl;

import java.util.Map;

import org.activiti.engine.FormService;
import org.activiti.engine.form.StartFormData;
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.impl.cmd.GetRenderedStartFormCmd;
import org.activiti.engine.impl.cmd.GetRenderedTaskFormCmd;
import org.activiti.engine.impl.cmd.GetStartFormCmd;
import org.activiti.engine.impl.cmd.GetTaskFormCmd;
import org.activiti.engine.impl.cmd.SubmitStartFormCmd;
import org.activiti.engine.impl.cmd.SubmitTaskFormCmd;
import org.activiti.engine.runtime.ProcessInstance;


// TODO: Auto-generated Javadoc
/**
 * The Class FormServiceImpl.
 *
 * @author Tom Baeyens
 */
public class FormServiceImpl extends ServiceImpl implements FormService {

  /* (non-Javadoc)
   * @see org.activiti.engine.FormService#getRenderedStartForm(java.lang.String)
   */
  public Object getRenderedStartForm(String processDefinitionId) {
    return commandExecutor.execute(new GetRenderedStartFormCmd(processDefinitionId, null));
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.FormService#getRenderedStartForm(java.lang.String, java.lang.String)
   */
  public Object getRenderedStartForm(String processDefinitionId, String engineName) {
    return commandExecutor.execute(new GetRenderedStartFormCmd(processDefinitionId, engineName));
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.FormService#getRenderedTaskForm(java.lang.String)
   */
  public Object getRenderedTaskForm(String taskId) {
    return commandExecutor.execute(new GetRenderedTaskFormCmd(taskId, null));
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.FormService#getRenderedTaskForm(java.lang.String, java.lang.String)
   */
  public Object getRenderedTaskForm(String taskId, String engineName) {
    return commandExecutor.execute(new GetRenderedTaskFormCmd(taskId, engineName));
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.FormService#getStartFormData(java.lang.String)
   */
  public StartFormData getStartFormData(String processDefinitionId) {
    return commandExecutor.execute(new GetStartFormCmd(processDefinitionId));
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.FormService#getTaskFormData(java.lang.String)
   */
  public TaskFormData getTaskFormData(String taskId) {
    return commandExecutor.execute(new GetTaskFormCmd(taskId));
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.FormService#submitStartFormData(java.lang.String, java.util.Map)
   */
  public ProcessInstance submitStartFormData(String processDefinitionId, Map<String, String> properties) {
    return commandExecutor.execute(new SubmitStartFormCmd(processDefinitionId, null, properties));
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.FormService#submitStartFormData(java.lang.String, java.lang.String, java.util.Map)
   */
  public ProcessInstance submitStartFormData(String processDefinitionId, String businessKey, Map<String, String> properties) {
	  return commandExecutor.execute(new SubmitStartFormCmd(processDefinitionId, businessKey, properties));
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.FormService#submitTaskFormData(java.lang.String, java.util.Map)
   */
  public void submitTaskFormData(String taskId, Map<String, String> properties) {
    commandExecutor.execute(new SubmitTaskFormCmd(taskId, properties));
  }
}
