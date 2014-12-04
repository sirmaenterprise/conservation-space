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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.variable.VariableTypes;


// TODO: Auto-generated Javadoc
/**
 * The Class HistoricTaskInstanceQueryImpl.
 *
 * @author Tom Baeyens
 */
public class HistoricTaskInstanceQueryImpl extends AbstractQuery<HistoricTaskInstanceQuery, HistoricTaskInstance> implements HistoricTaskInstanceQuery {
  
  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;
  
  /** The process definition id. */
  protected String processDefinitionId;
  
  /** The process definition key. */
  protected String processDefinitionKey;
  
  /** The process definition name. */
  protected String processDefinitionName;
  
  /** The process instance id. */
  protected String processInstanceId;
  
  /** The execution id. */
  protected String executionId;
  
  /** The task id. */
  protected String taskId;
  
  /** The task name. */
  protected String taskName;
  
  /** The task name like. */
  protected String taskNameLike;
  
  /** The task parent task id. */
  protected String taskParentTaskId;
  
  /** The task description. */
  protected String taskDescription;
  
  /** The task description like. */
  protected String taskDescriptionLike;
  
  /** The task delete reason. */
  protected String taskDeleteReason;
  
  /** The task delete reason like. */
  protected String taskDeleteReasonLike;
  
  /** The task owner. */
  protected String taskOwner;
  
  /** The task owner like. */
  protected String taskOwnerLike;
  
  /** The task assignee. */
  protected String taskAssignee;
  
  /** The task assignee like. */
  protected String taskAssigneeLike;
  
  /** The task definition key. */
  protected String taskDefinitionKey;
  
  /** The task priority. */
  protected Integer taskPriority;
  
  /** The finished. */
  protected boolean finished;
  
  /** The unfinished. */
  protected boolean unfinished;
  
  /** The process finished. */
  protected boolean processFinished;
  
  /** The process unfinished. */
  protected boolean processUnfinished;
  
  /** The variables. */
  protected List<TaskQueryVariableValue> variables = new ArrayList<TaskQueryVariableValue>();
  
  /** The due date. */
  protected Date dueDate;
  
  /** The due after. */
  protected Date dueAfter;
  
  /** The due before. */
  protected Date dueBefore;

  /**
   * Instantiates a new historic task instance query impl.
   */
  public HistoricTaskInstanceQueryImpl() {
  }

  /**
   * Instantiates a new historic task instance query impl.
   *
   * @param commandExecutor the command executor
   */
  public HistoricTaskInstanceQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.AbstractQuery#executeCount(org.activiti.engine.impl.interceptor.CommandContext)
   */
  @Override
  public long executeCount(CommandContext commandContext) {
    ensureVariablesInitialized();
    checkQueryOk();
    return commandContext
      .getHistoricTaskInstanceManager()
      .findHistoricTaskInstanceCountByQueryCriteria(this);
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.AbstractQuery#executeList(org.activiti.engine.impl.interceptor.CommandContext, org.activiti.engine.impl.Page)
   */
  @Override
  public List<HistoricTaskInstance> executeList(CommandContext commandContext, Page page) {
    ensureVariablesInitialized();
    checkQueryOk();
    return commandContext
      .getHistoricTaskInstanceManager()
      .findHistoricTaskInstancesByQueryCriteria(this, page);
  }


  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricTaskInstanceQuery#processInstanceId(java.lang.String)
   */
  public HistoricTaskInstanceQueryImpl processInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricTaskInstanceQuery#executionId(java.lang.String)
   */
  public HistoricTaskInstanceQueryImpl executionId(String executionId) {
    this.executionId = executionId;
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricTaskInstanceQuery#processDefinitionId(java.lang.String)
   */
  public HistoricTaskInstanceQueryImpl processDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricTaskInstanceQuery#processDefinitionKey(java.lang.String)
   */
  public HistoricTaskInstanceQuery processDefinitionKey(String processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricTaskInstanceQuery#processDefinitionName(java.lang.String)
   */
  public HistoricTaskInstanceQuery processDefinitionName(String processDefinitionName) {
    this.processDefinitionName = processDefinitionName;
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricTaskInstanceQuery#taskId(java.lang.String)
   */
  public HistoricTaskInstanceQuery taskId(String taskId) {
    this.taskId = taskId;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricTaskInstanceQuery#taskName(java.lang.String)
   */
  public HistoricTaskInstanceQueryImpl taskName(String taskName) {
    this.taskName = taskName;
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricTaskInstanceQuery#taskNameLike(java.lang.String)
   */
  public HistoricTaskInstanceQueryImpl taskNameLike(String taskNameLike) {
    this.taskNameLike = taskNameLike;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricTaskInstanceQuery#taskParentTaskId(java.lang.String)
   */
  public HistoricTaskInstanceQuery taskParentTaskId(String parentTaskId) {
    this.taskParentTaskId = parentTaskId;
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricTaskInstanceQuery#taskDescription(java.lang.String)
   */
  public HistoricTaskInstanceQueryImpl taskDescription(String taskDescription) {
    this.taskDescription = taskDescription;
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricTaskInstanceQuery#taskDescriptionLike(java.lang.String)
   */
  public HistoricTaskInstanceQueryImpl taskDescriptionLike(String taskDescriptionLike) {
    this.taskDescriptionLike = taskDescriptionLike;
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricTaskInstanceQuery#taskDeleteReason(java.lang.String)
   */
  public HistoricTaskInstanceQueryImpl taskDeleteReason(String taskDeleteReason) {
    this.taskDeleteReason = taskDeleteReason;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricTaskInstanceQuery#taskDeleteReasonLike(java.lang.String)
   */
  public HistoricTaskInstanceQueryImpl taskDeleteReasonLike(String taskDeleteReasonLike) {
    this.taskDeleteReasonLike = taskDeleteReasonLike;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricTaskInstanceQuery#taskAssignee(java.lang.String)
   */
  public HistoricTaskInstanceQueryImpl taskAssignee(String taskAssignee) {
    this.taskAssignee = taskAssignee;
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricTaskInstanceQuery#taskAssigneeLike(java.lang.String)
   */
  public HistoricTaskInstanceQueryImpl taskAssigneeLike(String taskAssigneeLike) {
    this.taskAssigneeLike = taskAssigneeLike;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricTaskInstanceQuery#taskOwner(java.lang.String)
   */
  public HistoricTaskInstanceQueryImpl taskOwner(String taskOwner) {
    this.taskOwner = taskOwner;
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricTaskInstanceQuery#taskOwnerLike(java.lang.String)
   */
  public HistoricTaskInstanceQueryImpl taskOwnerLike(String taskOwnerLike) {
    this.taskOwnerLike = taskOwnerLike;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricTaskInstanceQuery#finished()
   */
  public HistoricTaskInstanceQueryImpl finished() {
    this.finished = true;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricTaskInstanceQuery#unfinished()
   */
  public HistoricTaskInstanceQueryImpl unfinished() {
    this.unfinished = true;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricTaskInstanceQuery#taskVariableValueEquals(java.lang.String, java.lang.Object)
   */
  public HistoricTaskInstanceQueryImpl taskVariableValueEquals(String variableName, Object variableValue) {
    variables.add(new TaskQueryVariableValue(variableName, variableValue, QueryOperator.EQUALS, true));
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricTaskInstanceQuery#processVariableValueEquals(java.lang.String, java.lang.Object)
   */
  public HistoricTaskInstanceQuery processVariableValueEquals(String variableName, Object variableValue) {
    variables.add(new TaskQueryVariableValue(variableName, variableValue, QueryOperator.EQUALS, false));
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricTaskInstanceQuery#taskDefinitionKey(java.lang.String)
   */
  public HistoricTaskInstanceQuery taskDefinitionKey(String taskDefinitionKey) {
    this.taskDefinitionKey = taskDefinitionKey;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricTaskInstanceQuery#taskPriority(java.lang.Integer)
   */
  public HistoricTaskInstanceQuery taskPriority(Integer taskPriority) {
    this.taskPriority = taskPriority;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricTaskInstanceQuery#processFinished()
   */
  public HistoricTaskInstanceQuery processFinished() {
    this.processFinished = true;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricTaskInstanceQuery#processUnfinished()
   */
  public HistoricTaskInstanceQuery processUnfinished() {
    this.processUnfinished = true;
    return this;
  }
  
  /**
   * Ensure variables initialized.
   */
  protected void ensureVariablesInitialized() {    
    VariableTypes types = Context.getProcessEngineConfiguration().getVariableTypes();
    for(QueryVariableValue var : variables) {
      var.initialize(types);
    }
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricTaskInstanceQuery#taskDueDate(java.util.Date)
   */
  public HistoricTaskInstanceQuery taskDueDate(Date dueDate) {
    this.dueDate = dueDate;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricTaskInstanceQuery#taskDueAfter(java.util.Date)
   */
  public HistoricTaskInstanceQuery taskDueAfter(Date dueAfter) {
    this.dueAfter = dueAfter;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricTaskInstanceQuery#taskDueBefore(java.util.Date)
   */
  public HistoricTaskInstanceQuery taskDueBefore(Date dueBefore) {
    this.dueBefore = dueBefore;
    return this;
  }

  // ordering /////////////////////////////////////////////////////////////////

  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricTaskInstanceQuery#orderByTaskId()
   */
  public HistoricTaskInstanceQueryImpl orderByTaskId() {
    orderBy(HistoricTaskInstanceQueryProperty.HISTORIC_TASK_INSTANCE_ID);
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricTaskInstanceQuery#orderByHistoricActivityInstanceId()
   */
  public HistoricTaskInstanceQueryImpl orderByHistoricActivityInstanceId() {
    orderBy(HistoricTaskInstanceQueryProperty.PROCESS_DEFINITION_ID);
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricTaskInstanceQuery#orderByProcessDefinitionId()
   */
  public HistoricTaskInstanceQueryImpl orderByProcessDefinitionId() {
    orderBy(HistoricTaskInstanceQueryProperty.PROCESS_DEFINITION_ID);
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricTaskInstanceQuery#orderByProcessInstanceId()
   */
  public HistoricTaskInstanceQueryImpl orderByProcessInstanceId() {
    orderBy(HistoricTaskInstanceQueryProperty.PROCESS_INSTANCE_ID);
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricTaskInstanceQuery#orderByExecutionId()
   */
  public HistoricTaskInstanceQueryImpl orderByExecutionId() {
    orderBy(HistoricTaskInstanceQueryProperty.EXECUTION_ID);
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricTaskInstanceQuery#orderByHistoricTaskInstanceDuration()
   */
  public HistoricTaskInstanceQueryImpl orderByHistoricTaskInstanceDuration() {
    orderBy(HistoricTaskInstanceQueryProperty.DURATION);
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricTaskInstanceQuery#orderByHistoricTaskInstanceEndTime()
   */
  public HistoricTaskInstanceQueryImpl orderByHistoricTaskInstanceEndTime() {
    orderBy(HistoricTaskInstanceQueryProperty.END);
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricTaskInstanceQuery#orderByHistoricActivityInstanceStartTime()
   */
  public HistoricTaskInstanceQueryImpl orderByHistoricActivityInstanceStartTime() {
    orderBy(HistoricTaskInstanceQueryProperty.START);
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricTaskInstanceQuery#orderByTaskName()
   */
  public HistoricTaskInstanceQueryImpl orderByTaskName() {
    orderBy(HistoricTaskInstanceQueryProperty.TASK_NAME);
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricTaskInstanceQuery#orderByTaskDescription()
   */
  public HistoricTaskInstanceQueryImpl orderByTaskDescription() {
    orderBy(HistoricTaskInstanceQueryProperty.TASK_DESCRIPTION);
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricTaskInstanceQuery#orderByTaskAssignee()
   */
  public HistoricTaskInstanceQuery orderByTaskAssignee() {
    orderBy(HistoricTaskInstanceQueryProperty.TASK_ASSIGNEE);
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricTaskInstanceQuery#orderByTaskOwner()
   */
  public HistoricTaskInstanceQuery orderByTaskOwner() {
    orderBy(HistoricTaskInstanceQueryProperty.TASK_OWNER);
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricTaskInstanceQuery#orderByDeleteReason()
   */
  public HistoricTaskInstanceQueryImpl orderByDeleteReason() {
    orderBy(HistoricTaskInstanceQueryProperty.DELETE_REASON);
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricTaskInstanceQuery#orderByTaskDefinitionKey()
   */
  public HistoricTaskInstanceQuery orderByTaskDefinitionKey() {
    orderBy(HistoricTaskInstanceQueryProperty.TASK_DEFINITION_KEY);
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricTaskInstanceQuery#orderByTaskPriority()
   */
  public HistoricTaskInstanceQuery orderByTaskPriority() {
    orderBy(HistoricTaskInstanceQueryProperty.TASK_PRIORITY);
    return this;
  }

  // getters and setters //////////////////////////////////////////////////////
  
  /**
   * Gets the process instance id.
   *
   * @return the process instance id
   */
  public String getProcessInstanceId() {
    return processInstanceId;
  }
  
  /**
   * Gets the execution id.
   *
   * @return the execution id
   */
  public String getExecutionId() {
    return executionId;
  }
  
  /**
   * Gets the process definition id.
   *
   * @return the process definition id
   */
  public String getProcessDefinitionId() {
    return processDefinitionId;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.AbstractQuery#getOrderBy()
   */
  public String getOrderBy() {
    return orderBy;
  }
  
  /**
   * Checks if is finished.
   *
   * @return true, if is finished
   */
  public boolean isFinished() {
    return finished;
  }
  
  /**
   * Checks if is unfinished.
   *
   * @return true, if is unfinished
   */
  public boolean isUnfinished() {
    return unfinished;
  }
  
  /**
   * Gets the task name.
   *
   * @return the task name
   */
  public String getTaskName() {
    return taskName;
  }
  
  /**
   * Gets the task name like.
   *
   * @return the task name like
   */
  public String getTaskNameLike() {
    return taskNameLike;
  }
  
  /**
   * Gets the task description.
   *
   * @return the task description
   */
  public String getTaskDescription() {
    return taskDescription;
  }
  
  /**
   * Gets the task description like.
   *
   * @return the task description like
   */
  public String getTaskDescriptionLike() {
    return taskDescriptionLike;
  }
  
  /**
   * Gets the task delete reason.
   *
   * @return the task delete reason
   */
  public String getTaskDeleteReason() {
    return taskDeleteReason;
  }
  
  /**
   * Gets the task delete reason like.
   *
   * @return the task delete reason like
   */
  public String getTaskDeleteReasonLike() {
    return taskDeleteReasonLike;
  }
  
  /**
   * Gets the task assignee.
   *
   * @return the task assignee
   */
  public String getTaskAssignee() {
    return taskAssignee;
  }
  
  /**
   * Gets the task assignee like.
   *
   * @return the task assignee like
   */
  public String getTaskAssigneeLike() {
    return taskAssigneeLike;
  }
  
  /**
   * Gets the task id.
   *
   * @return the task id
   */
  public String getTaskId() {
    return taskId;
  }
  
  /**
   * Gets the task definition key.
   *
   * @return the task definition key
   */
  public String getTaskDefinitionKey() {
    return taskDefinitionKey;
  }
  
  /**
   * Gets the variables.
   *
   * @return the variables
   */
  public List<TaskQueryVariableValue> getVariables() {
    return variables;
  }
  
  /**
   * Gets the task owner like.
   *
   * @return the task owner like
   */
  public String getTaskOwnerLike() {
    return taskOwnerLike;
  }
  
  /**
   * Gets the task owner.
   *
   * @return the task owner
   */
  public String getTaskOwner() {
    return taskOwner;
  }
  
  /**
   * Gets the task parent task id.
   *
   * @return the task parent task id
   */
  public String getTaskParentTaskId() {
    return taskParentTaskId;
  }
}
