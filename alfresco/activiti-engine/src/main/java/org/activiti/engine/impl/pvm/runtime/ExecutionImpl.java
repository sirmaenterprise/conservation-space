/* Licensed under the Apache License, ersion 2.0 (the "License");
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
package org.activiti.engine.impl.pvm.runtime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.PvmException;
import org.activiti.engine.impl.pvm.PvmExecution;
import org.activiti.engine.impl.pvm.PvmProcessDefinition;
import org.activiti.engine.impl.pvm.PvmProcessElement;
import org.activiti.engine.impl.pvm.PvmProcessInstance;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.pvm.delegate.ExecutionListenerExecution;
import org.activiti.engine.impl.pvm.delegate.SignallableActivityBehavior;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.activiti.engine.impl.pvm.process.TransitionImpl;

// TODO: Auto-generated Javadoc
/**
 * The Class ExecutionImpl.
 *
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class ExecutionImpl implements
        Serializable,
        ActivityExecution, 
        ExecutionListenerExecution, 
        PvmExecution,
        InterpretableExecution {
  
  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;
  
  /** The log. */
  private static Logger log = Logger.getLogger(ExecutionImpl.class.getName());
  
  // current position /////////////////////////////////////////////////////////
  
  /** The process definition. */
  protected ProcessDefinitionImpl processDefinition;

  /** current activity. */
  protected ActivityImpl activity;
  
  /** current transition.  is null when there is no transition being taken. */
  protected TransitionImpl transition = null;

  /** the process instance.  this is the root of the execution tree.  
   * the processInstance of a process instance is a self reference. */
  protected ExecutionImpl processInstance;
  
  /** the parent execution. */
  protected ExecutionImpl parent;
  
  /** nested executions representing scopes or concurrent paths. */
  protected List<ExecutionImpl> executions;
  
  /** super execution, not-null if this execution is part of a subprocess. */
  protected ExecutionImpl superExecution;
  
  /** reference to a subprocessinstance, not-null if currently subprocess is started from this execution. */
  protected ExecutionImpl subProcessInstance;
  
  /** only available until the process instance is started. */
  protected StartingExecution startingExecution;
  
  // state/type of execution ////////////////////////////////////////////////// 
  
  /** indicates if this execution represents an active path of execution.
   * Executions are made inactive in the following situations:
   * <ul>
   *   <li>an execution enters a nested scope</li>
   *   <li>an execution is split up into multiple concurrent executions, then the parent is made inactive.</li>
   *   <li>an execution has arrived in a parallel gateway or join and that join has not yet activated/fired.</li>
   *   <li>an execution is ended.</li>
   * </ul>*/ 
  protected boolean isActive = true;
  
  /** The is scope. */
  protected boolean isScope = true;
  
  /** The is concurrent. */
  protected boolean isConcurrent = false;
  
  /** The is ended. */
  protected boolean isEnded = false;
  
  /** The is event scope. */
  protected boolean isEventScope = false;
  
  /** The variables. */
  protected Map<String, Object> variables = null;
  
  // events ///////////////////////////////////////////////////////////////////
  
  /** The event name. */
  protected String eventName;
  
  /** The event source. */
  protected PvmProcessElement eventSource;
  
  /** The execution listener index. */
  protected int executionListenerIndex = 0;
    
  // cascade deletion ////////////////////////////////////////////////////////
  
  /** The delete root. */
  protected boolean deleteRoot;
  
  /** The delete reason. */
  protected String deleteReason;
  
  // replaced by //////////////////////////////////////////////////////////////
  
  /** when execution structure is pruned during a takeAll, then 
   * the original execution has to be resolved to the replaced execution.
   * @see {@link #takeAll(List, List)} {@link OutgoingExecution} */
  protected ExecutionImpl replacedBy;
  
  // atomic operations ////////////////////////////////////////////////////////

  /** next operation.  process execution is in fact runtime interpretation of the process model.
   * each operation is a logical unit of interpretation of the process.  so sequentially processing 
   * the operations drives the interpretation or execution of a process. 
   * @see AtomicOperation
   * @see #performOperation(AtomicOperation) */
  protected AtomicOperation nextOperation;
  
  /** The is operating. */
  protected boolean isOperating = false;

  /* Default constructor for ibatis/jpa/etc. */
  /**
   * Instantiates a new execution impl.
   */
  public ExecutionImpl() {    
  }
  
  /**
   * Instantiates a new execution impl.
   *
   * @param initial the initial
   */
  public ExecutionImpl(ActivityImpl initial) {
    startingExecution = new StartingExecution(initial);
  }
  
  // lifecycle methods ////////////////////////////////////////////////////////
  
  /**
   * creates a new execution. properties processDefinition, processInstance and activity will be initialized.
   *
   * @return the execution impl
   */  
  public ExecutionImpl createExecution() {
    // create the new child execution
    ExecutionImpl createdExecution = newExecution();

    // manage the bidirectional parent-child relation
    ensureExecutionsInitialized();
    executions.add(createdExecution); 
    createdExecution.setParent(this);
    
    // initialize the new execution
    createdExecution.setProcessDefinition(getProcessDefinition());
    createdExecution.setProcessInstance(getProcessInstance());
    createdExecution.setActivity(getActivity());
    
    return createdExecution;
  }
  
  /**
   * instantiates a new execution.  can be overridden by subclasses
   *
   * @return the execution impl
   */
  protected ExecutionImpl newExecution() {
    return new ExecutionImpl();
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.delegate.ActivityExecution#createSubProcessInstance(org.activiti.engine.impl.pvm.PvmProcessDefinition)
   */
  public PvmProcessInstance createSubProcessInstance(PvmProcessDefinition processDefinition) {
    ExecutionImpl subProcessInstance = newExecution();
    
    // manage bidirectional super-subprocess relation
    subProcessInstance.setSuperExecution(this);
    this.setSubProcessInstance(subProcessInstance);
    
    // Initialize the new execution
    subProcessInstance.setProcessDefinition((ProcessDefinitionImpl) processDefinition);
    subProcessInstance.setProcessInstance(subProcessInstance);

    return subProcessInstance;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.runtime.InterpretableExecution#initialize()
   */
  public void initialize() {
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.runtime.InterpretableExecution#destroy()
   */
  public void destroy() {
    setScope(false);
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.runtime.InterpretableExecution#remove()
   */
  public void remove() {
    ensureParentInitialized();
    if (parent!=null) {
      parent.ensureExecutionsInitialized();
      parent.executions.remove(this);
    }
    
    // remove event scopes:            
    List<InterpretableExecution> childExecutions = new ArrayList<InterpretableExecution>(getExecutions());
    for (InterpretableExecution childExecution : childExecutions) {
      if(childExecution.isEventScope()) {
        log.fine("removing eventScope "+childExecution);
        childExecution.destroy();
        childExecution.remove();
      }
    }
  }
  
  // parent ///////////////////////////////////////////////////////////////////

  /**
   * ensures initialization and returns the parent.
   *
   * @return the parent
   */
  public ExecutionImpl getParent() {
    ensureParentInitialized();
    return parent;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.DelegateExecution#getParentId()
   */
  public String getParentId() {
    ensureActivityInitialized();
    if(parent != null) {
      return parent.getId();
    }
    return null;
  }

  /**
   * all updates need to go through this setter as subclasses can override this method.
   *
   * @param parent the new parent
   */
  public void setParent(InterpretableExecution parent) {
    this.parent = (ExecutionImpl) parent;
  }

  /** must be called before memberfield parent is used. 
   * can be used by subclasses to provide parent member field initialization. */
  protected void ensureParentInitialized() {
  }

  // executions ///////////////////////////////////////////////////////////////  

  /**
   * ensures initialization and returns the non-null executions list.
   *
   * @return the executions
   */
  public List<ExecutionImpl> getExecutions() {
    ensureExecutionsInitialized();
    return executions;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.runtime.InterpretableExecution#getSuperExecution()
   */
  public ExecutionImpl getSuperExecution() {
    ensureSuperExecutionInitialized();
    return superExecution;
  }

  /**
   * Sets the super execution.
   *
   * @param superExecution the new super execution
   */
  public void setSuperExecution(ExecutionImpl superExecution) {
    this.superExecution = superExecution;
    if (superExecution != null) {
      superExecution.setSubProcessInstance(null);
    }
  }
  
  // Meant to be overridden by persistent subclasseses
  /**
   * Ensure super execution initialized.
   */
  protected void ensureSuperExecutionInitialized() {
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.runtime.InterpretableExecution#getSubProcessInstance()
   */
  public ExecutionImpl getSubProcessInstance() {
    ensureSubProcessInstanceInitialized();
    return subProcessInstance;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.runtime.InterpretableExecution#setSubProcessInstance(org.activiti.engine.impl.pvm.runtime.InterpretableExecution)
   */
  public void setSubProcessInstance(InterpretableExecution subProcessInstance) {
    this.subProcessInstance = (ExecutionImpl) subProcessInstance;
  }

  // Meant to be overridden by persistent subclasses
  /**
   * Ensure sub process instance initialized.
   */
  protected void ensureSubProcessInstanceInitialized() {
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.runtime.InterpretableExecution#deleteCascade(java.lang.String)
   */
  public void deleteCascade(String deleteReason) {
    this.deleteReason = deleteReason;
    this.deleteRoot = true;
    performOperation(AtomicOperation.DELETE_CASCADE);
  }
  
  /** removes an execution. if there are nested executions, those will be ended recursively.
   * if there is a parent, this method removes the bidirectional relation 
   * between parent and this execution. */
  public void end() {
    isActive = false;
    isEnded = true;
    performOperation(AtomicOperation.ACTIVITY_END);
  }

  /**
   * searches for an execution positioned in the given activity.
   *
   * @param activityId the activity id
   * @return the execution impl
   */
  public ExecutionImpl findExecution(String activityId) {
    if ( (getActivity()!=null)
         && (getActivity().getId().equals(activityId))
       ) {
      return this;
    }
    for (ExecutionImpl nestedExecution : getExecutions()) {
      ExecutionImpl result = nestedExecution.findExecution(activityId);
      if (result != null) {
        return result;
      }
    }
    return null;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.PvmProcessInstance#findActiveActivityIds()
   */
  public List<String> findActiveActivityIds() {
    List<String> activeActivityIds = new ArrayList<String>();
    collectActiveActivityIds(activeActivityIds);
    return activeActivityIds;
  }

  /**
   * Collect active activity ids.
   *
   * @param activeActivityIds the active activity ids
   */
  protected void collectActiveActivityIds(List<String> activeActivityIds) {
    ensureActivityInitialized();
    if (isActive && activity!=null) {
      activeActivityIds.add(activity.getId());
    }
    ensureExecutionsInitialized();
    for (ExecutionImpl execution: executions) {
      execution.collectActiveActivityIds(activeActivityIds);
    }
  }

  /** must be called before memberfield executions is used. 
   * can be used by subclasses to provide executions member field initialization. */
  protected void ensureExecutionsInitialized() {
    if (executions==null) {
      executions = new ArrayList<ExecutionImpl>();
    }
  }

  // process definition ///////////////////////////////////////////////////////
  
  /**
   * ensures initialization and returns the process definition.
   *
   * @return the process definition
   */
  public ProcessDefinitionImpl getProcessDefinition() {
    ensureProcessDefinitionInitialized();
    return processDefinition;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.DelegateExecution#getProcessDefinitionId()
   */
  public String getProcessDefinitionId() {
    return getProcessDefinition().getId();
  }

  /**
   * for setting the process definition, this setter must be used as subclasses can override.
   */  

  /** must be called before memberfield processDefinition is used. 
   * can be used by subclasses to provide processDefinition member field initialization. */
  protected void ensureProcessDefinitionInitialized() {
  }
  
  // process instance /////////////////////////////////////////////////////////

  /**
   * ensures initialization and returns the process instance.
   *
   * @return the process instance
   */
  public ExecutionImpl getProcessInstance() {
    ensureProcessInstanceInitialized();
    return processInstance;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.DelegateExecution#getProcessInstanceId()
   */
  public String getProcessInstanceId() {
    return getProcessInstance().getId();
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.DelegateExecution#getBusinessKey()
   */
  public String getBusinessKey() {
    return getProcessInstance().getBusinessKey();
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.DelegateExecution#getProcessBusinessKey()
   */
  public String getProcessBusinessKey() {
    return getProcessInstance().getBusinessKey();
  }
  
  /**
   * for setting the process instance, this setter must be used as subclasses can override.
   *
   * @param processInstance the new process instance
   */  
  public void setProcessInstance(InterpretableExecution processInstance) {
    this.processInstance = (ExecutionImpl) processInstance;
  }

  /** must be called before memberfield processInstance is used. 
   * can be used by subclasses to provide processInstance member field initialization. */
  protected void ensureProcessInstanceInitialized() {
  }
  
  // activity /////////////////////////////////////////////////////////////////
  
  /**
   * ensures initialization and returns the activity.
   *
   * @return the activity
   */
  public ActivityImpl getActivity() {
    ensureActivityInitialized();
    return activity;
  }
  
  /**
   * sets the current activity.  can be overridden by subclasses.  doesn't
   * require initialization.
   *
   * @param activity the new activity
   */
  public void setActivity(ActivityImpl activity) {
    this.activity = activity;
  }

  /**
   * must be called before the activity member field or getActivity() is called.
   */
  protected void ensureActivityInitialized() {
  }
  
  // scopes ///////////////////////////////////////////////////////////////////
  
  /**
   * Ensure scope initialized.
   */
  protected void ensureScopeInitialized() {
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.delegate.ActivityExecution#isScope()
   */
  public boolean isScope() {
    return isScope;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.delegate.ActivityExecution#setScope(boolean)
   */
  public void setScope(boolean isScope) {
    this.isScope = isScope;
  }
  
  // process instance start implementation ////////////////////////////////////

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.PvmProcessInstance#start()
   */
  public void start() {
    if(startingExecution == null && isProcessInstance()) {
      startingExecution = new StartingExecution(processDefinition.getInitial());
    }
    performOperation(AtomicOperation.PROCESS_START);
  }
  
  // methods that translate to operations /////////////////////////////////////

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.PvmExecution#signal(java.lang.String, java.lang.Object)
   */
  public void signal(String signalName, Object signalData) {
    ensureActivityInitialized();
    SignallableActivityBehavior activityBehavior = (SignallableActivityBehavior) activity.getActivityBehavior();
    try {
      activityBehavior.signal(this, signalName, signalData);
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new PvmException("couldn't process signal '"+signalName+"' on activity '"+activity.getId()+"': "+e.getMessage(), e);
    }
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.delegate.ActivityExecution#take(org.activiti.engine.impl.pvm.PvmTransition)
   */
  public void take(PvmTransition transition) {
    if (this.transition!=null) {
      throw new PvmException("already taking a transition");
    }
    if (transition==null) {
      throw new PvmException("transition is null");
    }
    setTransition((TransitionImpl) transition);
    performOperation(AtomicOperation.TRANSITION_NOTIFY_LISTENER_END);
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.delegate.ActivityExecution#executeActivity(org.activiti.engine.impl.pvm.PvmActivity)
   */
  public void executeActivity(PvmActivity activity) {
    setActivity((ActivityImpl) activity);
    performOperation(AtomicOperation.ACTIVITY_START);
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.delegate.ActivityExecution#findInactiveConcurrentExecutions(org.activiti.engine.impl.pvm.PvmActivity)
   */
  public List<ActivityExecution> findInactiveConcurrentExecutions(PvmActivity activity) {
    List<ActivityExecution> inactiveConcurrentExecutionsInActivity = new ArrayList<ActivityExecution>();
    List<ActivityExecution> otherConcurrentExecutions = new ArrayList<ActivityExecution>();
    if (isConcurrent()) {
      List< ? extends ActivityExecution> concurrentExecutions = getParent().getExecutions();
      for (ActivityExecution concurrentExecution: concurrentExecutions) {
        if (concurrentExecution.getActivity()==activity) {
          if (concurrentExecution.isActive()) {
            throw new PvmException("didn't expect active execution in "+activity+". bug?");
          }
          inactiveConcurrentExecutionsInActivity.add(concurrentExecution);
        } else {
          otherConcurrentExecutions.add(concurrentExecution);
        }
      }
    } else {
      if (!isActive()) {
        inactiveConcurrentExecutionsInActivity.add(this);
      } else {
        otherConcurrentExecutions.add(this);
      }
    }
    if (log.isLoggable(Level.FINE)) {
      log.fine("inactive concurrent executions in '"+activity+"': "+inactiveConcurrentExecutionsInActivity);
      log.fine("other concurrent executions: "+otherConcurrentExecutions);
    }
    return inactiveConcurrentExecutionsInActivity;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.delegate.ActivityExecution#takeAll(java.util.List, java.util.List)
   */
  @SuppressWarnings("unchecked")
  public void takeAll(List<PvmTransition> transitions, List<ActivityExecution> recyclableExecutions) {
    transitions = new ArrayList<PvmTransition>(transitions);
    recyclableExecutions = (recyclableExecutions!=null ? new ArrayList<ActivityExecution>(recyclableExecutions) : new ArrayList<ActivityExecution>());
    
    if (recyclableExecutions.size()>1) {
      for (ActivityExecution recyclableExecution: recyclableExecutions) {
        if (((ExecutionImpl)recyclableExecution).isScope()) {
          throw new PvmException("joining scope executions is not allowed");
        }
      }
    }

    ExecutionImpl concurrentRoot = ((isConcurrent && !isScope) ? getParent() : this);
    List<ExecutionImpl> concurrentActiveExecutions = new ArrayList<ExecutionImpl>();
    for (ExecutionImpl execution: concurrentRoot.getExecutions()) {
      if (execution.isActive()) {
        concurrentActiveExecutions.add(execution);
      }
    }

    if (log.isLoggable(Level.FINE)) {
      log.fine("transitions to take concurrent: " + transitions);
      log.fine("active concurrent executions: " + concurrentActiveExecutions);
    }

    if ( (transitions.size()==1)
         && (concurrentActiveExecutions.isEmpty())
       ) {

      List<ExecutionImpl> recyclableExecutionImpls = (List) recyclableExecutions;
      for (ExecutionImpl prunedExecution: recyclableExecutionImpls) {
        // End the pruned executions if necessary.
        // Some recyclable executions are inactivated (joined executions)
        // Others are already ended (end activities)
        if (!prunedExecution.isEnded()) {
          log.fine("pruning execution " + prunedExecution);
          prunedExecution.remove();
        }
      }

      log.fine("activating the concurrent root "+concurrentRoot+" as the single path of execution going forward");
      concurrentRoot.setActive(true);
      concurrentRoot.setActivity(activity);
      concurrentRoot.setConcurrent(false);
      concurrentRoot.take(transitions.get(0));

    } else {
      
      List<OutgoingExecution> outgoingExecutions = new ArrayList<OutgoingExecution>();

      recyclableExecutions.remove(concurrentRoot);
  
      log.fine("recyclable executions for reused: " + recyclableExecutions);
      
      // first create the concurrent executions
      while (!transitions.isEmpty()) {
        PvmTransition outgoingTransition = transitions.remove(0);

        ExecutionImpl outgoingExecution = null;
        if (recyclableExecutions.isEmpty()) {
          outgoingExecution = concurrentRoot.createExecution();
          log.fine("new "+outgoingExecution+" created to take transition "+outgoingTransition);
        } else {
          outgoingExecution = (ExecutionImpl) recyclableExecutions.remove(0);
          log.fine("recycled "+outgoingExecution+" to take transition "+outgoingTransition);
        }
        
        outgoingExecution.setActive(true);
        outgoingExecution.setScope(false);
        outgoingExecution.setConcurrent(true);
        outgoingExecutions.add(new OutgoingExecution(outgoingExecution, outgoingTransition, true));
      }

      // prune the executions that are not recycled 
      for (ActivityExecution prunedExecution: recyclableExecutions) {
        log.fine("pruning execution "+prunedExecution);
        prunedExecution.end();
      }

      // then launch all the concurrent executions
      for (OutgoingExecution outgoingExecution: outgoingExecutions) {
        outgoingExecution.take();
      }
    }
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.runtime.InterpretableExecution#performOperation(org.activiti.engine.impl.pvm.runtime.AtomicOperation)
   */
  public void performOperation(AtomicOperation executionOperation) {
    this.nextOperation = executionOperation;
    if (!isOperating) {
      isOperating = true;
      while (nextOperation!=null) {
        AtomicOperation currentOperation = this.nextOperation;
        this.nextOperation = null;
        if (log.isLoggable(Level.FINEST)) {
          log.finest("AtomicOperation: " + currentOperation + " on " + this);
        }
        currentOperation.execute(this);
      }
      isOperating = false;
    }
  }

  
  /**
   * Checks if is active.
   *
   * @param activityId the activity id
   * @return true, if is active
   */
  public boolean isActive(String activityId) {
    return findExecution(activityId)!=null;
  }

  // variables ////////////////////////////////////////////////////////////////

  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.VariableScope#getVariable(java.lang.String)
   */
  public Object getVariable(String variableName) {
    ensureVariablesInitialized();
    
    // If value is found in this scope, return it
    if (variables.containsKey(variableName)) {
      return variables.get(variableName);
    }
    
    // If value not found in this scope, check the parent scope
    ensureParentInitialized();
    if (parent != null) {
      return parent.getVariable(variableName);        
    }
    
    // Variable is nowhere to be found
    return null;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.VariableScope#getVariables()
   */
  public Map<String, Object> getVariables() {
    Map<String, Object> collectedVariables = new HashMap<String, Object>();
    collectVariables(collectedVariables);
    return collectedVariables;
  }
  
  /**
   * Collect variables.
   *
   * @param collectedVariables the collected variables
   */
  protected void collectVariables(Map<String, Object> collectedVariables) {
    ensureParentInitialized();
    if (parent!=null) {
      parent.collectVariables(collectedVariables);
    }
    ensureVariablesInitialized();
    for (String variableName: variables.keySet()) {
      collectedVariables.put(variableName, variables.get(variableName));
    }
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.VariableScope#setVariables(java.util.Map)
   */
  public void setVariables(Map<String, ? extends Object> variables) {
    ensureVariablesInitialized();
    if (variables!=null) {
      for (String variableName: variables.keySet()) {
        setVariable(variableName, variables.get(variableName));
      }
    }
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.VariableScope#setVariable(java.lang.String, java.lang.Object)
   */
  public void setVariable(String variableName, Object value) {
    ensureVariablesInitialized();
    if (variables.containsKey(variableName)) {
      setVariableLocally(variableName, value);
    } else {
      ensureParentInitialized();
      if (parent!=null) {
        parent.setVariable(variableName, value);
      } else {
        setVariableLocally(variableName, value);
      }
    }
  }

  /**
   * Sets the variable locally.
   *
   * @param variableName the variable name
   * @param value the value
   */
  public void setVariableLocally(String variableName, Object value) {
    log.fine("setting variable '"+variableName+"' to value '"+value+"' on "+this);
    variables.put(variableName, value);
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.VariableScope#hasVariable(java.lang.String)
   */
  public boolean hasVariable(String variableName) {
    ensureVariablesInitialized();
    if (variables.containsKey(variableName)) {
      return true;
    }
    ensureParentInitialized();
    if (parent!=null) {
      return parent.hasVariable(variableName);
    }
    return false;
  }

  /**
   * Ensure variables initialized.
   */
  protected void ensureVariablesInitialized() {
    if (variables==null) {
      variables = new HashMap<String, Object>();
    }
  }
  
  // toString /////////////////////////////////////////////////////////////////
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString() {
    if (isProcessInstance()) {
      return "ProcessInstance["+getToStringIdentity()+"]";
    } else {
      return (isEventScope? "EventScope":"")+(isConcurrent? "Concurrent" : "")+(isScope() ? "Scope" : "")+"Execution["+getToStringIdentity()+"]";
    }
  }

  /**
   * Gets the to string identity.
   *
   * @return the to string identity
   */
  protected String getToStringIdentity() {
    return Integer.toString(System.identityHashCode(this));
  }
  
  // customized getters and setters ///////////////////////////////////////////

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.delegate.ActivityExecution#isProcessInstance()
   */
  public boolean isProcessInstance() {
    ensureParentInitialized();
    return parent==null;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.delegate.ActivityExecution#inactivate()
   */
  public void inactivate() {
    this.isActive = false;
  }
  
  // allow for subclasses to expose a real id /////////////////////////////////
  
  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.DelegateExecution#getId()
   */
  public String getId() {
    return null;
  }
  
  // getters and setters //////////////////////////////////////////////////////

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.runtime.InterpretableExecution#getTransition()
   */
  public TransitionImpl getTransition() {
    return transition;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.runtime.InterpretableExecution#setTransition(org.activiti.engine.impl.pvm.process.TransitionImpl)
   */
  public void setTransition(TransitionImpl transition) {
    this.transition = transition;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.runtime.InterpretableExecution#getExecutionListenerIndex()
   */
  public Integer getExecutionListenerIndex() {
    return executionListenerIndex;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.runtime.InterpretableExecution#setExecutionListenerIndex(java.lang.Integer)
   */
  public void setExecutionListenerIndex(Integer executionListenerIndex) {
    this.executionListenerIndex = executionListenerIndex;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.delegate.ActivityExecution#isConcurrent()
   */
  public boolean isConcurrent() {
    return isConcurrent;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.delegate.ActivityExecution#setConcurrent(boolean)
   */
  public void setConcurrent(boolean isConcurrent) {
    this.isConcurrent = isConcurrent;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.delegate.ActivityExecution#isActive()
   */
  public boolean isActive() {
    return isActive;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.delegate.ActivityExecution#setActive(boolean)
   */
  public void setActive(boolean isActive) {
    this.isActive = isActive;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.delegate.ActivityExecution#isEnded()
   */
  public boolean isEnded() {
    return isEnded;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.runtime.InterpretableExecution#setProcessDefinition(org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl)
   */
  public void setProcessDefinition(ProcessDefinitionImpl processDefinition) {
    this.processDefinition = processDefinition;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.DelegateExecution#getEventName()
   */
  public String getEventName() {
    return eventName;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.runtime.InterpretableExecution#setEventName(java.lang.String)
   */
  public void setEventName(String eventName) {
    this.eventName = eventName;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.delegate.ExecutionListenerExecution#getEventSource()
   */
  public PvmProcessElement getEventSource() {
    return eventSource;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.runtime.InterpretableExecution#setEventSource(org.activiti.engine.impl.pvm.PvmProcessElement)
   */
  public void setEventSource(PvmProcessElement eventSource) {
    this.eventSource = eventSource;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.delegate.ExecutionListenerExecution#getDeleteReason()
   */
  public String getDeleteReason() {
    return deleteReason;
  }
  
  /**
   * Sets the delete reason.
   *
   * @param deleteReason the new delete reason
   */
  public void setDeleteReason(String deleteReason) {
    this.deleteReason = deleteReason;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.runtime.InterpretableExecution#getReplacedBy()
   */
  public ExecutionImpl getReplacedBy() {
    return replacedBy;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.runtime.InterpretableExecution#setReplacedBy(org.activiti.engine.impl.pvm.runtime.InterpretableExecution)
   */
  public void setReplacedBy(InterpretableExecution replacedBy) {
    this.replacedBy = (ExecutionImpl) replacedBy;
  }
  
  /**
   * Sets the executions.
   *
   * @param executions the new executions
   */
  public void setExecutions(List<ExecutionImpl> executions) {
    this.executions = executions;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.runtime.InterpretableExecution#isDeleteRoot()
   */
  public boolean isDeleteRoot() {
    return deleteRoot;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.DelegateExecution#getCurrentActivityId()
   */
  public String getCurrentActivityId() {
    String currentActivityId = null;
    if (this.activity != null) {
      currentActivityId = activity.getId();
    }
    return currentActivityId;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.DelegateExecution#getCurrentActivityName()
   */
  public String getCurrentActivityName() {
    String currentActivityName = null;
    if (this.activity != null) {
      currentActivityName = (String) activity.getProperty("name");
    }
    return currentActivityName;
  }


  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.VariableScope#createVariableLocal(java.lang.String, java.lang.Object)
   */
  public void createVariableLocal(String variableName, Object value) {
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.VariableScope#createVariablesLocal(java.util.Map)
   */
  public void createVariablesLocal(Map<String, ? extends Object> variables) {
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.VariableScope#getVariableLocal(java.lang.Object)
   */
  public Object getVariableLocal(Object variableName) {
    return null;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.VariableScope#getVariableNames()
   */
  public Set<String> getVariableNames() {
    return null;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.VariableScope#getVariableNamesLocal()
   */
  public Set<String> getVariableNamesLocal() {
    return null;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.VariableScope#getVariablesLocal()
   */
  public Map<String, Object> getVariablesLocal() {
    return null;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.VariableScope#hasVariableLocal(java.lang.String)
   */
  public boolean hasVariableLocal(String variableName) {
    return false;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.VariableScope#hasVariables()
   */
  public boolean hasVariables() {
    return false;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.VariableScope#hasVariablesLocal()
   */
  public boolean hasVariablesLocal() {
    return false;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.VariableScope#removeVariable(java.lang.String)
   */
  public void removeVariable(String variableName) {
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.VariableScope#removeVariableLocal(java.lang.String)
   */
  public void removeVariableLocal(String variableName) {
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.VariableScope#removeVariables()
   */
  public void removeVariables() {
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.VariableScope#removeVariablesLocal()
   */
  public void removeVariablesLocal() {
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.VariableScope#setVariableLocal(java.lang.String, java.lang.Object)
   */
  public Object setVariableLocal(String variableName, Object value) {
    return null;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.VariableScope#setVariablesLocal(java.util.Map)
   */
  public void setVariablesLocal(Map<String, ? extends Object> variables) {
  }
    
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.runtime.InterpretableExecution#isEventScope()
   */
  public boolean isEventScope() {
    return isEventScope;
  }
    
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.runtime.InterpretableExecution#setEventScope(boolean)
   */
  public void setEventScope(boolean isEventScope) {
    this.isEventScope = isEventScope;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.runtime.InterpretableExecution#getStartingExecution()
   */
  public StartingExecution getStartingExecution() {
    return startingExecution;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.runtime.InterpretableExecution#disposeStartingExecution()
   */
  public void disposeStartingExecution() {
    startingExecution = null;
  }
}
