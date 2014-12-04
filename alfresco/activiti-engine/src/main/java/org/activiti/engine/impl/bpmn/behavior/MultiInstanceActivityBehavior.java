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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.BpmnError;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.bpmn.helper.ErrorPropagation;
import org.activiti.engine.impl.bpmn.helper.ScopeUtil;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.delegate.ExecutionListenerInvocation;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.pvm.delegate.CompositeActivityBehavior;
import org.activiti.engine.impl.pvm.delegate.SubProcessActivityBehavior;
import org.activiti.engine.impl.pvm.process.ActivityImpl;


// TODO: Auto-generated Javadoc
/**
 * Implementation of the multi-instance functionality as described in the BPMN 2.0 spec.
 * 
 * Multi instance functionality is implemented as an {@link ActivityBehavior} that
 * wraps the original {@link ActivityBehavior} of the activity.
 *
 * Only subclasses of {@link AbstractBpmnActivityBehavior} can have multi-instance
 * behavior. As such, special logic is contained in the {@link AbstractBpmnActivityBehavior}
 * to delegate to the {@link MultiInstanceActivityBehavior} if needed.
 * 
 * @author Joram Barrez
 * @author Falko Menge
 */
public abstract class MultiInstanceActivityBehavior extends FlowNodeActivityBehavior  
  implements CompositeActivityBehavior, SubProcessActivityBehavior {
  
  /** The Constant LOGGER. */
  protected static final Logger LOGGER = Logger.getLogger(MultiInstanceActivityBehavior.class.getName());
  
  // Variable names for outer instance(as described in spec)
  /** The number of instances. */
  protected final String NUMBER_OF_INSTANCES = "nrOfInstances";
  
  /** The number of active instances. */
  protected final String NUMBER_OF_ACTIVE_INSTANCES = "nrOfActiveInstances";
  
  /** The number of completed instances. */
  protected final String NUMBER_OF_COMPLETED_INSTANCES = "nrOfCompletedInstances";
  
  // Variable names for inner instances (as described in the spec)
  /** The loop counter. */
  protected final String LOOP_COUNTER = "loopCounter";
  
  // Instance members
  /** The activity. */
  protected ActivityImpl activity;
  
  /** The inner activity behavior. */
  protected AbstractBpmnActivityBehavior innerActivityBehavior;
  
  /** The loop cardinality expression. */
  protected Expression loopCardinalityExpression;
  
  /** The completion condition expression. */
  protected Expression completionConditionExpression;
  
  /** The collection expression. */
  protected Expression collectionExpression;
  
  /** The collection variable. */
  protected String collectionVariable;
  
  /** The collection element variable. */
  protected String collectionElementVariable;
  
  /**
   * Instantiates a new multi instance activity behavior.
   *
   * @param activity the activity
   * @param innerActivityBehavior The original {@link ActivityBehavior} of the activity
   * that will be wrapped inside this behavior.
   */
  public MultiInstanceActivityBehavior(ActivityImpl activity, AbstractBpmnActivityBehavior innerActivityBehavior) {
    this.activity = activity;
    setInnerActivityBehavior(innerActivityBehavior);
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.bpmn.behavior.FlowNodeActivityBehavior#execute(org.activiti.engine.impl.pvm.delegate.ActivityExecution)
   */
  public void execute(ActivityExecution execution) throws Exception {
    if (getLoopVariable(execution, LOOP_COUNTER) == null) {
      try {
        createInstances(execution);
      } catch (BpmnError error) {
        ErrorPropagation.propagateError(error, execution);
      }
    } else {
        innerActivityBehavior.execute(execution);
    }
  }
  
  /**
   * Creates the instances.
   *
   * @param execution the execution
   * @throws Exception the exception
   */
  protected abstract void createInstances(ActivityExecution execution) throws Exception;
  
  // Intercepts signals, and delegates it to the wrapped {@link ActivityBehavior}.
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.bpmn.behavior.FlowNodeActivityBehavior#signal(org.activiti.engine.impl.pvm.delegate.ActivityExecution, java.lang.String, java.lang.Object)
   */
  public void signal(ActivityExecution execution, String signalName, Object signalData) throws Exception {
    innerActivityBehavior.signal(execution, signalName, signalData);
  }
  
  // required for supporting embedded subprocesses
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.delegate.CompositeActivityBehavior#lastExecutionEnded(org.activiti.engine.impl.pvm.delegate.ActivityExecution)
   */
  public void lastExecutionEnded(ActivityExecution execution) {
    ScopeUtil.createEventScopeExecution((ExecutionEntity) execution);
    leave(execution);
  }
  
  // required for supporting external subprocesses
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.delegate.SubProcessActivityBehavior#completing(org.activiti.engine.delegate.DelegateExecution, org.activiti.engine.delegate.DelegateExecution)
   */
  public void completing(DelegateExecution execution, DelegateExecution subProcessInstance) throws Exception {
  }

  // required for supporting external subprocesses
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.delegate.SubProcessActivityBehavior#completed(org.activiti.engine.impl.pvm.delegate.ActivityExecution)
   */
  public void completed(ActivityExecution execution) throws Exception {
    leave(execution);
  }
  
  // Helpers //////////////////////////////////////////////////////////////////////
  
  /**
   * Resolve nr of instances.
   *
   * @param execution the execution
   * @return the int
   */
  @SuppressWarnings("rawtypes")
  protected int resolveNrOfInstances(ActivityExecution execution) {
    int nrOfInstances = -1;
    if (loopCardinalityExpression != null) {
      nrOfInstances = resolveLoopCardinality(execution);
    } else if (collectionExpression != null) {
      Object obj = collectionExpression.getValue(execution);
      if (!(obj instanceof Collection)) {
        throw new ActivitiException(collectionExpression.getExpressionText()+"' didn't resolve to a Collection");
      }
      nrOfInstances = ((Collection) obj).size();
    } else if (collectionVariable != null) {
      Object obj = execution.getVariable(collectionVariable);
      if (!(obj instanceof Collection)) {
        throw new ActivitiException("Variable " + collectionVariable+"' is not a Collection");
      }
      nrOfInstances = ((Collection) obj).size();
    } else {
      throw new ActivitiException("Couldn't resolve collection expression nor variable reference");
    }
    return nrOfInstances;
  }
  
  /**
   * Execute original behavior.
   *
   * @param execution the execution
   * @param loopCounter the loop counter
   * @throws Exception the exception
   */
  @SuppressWarnings("rawtypes")
  protected void executeOriginalBehavior(ActivityExecution execution, int loopCounter) throws Exception {
    if (usesCollection() && collectionElementVariable != null) {
      Collection collection = null;
      if (collectionExpression != null) {
        collection = (Collection) collectionExpression.getValue(execution);
      } else if (collectionVariable != null) {
        collection = (Collection) execution.getVariable(collectionVariable);
      }
       
      Object value = null;
      int index = 0;
      Iterator it = collection.iterator();
      while (index <= loopCounter) {
        value = it.next();
        index++;
      }
      setLoopVariable(execution, collectionElementVariable, value);
    }

    // If loopcounter == 1, then historic activity instance already created, no need to
    // pass through executeActivity again since it will create a new historic activity
    if (loopCounter == 0) {
      innerActivityBehavior.execute(execution);
    } else {
      execution.executeActivity(activity);
    }
  }
  
  /**
   * Uses collection.
   *
   * @return true, if successful
   */
  protected boolean usesCollection() {
    return collectionExpression != null 
              || collectionVariable != null;
  }
  
  /**
   * Checks if is extra scope needed.
   *
   * @return true, if is extra scope needed
   */
  protected boolean isExtraScopeNeeded() {
    // special care is needed when the behavior is an embedded subprocess (not very clean, but it works)
    return innerActivityBehavior instanceof org.activiti.engine.impl.bpmn.behavior.SubProcessActivityBehavior;  
  }
  
  /**
   * Resolve loop cardinality.
   *
   * @param execution the execution
   * @return the int
   */
  protected int resolveLoopCardinality(ActivityExecution execution) {
    // Using Number since expr can evaluate to eg. Long (which is also the default for Juel)
    Object value = loopCardinalityExpression.getValue(execution);
    if (value instanceof Number) {
      return ((Number) value).intValue();
    } else if (value instanceof String) {
      return Integer.valueOf((String) value);
    } else {
      throw new ActivitiException("Could not resolve loopCardinality expression '" 
              +loopCardinalityExpression.getExpressionText()+"': not a number nor number String");
    }
  }
  
  /**
   * Completion condition satisfied.
   *
   * @param execution the execution
   * @return true, if successful
   */
  protected boolean completionConditionSatisfied(ActivityExecution execution) {
    if (completionConditionExpression != null) {
      Object value = completionConditionExpression.getValue(execution);
      if (! (value instanceof Boolean)) {
        throw new ActivitiException("completionCondition '"
                + completionConditionExpression.getExpressionText()
                + "' does not evaluate to a boolean value");
      }
      Boolean booleanValue = (Boolean) value;
      if (LOGGER.isLoggable(Level.FINE)) {
        LOGGER.fine("Completion condition of multi-instance satisfied: " + booleanValue);
      }
      return booleanValue;
    }
    return false;
  }
  
  /**
   * Sets the loop variable.
   *
   * @param execution the execution
   * @param variableName the variable name
   * @param value the value
   */
  protected void setLoopVariable(ActivityExecution execution, String variableName, Object value) {
    execution.setVariableLocal(variableName, value);
  }
  
  /**
   * Gets the loop variable.
   *
   * @param execution the execution
   * @param variableName the variable name
   * @return the loop variable
   */
  protected Integer getLoopVariable(ActivityExecution execution, String variableName) {
    Object value = execution.getVariableLocal(variableName);
    ActivityExecution parent = execution.getParent();
    while (value == null && parent != null) {
      value = parent.getVariableLocal(variableName);
      parent = parent.getParent();
    }
    return (Integer) value;
  }
  
  /**
   * Since no transitions are followed when leaving the inner activity,
   * it is needed to call the end listeners yourself.
   *
   * @param execution the execution
   */
  protected void callActivityEndListeners(ActivityExecution execution) {
    List<ExecutionListener> listeners = activity.getExecutionListeners(org.activiti.engine.impl.pvm.PvmEvent.EVENTNAME_END);
    for (ExecutionListener executionListener : listeners) {
      try {
        Context.getProcessEngineConfiguration()
          .getDelegateInterceptor()
          .handleInvocation(new ExecutionListenerInvocation(executionListener, execution));
      } catch (Exception e) {
        throw new ActivitiException("Couldn't execute end listener", e);
      }
    }
  }
  
  /**
   * Log loop details.
   *
   * @param execution the execution
   * @param custom the custom
   * @param loopCounter the loop counter
   * @param nrOfCompletedInstances the nr of completed instances
   * @param nrOfActiveInstances the nr of active instances
   * @param nrOfInstances the nr of instances
   */
  protected void logLoopDetails(ActivityExecution execution, String custom, int loopCounter, 
          int nrOfCompletedInstances, int nrOfActiveInstances, int nrOfInstances) {
    if (LOGGER.isLoggable(Level.FINE)) {
      StringBuilder strb = new StringBuilder();
      strb.append("Multi-instance '" + execution.getActivity() + "' " + custom + ". ");
      strb.append("Details: loopCounter=" + loopCounter + ", ");
      strb.append("nrOrCompletedInstances=" + nrOfCompletedInstances + ", ");
      strb.append("nrOfActiveInstances=" + nrOfActiveInstances+ ", ");
      strb.append("nrOfInstances=" + nrOfInstances);
      LOGGER.fine(strb.toString());
    }
  }

  
  // Getters and Setters ///////////////////////////////////////////////////////////
  
  /**
   * Gets the loop cardinality expression.
   *
   * @return the loop cardinality expression
   */
  public Expression getLoopCardinalityExpression() {
    return loopCardinalityExpression;
  }
  
  /**
   * Sets the loop cardinality expression.
   *
   * @param loopCardinalityExpression the new loop cardinality expression
   */
  public void setLoopCardinalityExpression(Expression loopCardinalityExpression) {
    this.loopCardinalityExpression = loopCardinalityExpression;
  }
  
  /**
   * Gets the completion condition expression.
   *
   * @return the completion condition expression
   */
  public Expression getCompletionConditionExpression() {
    return completionConditionExpression;
  }
  
  /**
   * Sets the completion condition expression.
   *
   * @param completionConditionExpression the new completion condition expression
   */
  public void setCompletionConditionExpression(Expression completionConditionExpression) {
    this.completionConditionExpression = completionConditionExpression;
  }
  
  /**
   * Gets the collection expression.
   *
   * @return the collection expression
   */
  public Expression getCollectionExpression() {
    return collectionExpression;
  }
  
  /**
   * Sets the collection expression.
   *
   * @param collectionExpression the new collection expression
   */
  public void setCollectionExpression(Expression collectionExpression) {
    this.collectionExpression = collectionExpression;
  }
  
  /**
   * Gets the collection variable.
   *
   * @return the collection variable
   */
  public String getCollectionVariable() {
    return collectionVariable;
  }
  
  /**
   * Sets the collection variable.
   *
   * @param collectionVariable the new collection variable
   */
  public void setCollectionVariable(String collectionVariable) {
    this.collectionVariable = collectionVariable;
  }
  
  /**
   * Gets the collection element variable.
   *
   * @return the collection element variable
   */
  public String getCollectionElementVariable() {
    return collectionElementVariable;
  }
  
  /**
   * Sets the collection element variable.
   *
   * @param collectionElementVariable the new collection element variable
   */
  public void setCollectionElementVariable(String collectionElementVariable) {
    this.collectionElementVariable = collectionElementVariable;
  }
  
  /**
   * Sets the inner activity behavior.
   *
   * @param innerActivityBehavior the new inner activity behavior
   */
  public void setInnerActivityBehavior(AbstractBpmnActivityBehavior innerActivityBehavior) {
    this.innerActivityBehavior = innerActivityBehavior;
    this.innerActivityBehavior.setMultiInstanceActivityBehavior(this);
  }
  
}
