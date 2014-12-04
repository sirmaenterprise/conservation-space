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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.form.FormData;
import org.activiti.engine.impl.cmd.ActivateProcessInstanceCmd;
import org.activiti.engine.impl.cmd.DeleteProcessInstanceCmd;
import org.activiti.engine.impl.cmd.FindActiveActivityIdsCmd;
import org.activiti.engine.impl.cmd.GetExecutionVariableCmd;
import org.activiti.engine.impl.cmd.GetExecutionVariablesCmd;
import org.activiti.engine.impl.cmd.GetStartFormCmd;
import org.activiti.engine.impl.cmd.MessageEventReceivedCmd;
import org.activiti.engine.impl.cmd.SetExecutionVariablesCmd;
import org.activiti.engine.impl.cmd.SignalCmd;
import org.activiti.engine.impl.cmd.SignalEventReceivedCmd;
import org.activiti.engine.impl.cmd.StartProcessInstanceByMessageCmd;
import org.activiti.engine.impl.cmd.StartProcessInstanceCmd;
import org.activiti.engine.impl.cmd.SuspendProcessInstanceCmd;
import org.activiti.engine.runtime.ExecutionQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;

// TODO: Auto-generated Javadoc
/**
 * The Class RuntimeServiceImpl.
 *
 * @author Tom Baeyens
 * @author Daniel Meyer
 */
public class RuntimeServiceImpl extends ServiceImpl implements RuntimeService {

  /* (non-Javadoc)
   * @see org.activiti.engine.RuntimeService#startProcessInstanceByKey(java.lang.String)
   */
  public ProcessInstance startProcessInstanceByKey(String processDefinitionKey) {
    return commandExecutor.execute(new StartProcessInstanceCmd<ProcessInstance>(processDefinitionKey, null, null, null));
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.RuntimeService#startProcessInstanceByKey(java.lang.String, java.lang.String)
   */
  public ProcessInstance startProcessInstanceByKey(String processDefinitionKey, String businessKey) {
    return commandExecutor.execute(new StartProcessInstanceCmd<ProcessInstance>(processDefinitionKey, null, businessKey, null));
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.RuntimeService#startProcessInstanceByKey(java.lang.String, java.util.Map)
   */
  public ProcessInstance startProcessInstanceByKey(String processDefinitionKey, Map<String, Object> variables) {
    return commandExecutor.execute(new StartProcessInstanceCmd<ProcessInstance>(processDefinitionKey, null, null, variables));
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.RuntimeService#startProcessInstanceByKey(java.lang.String, java.lang.String, java.util.Map)
   */
  public ProcessInstance startProcessInstanceByKey(String processDefinitionKey, String businessKey, Map<String, Object> variables) {
    return commandExecutor.execute(new StartProcessInstanceCmd<ProcessInstance>(processDefinitionKey, null, businessKey, variables));
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.RuntimeService#startProcessInstanceById(java.lang.String)
   */
  public ProcessInstance startProcessInstanceById(String processDefinitionId) {
    return commandExecutor.execute(new StartProcessInstanceCmd<ProcessInstance>(null, processDefinitionId, null, null));
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.RuntimeService#startProcessInstanceById(java.lang.String, java.lang.String)
   */
  public ProcessInstance startProcessInstanceById(String processDefinitionId, String businessKey) {
    return commandExecutor.execute(new StartProcessInstanceCmd<ProcessInstance>(null, processDefinitionId, businessKey, null));
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.RuntimeService#startProcessInstanceById(java.lang.String, java.util.Map)
   */
  public ProcessInstance startProcessInstanceById(String processDefinitionId, Map<String, Object> variables) {
    return commandExecutor.execute(new StartProcessInstanceCmd<ProcessInstance>(null, processDefinitionId, null, variables));
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.RuntimeService#startProcessInstanceById(java.lang.String, java.lang.String, java.util.Map)
   */
  public ProcessInstance startProcessInstanceById(String processDefinitionId, String businessKey, Map<String, Object> variables) {
    return commandExecutor.execute(new StartProcessInstanceCmd<ProcessInstance>(null, processDefinitionId, businessKey, variables));
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.RuntimeService#deleteProcessInstance(java.lang.String, java.lang.String)
   */
  public void deleteProcessInstance(String processInstanceId, String deleteReason) {
    commandExecutor.execute(new DeleteProcessInstanceCmd(processInstanceId, deleteReason));
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.RuntimeService#createExecutionQuery()
   */
  public ExecutionQuery createExecutionQuery() {
    return new ExecutionQueryImpl(commandExecutor);
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.RuntimeService#getVariables(java.lang.String)
   */
  public Map<String, Object> getVariables(String executionId) {
    return commandExecutor.execute(new GetExecutionVariablesCmd(executionId, null, false));
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.RuntimeService#getVariablesLocal(java.lang.String)
   */
  public Map<String, Object> getVariablesLocal(String executionId) {
    return commandExecutor.execute(new GetExecutionVariablesCmd(executionId, null, true));
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.RuntimeService#getVariables(java.lang.String, java.util.Collection)
   */
  public Map<String, Object> getVariables(String executionId, Collection<String> variableNames) {
    return commandExecutor.execute(new GetExecutionVariablesCmd(executionId, variableNames, false));
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.RuntimeService#getVariablesLocal(java.lang.String, java.util.Collection)
   */
  public Map<String, Object> getVariablesLocal(String executionId, Collection<String> variableNames) {
    return commandExecutor.execute(new GetExecutionVariablesCmd(executionId, variableNames, true));
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.RuntimeService#getVariable(java.lang.String, java.lang.String)
   */
  public Object getVariable(String executionId, String variableName) {
    return commandExecutor.execute(new GetExecutionVariableCmd(executionId, variableName, false));
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.RuntimeService#getVariableLocal(java.lang.String, java.lang.String)
   */
  public Object getVariableLocal(String executionId, String variableName) {
    return commandExecutor.execute(new GetExecutionVariableCmd(executionId, variableName, true));
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.RuntimeService#setVariable(java.lang.String, java.lang.String, java.lang.Object)
   */
  public void setVariable(String executionId, String variableName, Object value) {
    if(variableName == null) {
      throw new ActivitiException("variableName is null");
    }
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put(variableName, value);
    commandExecutor.execute(new SetExecutionVariablesCmd(executionId, variables, false));
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.RuntimeService#setVariableLocal(java.lang.String, java.lang.String, java.lang.Object)
   */
  public void setVariableLocal(String executionId, String variableName, Object value) {
    if(variableName == null) {
      throw new ActivitiException("variableName is null");
    }
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put(variableName, value);
    commandExecutor.execute(new SetExecutionVariablesCmd(executionId, variables, true));
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.RuntimeService#setVariables(java.lang.String, java.util.Map)
   */
  public void setVariables(String executionId, Map<String, ? extends Object> variables) {
    commandExecutor.execute(new SetExecutionVariablesCmd(executionId, variables, false));
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.RuntimeService#setVariablesLocal(java.lang.String, java.util.Map)
   */
  public void setVariablesLocal(String executionId, Map<String, ? extends Object> variables) {
    commandExecutor.execute(new SetExecutionVariablesCmd(executionId, variables, true));
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.RuntimeService#signal(java.lang.String)
   */
  public void signal(String executionId) {
    commandExecutor.execute(new SignalCmd(executionId, null, null, null));
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.RuntimeService#signal(java.lang.String, java.util.Map)
   */
  public void signal(String executionId, Map<String, Object> processVariables) {
    commandExecutor.execute(new SignalCmd(executionId, null, null, processVariables));
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.RuntimeService#createProcessInstanceQuery()
   */
  public ProcessInstanceQuery createProcessInstanceQuery() {
    return new ProcessInstanceQueryImpl(commandExecutor);
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.RuntimeService#getActiveActivityIds(java.lang.String)
   */
  public List<String> getActiveActivityIds(String executionId) {
    return commandExecutor.execute(new FindActiveActivityIdsCmd(executionId));
  }

  /**
   * Gets the form instance by id.
   *
   * @param processDefinitionId the process definition id
   * @return the form instance by id
   */
  public FormData getFormInstanceById(String processDefinitionId) {
    return commandExecutor.execute(new GetStartFormCmd(processDefinitionId));
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.RuntimeService#suspendProcessInstanceById(java.lang.String)
   */
  public void suspendProcessInstanceById(String processInstanceId) {
    commandExecutor.execute(new SuspendProcessInstanceCmd(processInstanceId));
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.RuntimeService#activateProcessInstanceById(java.lang.String)
   */
  public void activateProcessInstanceById(String processInstanceId) {
    commandExecutor.execute(new ActivateProcessInstanceCmd(processInstanceId));
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.RuntimeService#startProcessInstanceByMessage(java.lang.String)
   */
  public ProcessInstance startProcessInstanceByMessage(String messageName) {
    return commandExecutor.execute(new StartProcessInstanceByMessageCmd(messageName,null, null));
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.RuntimeService#startProcessInstanceByMessage(java.lang.String, java.lang.String)
   */
  public ProcessInstance startProcessInstanceByMessage(String messageName, String businessKey) {
    return commandExecutor.execute(new StartProcessInstanceByMessageCmd(messageName, businessKey, null));
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.RuntimeService#startProcessInstanceByMessage(java.lang.String, java.util.Map)
   */
  public ProcessInstance startProcessInstanceByMessage(String messageName, Map<String, Object> processVariables) {
    return commandExecutor.execute(new StartProcessInstanceByMessageCmd(messageName, null, processVariables));
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.RuntimeService#startProcessInstanceByMessage(java.lang.String, java.lang.String, java.util.Map)
   */
  public ProcessInstance startProcessInstanceByMessage(String messageName, String businessKey, Map<String, Object> processVariables) {
    return commandExecutor.execute(new StartProcessInstanceByMessageCmd(messageName, businessKey, processVariables));
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.RuntimeService#signalEventReceived(java.lang.String)
   */
  public void signalEventReceived(String signalName) {
    commandExecutor.execute(new SignalEventReceivedCmd(signalName, null, null));
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.RuntimeService#signalEventReceived(java.lang.String, java.util.Map)
   */
  public void signalEventReceived(String signalName, Map<String, Object> processVariables) {
    commandExecutor.execute(new SignalEventReceivedCmd(signalName, null, processVariables));
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.RuntimeService#signalEventReceived(java.lang.String, java.lang.String)
   */
  public void signalEventReceived(String signalName, String executionId) {
    commandExecutor.execute(new SignalEventReceivedCmd(signalName, executionId, null));
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.RuntimeService#signalEventReceived(java.lang.String, java.lang.String, java.util.Map)
   */
  public void signalEventReceived(String signalName, String executionId, Map<String, Object> processVariables) {
    commandExecutor.execute(new SignalEventReceivedCmd(signalName, executionId, processVariables));
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.RuntimeService#messageEventReceived(java.lang.String, java.lang.String)
   */
  public void messageEventReceived(String messageName, String executionId) {
    commandExecutor.execute(new MessageEventReceivedCmd(messageName, executionId, null));
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.RuntimeService#messageEventReceived(java.lang.String, java.lang.String, java.util.Map)
   */
  public void messageEventReceived(String messageName, String executionId, Map<String, Object> processVariables) {
    commandExecutor.execute(new MessageEventReceivedCmd(messageName, executionId, processVariables));
  }
   
}
