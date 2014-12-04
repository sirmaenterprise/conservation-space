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
import org.activiti.engine.form.StartFormData;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.form.FormEngine;
import org.activiti.engine.impl.form.StartFormHandler;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;


// TODO: Auto-generated Javadoc
/**
 * The Class GetRenderedStartFormCmd.
 *
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class GetRenderedStartFormCmd implements Command<Object>, Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;
  
  /** The process definition id. */
  protected String processDefinitionId;
  
  /** The form engine name. */
  protected String formEngineName;
  
  /**
   * Instantiates a new gets the rendered start form cmd.
   *
   * @param processDefinitionId the process definition id
   * @param formEngineName the form engine name
   */
  public GetRenderedStartFormCmd(String processDefinitionId, String formEngineName) {
    this.processDefinitionId = processDefinitionId;
    this.formEngineName = formEngineName;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.interceptor.Command#execute(org.activiti.engine.impl.interceptor.CommandContext)
   */
  public Object execute(CommandContext commandContext) {
    ProcessDefinitionEntity processDefinition = Context
      .getProcessEngineConfiguration()
      .getDeploymentCache()
      .findDeployedProcessDefinitionById(processDefinitionId);
    if (processDefinition == null) {
      throw new ActivitiException("Process Definition '" + processDefinitionId +"' not found");
    }
    StartFormHandler startFormHandler = processDefinition.getStartFormHandler();
    if (startFormHandler == null) {
      return null;
    }
    
    FormEngine formEngine = Context
      .getProcessEngineConfiguration()
      .getFormEngines()
      .get(formEngineName);
    
    if (formEngine==null) {
      throw new ActivitiException("No formEngine '" + formEngineName +"' defined process engine configuration");
    }
    
    StartFormData startForm = startFormHandler.createStartFormData(processDefinition);
    
    return formEngine.renderStartForm(startForm);
  }
}
