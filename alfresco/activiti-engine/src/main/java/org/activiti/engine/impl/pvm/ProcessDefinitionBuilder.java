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

package org.activiti.engine.impl.pvm;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.activiti.engine.impl.pvm.process.ProcessElementImpl;
import org.activiti.engine.impl.pvm.process.ScopeImpl;
import org.activiti.engine.impl.pvm.process.TransitionImpl;



// TODO: Auto-generated Javadoc
/**
 * The Class ProcessDefinitionBuilder.
 *
 * @author Tom Baeyens
 */
@SuppressWarnings("deprecation")
public class ProcessDefinitionBuilder {

  /** The process definition. */
  protected ProcessDefinitionImpl processDefinition;
  
  /** The scope stack. */
  protected Stack<ScopeImpl> scopeStack = new Stack<ScopeImpl>();
  
  /** The process element. */
  protected ProcessElementImpl processElement = processDefinition;
  
  /** The transition. */
  protected TransitionImpl transition;
  
  /** The unresolved transitions. */
  protected List<Object[]> unresolvedTransitions = new ArrayList<Object[]>();
  
  /**
   * Instantiates a new process definition builder.
   */
  public ProcessDefinitionBuilder() {
    this(null);
  }
  
  /**
   * Instantiates a new process definition builder.
   *
   * @param processDefinitionId the process definition id
   */
  public ProcessDefinitionBuilder(String processDefinitionId) {
    processDefinition = new ProcessDefinitionImpl(processDefinitionId);
    scopeStack.push(processDefinition);
  }

  /**
   * Creates the activity.
   *
   * @param id the id
   * @return the process definition builder
   */
  public ProcessDefinitionBuilder createActivity(String id) {
    ActivityImpl activity = scopeStack.peek().createActivity(id);
    scopeStack.push(activity);
    processElement = activity;
    
    transition = null;
    
    return this;
  }
  
  /**
   * End activity.
   *
   * @return the process definition builder
   */
  public ProcessDefinitionBuilder endActivity() {
    scopeStack.pop();
    processElement = scopeStack.peek();

    transition = null;
    
    return this;
  }
  
  /**
   * Initial.
   *
   * @return the process definition builder
   */
  public ProcessDefinitionBuilder initial() {
    processDefinition.setInitial(getActivity());
    return this;
  }

  /**
   * Start transition.
   *
   * @param destinationActivityId the destination activity id
   * @return the process definition builder
   */
  public ProcessDefinitionBuilder startTransition(String destinationActivityId) {
    return startTransition(destinationActivityId, null);
  }
  
  /**
   * Start transition.
   *
   * @param destinationActivityId the destination activity id
   * @param transitionId the transition id
   * @return the process definition builder
   */
  public ProcessDefinitionBuilder startTransition(String destinationActivityId, String transitionId) {
    if (destinationActivityId==null) {
      throw new PvmException("destinationActivityId is null");
    }
    ActivityImpl activity = getActivity();
    transition = activity.createOutgoingTransition(transitionId);
    unresolvedTransitions.add(new Object[]{transition, destinationActivityId});
    processElement = transition;
    return this;
  }
  
  /**
   * End transition.
   *
   * @return the process definition builder
   */
  public ProcessDefinitionBuilder endTransition() {
    processElement = scopeStack.peek();
    transition = null;
    return this;
  }

  /**
   * Transition.
   *
   * @param destinationActivityId the destination activity id
   * @return the process definition builder
   */
  public ProcessDefinitionBuilder transition(String destinationActivityId) {
    return transition(destinationActivityId, null);
  }
  
  /**
   * Transition.
   *
   * @param destinationActivityId the destination activity id
   * @param transitionId the transition id
   * @return the process definition builder
   */
  public ProcessDefinitionBuilder transition(String destinationActivityId, String transitionId) {
    startTransition(destinationActivityId, transitionId);
    endTransition();
    return this;
  }

  /**
   * Behavior.
   *
   * @param activityBehaviour the activity behaviour
   * @return the process definition builder
   */
  public ProcessDefinitionBuilder behavior(ActivityBehavior activityBehaviour) {
    getActivity().setActivityBehavior(activityBehaviour);
    return this;
  }
  
  /**
   * Property.
   *
   * @param name the name
   * @param value the value
   * @return the process definition builder
   */
  public ProcessDefinitionBuilder property(String name, Object value) {
    processElement.setProperty(name, value);
    return this;
  }

  /**
   * Builds the process definition.
   *
   * @return the pvm process definition
   */
  public PvmProcessDefinition buildProcessDefinition() {
    for (Object[] unresolvedTransition: unresolvedTransitions) {
      TransitionImpl transition = (TransitionImpl) unresolvedTransition[0];
      String destinationActivityName = (String) unresolvedTransition[1];
      ActivityImpl destination = processDefinition.findActivity(destinationActivityName);
      if (destination == null) {
        throw new RuntimeException("destination '"+destinationActivityName+"' not found.  (referenced from transition in '"+transition.getSource().getId()+"')");
      }
      transition.setDestination(destination);
    }
    return processDefinition;
  }
  
  /**
   * Gets the activity.
   *
   * @return the activity
   */
  protected ActivityImpl getActivity() {
    return (ActivityImpl) scopeStack.peek(); 
  }

  /**
   * Scope.
   *
   * @return the process definition builder
   */
  public ProcessDefinitionBuilder scope() {
    getActivity().setScope(true);
    return this;
  }

  /**
   * Execution listener.
   *
   * @param executionListener the execution listener
   * @return the process definition builder
   */
  public ProcessDefinitionBuilder executionListener(ExecutionListener executionListener) {
    if (transition!=null) {
      transition.addExecutionListener(executionListener);
    } else {
      throw new PvmException("not in a transition scope");
    }
    return this;
  }
  
  /**
   * Execution listener.
   *
   * @param eventName the event name
   * @param executionListener the execution listener
   * @return the process definition builder
   */
  public ProcessDefinitionBuilder executionListener(String eventName, ExecutionListener executionListener) {
    if (transition==null) {
      scopeStack.peek().addExecutionListener(eventName, executionListener);
    } else {
      throw new PvmException("not in an activity- or process definition scope. (but in a transition scope)");
    }
    return this;
  }
}
