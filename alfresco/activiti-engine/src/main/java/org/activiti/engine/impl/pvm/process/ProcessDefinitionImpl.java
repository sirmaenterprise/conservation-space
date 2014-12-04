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

package org.activiti.engine.impl.pvm.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.pvm.PvmProcessDefinition;
import org.activiti.engine.impl.pvm.PvmProcessInstance;
import org.activiti.engine.impl.pvm.runtime.ExecutionImpl;
import org.activiti.engine.impl.pvm.runtime.InterpretableExecution;



// TODO: Auto-generated Javadoc
/**
 * The Class ProcessDefinitionImpl.
 *
 * @author Tom Baeyens
 */
public class ProcessDefinitionImpl extends ScopeImpl implements PvmProcessDefinition {
  
  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;
  
  /** The name. */
  protected String name;
  
  /** The description. */
  protected String description;
  
  /** The initial. */
  protected ActivityImpl initial;
  
  /** The initial activity stacks. */
  protected Map<ActivityImpl, List<ActivityImpl>> initialActivityStacks = new HashMap<ActivityImpl, List<ActivityImpl>>();
  
  /** The lane sets. */
  protected List<LaneSet> laneSets;
  
  /** The participant process. */
  protected ParticipantProcess participantProcess;

  /**
   * Instantiates a new process definition impl.
   *
   * @param id the id
   */
  public ProcessDefinitionImpl(String id) {
    super(id, null);
    processDefinition = this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.PvmProcessDefinition#createProcessInstance()
   */
  public PvmProcessInstance createProcessInstance() {
    if(initial == null) {
      throw new ActivitiException("Process '"+name+"' has no default start activity (e.g. none start event), hence you cannot use 'startProcessInstanceBy...' but have to start it using one of the modeled start events (e.g. message start events).");
    }
    return createProcessInstanceForInitial(initial);
  }
  
  /**
   * creates a process instance using the provided activity as initial.
   *
   * @param initial the initial
   * @return the pvm process instance
   */
  public PvmProcessInstance createProcessInstanceForInitial(ActivityImpl initial) {
    
    if(initial == null) {
      throw new ActivitiException("Cannot start process instance, initial activity where the process instance should start is null.");
    }
    
    InterpretableExecution processInstance = newProcessInstance(initial);
    processInstance.setProcessDefinition(this);
    processInstance.setProcessInstance(processInstance);
    processInstance.initialize();

    InterpretableExecution scopeInstance = processInstance;
    
    List<ActivityImpl> initialActivityStack = getInitialActivityStack(initial);
    
    for (ActivityImpl initialActivity: initialActivityStack) {
      if (initialActivity.isScope()) {
        scopeInstance = (InterpretableExecution) scopeInstance.createExecution();
        scopeInstance.setActivity(initialActivity);
        if (initialActivity.isScope()) {
          scopeInstance.initialize();
        }
      }
    }
    
    scopeInstance.setActivity(initial);

    return processInstance;
  }

  /**
   * Gets the initial activity stack.
   *
   * @return the initial activity stack
   */
  public List<ActivityImpl> getInitialActivityStack() {
    return getInitialActivityStack(initial);    
  }
  
  /**
   * Gets the initial activity stack.
   *
   * @param startActivity the start activity
   * @return the initial activity stack
   */
  public synchronized List<ActivityImpl> getInitialActivityStack(ActivityImpl startActivity) {
    List<ActivityImpl> initialActivityStack = initialActivityStacks.get(startActivity);
    if(initialActivityStack == null) {
      initialActivityStack = new ArrayList<ActivityImpl>();
      ActivityImpl activity = startActivity;
      while (activity!=null) {
        initialActivityStack.add(0, activity);
        activity = activity.getParentActivity();
      }
      initialActivityStacks.put(startActivity, initialActivityStack);
    }
    return initialActivityStack;
  }

  /**
   * New process instance.
   *
   * @param startActivity the start activity
   * @return the interpretable execution
   */
  protected InterpretableExecution newProcessInstance(ActivityImpl startActivity) {
    return new ExecutionImpl(startActivity);
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.ReadOnlyProcessDefinition#getDiagramResourceName()
   */
  public String getDiagramResourceName() {
    return null;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.PvmProcessDefinition#getDeploymentId()
   */
  public String getDeploymentId() {
    return null;
  }
  
  /**
   * Adds the lane set.
   *
   * @param newLaneSet the new lane set
   */
  public void addLaneSet(LaneSet newLaneSet) {
    getLaneSets().add(newLaneSet);
  }
  
  /**
   * Gets the lane for id.
   *
   * @param id the id
   * @return the lane for id
   */
  public Lane getLaneForId(String id) {
    if(laneSets != null && laneSets.size() > 0) {
      Lane lane;
      for(LaneSet set : laneSets) {
        lane = set.getLaneForId(id);
        if(lane != null) {
          return lane;
        }
      }
    }
    return null;
  }
  
  // getters and setters //////////////////////////////////////////////////////
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.ReadOnlyProcessDefinition#getInitial()
   */
  public ActivityImpl getInitial() {
    return initial;
  }

  /**
   * Sets the initial.
   *
   * @param initial the new initial
   */
  public void setInitial(ActivityImpl initial) {
    this.initial = initial;
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return "ProcessDefinition("+id+")";
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.ReadOnlyProcessDefinition#getName()
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the name.
   *
   * @param name the new name
   */
  public void setName(String name) {
    this.name = name;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.ReadOnlyProcessDefinition#getDescription()
   */
  public String getDescription() {
    return (String) getProperty("documentation");
  }
  
  /**
   * Gets the lane sets.
   *
   * @return all lane-sets defined on this process-instance. Returns an empty list if none are defined.
   */
  public List<LaneSet> getLaneSets() {
    if(laneSets == null) {
      laneSets = new ArrayList<LaneSet>();
    }
    return laneSets;
  }
  
  
  /**
   * Sets the participant process.
   *
   * @param participantProcess the new participant process
   */
  public void setParticipantProcess(ParticipantProcess participantProcess) {
    this.participantProcess = participantProcess;
  }
  
  /**
   * Gets the participant process.
   *
   * @return the participant process
   */
  public ParticipantProcess getParticipantProcess() {
    return participantProcess;
  }
}
