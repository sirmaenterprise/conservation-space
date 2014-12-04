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

package org.activiti.engine.impl.context;

import org.activiti.engine.impl.persistence.entity.DeploymentEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.runtime.InterpretableExecution;


// TODO: Auto-generated Javadoc
/**
 * The Class ExecutionContext.
 *
 * @author Tom Baeyens
 */
public class ExecutionContext {

  /** The execution. */
  protected ExecutionEntity execution;
  
  /**
   * Instantiates a new execution context.
   *
   * @param execution the execution
   */
  public ExecutionContext(InterpretableExecution execution) {
    this.execution = (ExecutionEntity) execution;
  }
  
  /**
   * Gets the execution.
   *
   * @return the execution
   */
  public ExecutionEntity getExecution() {
    return execution;
  }

  /**
   * Gets the process instance.
   *
   * @return the process instance
   */
  public ExecutionEntity getProcessInstance() {
    return execution.getProcessInstance();
  }

  /**
   * Gets the process definition.
   *
   * @return the process definition
   */
  public ProcessDefinitionEntity getProcessDefinition() {
    return (ProcessDefinitionEntity) execution.getProcessDefinition();
  }

  /**
   * Gets the deployment.
   *
   * @return the deployment
   */
  public DeploymentEntity getDeployment() {
    String deploymentId = getProcessDefinition().getDeploymentId();
    DeploymentEntity deployment = Context
      .getCommandContext()
      .getDeploymentManager()
      .findDeploymentById(deploymentId);
    return deployment;
  }
}
