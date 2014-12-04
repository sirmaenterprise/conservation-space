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

package org.activiti.engine.impl.bpmn.behavior;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.bpmn.data.AbstractDataAssociation;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.pvm.PvmProcessInstance;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.pvm.delegate.SubProcessActivityBehavior;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;


// TODO: Auto-generated Javadoc
/**
 * Implementation of the BPMN 2.0 call activity
 * (limited currently to calling a subprocess and not (yet) a global task).
 * 
 * @author Joram Barrez
 */
public class CallActivityBehavior extends AbstractBpmnActivityBehavior implements SubProcessActivityBehavior {
  
  /** The process definiton key. */
  protected String processDefinitonKey;
  
  /** The data input associations. */
  private List<AbstractDataAssociation> dataInputAssociations = new ArrayList<AbstractDataAssociation>();
  
  /** The data output associations. */
  private List<AbstractDataAssociation> dataOutputAssociations = new ArrayList<AbstractDataAssociation>();
  
  /** The process definition expression. */
  private Expression processDefinitionExpression;

  /**
   * Instantiates a new call activity behavior.
   *
   * @param processDefinitionKey the process definition key
   */
  public CallActivityBehavior(String processDefinitionKey) {
    this.processDefinitonKey = processDefinitionKey;
  }
  
  /**
   * Instantiates a new call activity behavior.
   *
   * @param processDefinitionExpression the process definition expression
   */
  public CallActivityBehavior(Expression processDefinitionExpression) {
    super();
    this.processDefinitionExpression = processDefinitionExpression;
  }

  /**
   * Adds the data input association.
   *
   * @param dataInputAssociation the data input association
   */
  public void addDataInputAssociation(AbstractDataAssociation dataInputAssociation) {
    this.dataInputAssociations.add(dataInputAssociation);
  }

  /**
   * Adds the data output association.
   *
   * @param dataOutputAssociation the data output association
   */
  public void addDataOutputAssociation(AbstractDataAssociation dataOutputAssociation) {
    this.dataOutputAssociations.add(dataOutputAssociation);
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.bpmn.behavior.FlowNodeActivityBehavior#execute(org.activiti.engine.impl.pvm.delegate.ActivityExecution)
   */
  public void execute(ActivityExecution execution) throws Exception {
    
    if (processDefinitionExpression != null) {
      processDefinitonKey = (String) processDefinitionExpression.getValue(execution);
    }
    
    ProcessDefinitionImpl processDefinition = Context
      .getProcessEngineConfiguration()
      .getDeploymentCache()
      .findDeployedLatestProcessDefinitionByKey(processDefinitonKey);
    
    PvmProcessInstance subProcessInstance = execution.createSubProcessInstance(processDefinition);
    
    // copy process variables
    for (AbstractDataAssociation dataInputAssociation : dataInputAssociations) {
      Object value = null;
      if (dataInputAssociation.getSourceExpression()!=null) {
        value = dataInputAssociation.getSourceExpression().getValue(execution);
      }
      else {
        value = execution.getVariable(dataInputAssociation.getSource());
      }
      subProcessInstance.setVariable(dataInputAssociation.getTarget(), value);
    }
    
    subProcessInstance.start();
  }
  
  /**
   * Sets the process definiton key.
   *
   * @param processDefinitonKey the new process definiton key
   */
  public void setProcessDefinitonKey(String processDefinitonKey) {
    this.processDefinitonKey = processDefinitonKey;
  }
  
  /**
   * Gets the process definiton key.
   *
   * @return the process definiton key
   */
  public String getProcessDefinitonKey() {
    return processDefinitonKey;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.delegate.SubProcessActivityBehavior#completing(org.activiti.engine.delegate.DelegateExecution, org.activiti.engine.delegate.DelegateExecution)
   */
  public void completing(DelegateExecution execution, DelegateExecution subProcessInstance) throws Exception {
    // only data.  no control flow available on this execution.

    // copy process variables
    for (AbstractDataAssociation dataOutputAssociation : dataOutputAssociations) {
      Object value = null;
      if (dataOutputAssociation.getSourceExpression()!=null) {
        value = dataOutputAssociation.getSourceExpression().getValue(subProcessInstance);
      }
      else {
        value = subProcessInstance.getVariable(dataOutputAssociation.getSource());
      }
      
      execution.setVariable(dataOutputAssociation.getTarget(), value);
    }
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.delegate.SubProcessActivityBehavior#completed(org.activiti.engine.impl.pvm.delegate.ActivityExecution)
   */
  public void completed(ActivityExecution execution) throws Exception {
    // only control flow.  no sub process instance data available
    leave(execution);
  }

}
