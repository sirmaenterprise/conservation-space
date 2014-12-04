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
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.identity.Group;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.variable.VariableTypes;
import org.activiti.engine.task.DelegationState;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;

// TODO: Auto-generated Javadoc
/**
 * The Class TaskQueryImpl.
 *
 * @author Joram Barrez
 * @author Tom Baeyens
 * @author Falko Menge
 */
public class TaskQueryImpl extends AbstractQuery<TaskQuery, Task> implements TaskQuery {
  
  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;
  
  /** The task id. */
  protected String taskId;
  
  /** The name. */
  protected String name;
  
  /** The name like. */
  protected String nameLike;
  
  /** The description. */
  protected String description;
  
  /** The description like. */
  protected String descriptionLike;
  
  /** The priority. */
  protected Integer priority;
  
  /** The min priority. */
  protected Integer minPriority;
  
  /** The max priority. */
  protected Integer maxPriority;
  
  /** The assignee. */
  protected String assignee;
  
  /** The involved user. */
  protected String involvedUser;
  
  /** The owner. */
  protected String owner;
  
  /** The unassigned. */
  protected boolean unassigned = false;
  
  /** The no delegation state. */
  protected boolean noDelegationState = false;
  
  /** The delegation state. */
  protected DelegationState delegationState;
  
  /** The candidate user. */
  protected String candidateUser;
  
  /** The candidate group. */
  protected String candidateGroup;
  
  /** The candidate groups. */
  private List<String> candidateGroups;
  
  /** The process instance id. */
  protected String processInstanceId;
  
  /** The execution id. */
  protected String executionId;
  
  /** The create time. */
  protected Date createTime;
  
  /** The create time before. */
  protected Date createTimeBefore;
  
  /** The create time after. */
  protected Date createTimeAfter;
  
  /** The key. */
  protected String key;
  
  /** The key like. */
  protected String keyLike;
  
  /** The process definition key. */
  protected String processDefinitionKey;
  
  /** The process definition id. */
  protected String processDefinitionId;
  
  /** The process definition name. */
  protected String processDefinitionName;
  
  /** The process instance business key. */
  protected String processInstanceBusinessKey;
  
  /** The variables. */
  protected List<TaskQueryVariableValue> variables = new ArrayList<TaskQueryVariableValue>();
  
  /** The due date. */
  protected Date dueDate;
  
  /** The due before. */
  protected Date dueBefore;
  
  /** The due after. */
  protected Date dueAfter;
  
  /**
   * Instantiates a new task query impl.
   */
  public TaskQueryImpl() {
  }
  
  /**
   * Instantiates a new task query impl.
   *
   * @param commandContext the command context
   */
  public TaskQueryImpl(CommandContext commandContext) {
    super(commandContext);
  }
  
  /**
   * Instantiates a new task query impl.
   *
   * @param commandExecutor the command executor
   */
  public TaskQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.task.TaskQuery#taskId(java.lang.String)
   */
  public TaskQueryImpl taskId(String taskId) {
    if (taskId == null) {
      throw new ActivitiException("Task id is null");
    }
    this.taskId = taskId;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.task.TaskQuery#taskName(java.lang.String)
   */
  public TaskQueryImpl taskName(String name) {
    this.name = name;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.task.TaskQuery#taskNameLike(java.lang.String)
   */
  public TaskQueryImpl taskNameLike(String nameLike) {
    if (nameLike == null) {
      throw new ActivitiException("Task namelike is null");
    }
    this.nameLike = nameLike;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.task.TaskQuery#taskDescription(java.lang.String)
   */
  public TaskQueryImpl taskDescription(String description) {
    if (description == null) {
      throw new ActivitiException("Description is null");
    }
    this.description = description;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.task.TaskQuery#taskDescriptionLike(java.lang.String)
   */
  public TaskQuery taskDescriptionLike(String descriptionLike) {
    if (descriptionLike == null) {
      throw new ActivitiException("Task descriptionlike is null");
    }
    this.descriptionLike = descriptionLike;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.task.TaskQuery#taskPriority(java.lang.Integer)
   */
  public TaskQuery taskPriority(Integer priority) {
    if (priority == null) {
      throw new ActivitiException("Priority is null");
    }
    this.priority = priority;
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.task.TaskQuery#taskMinPriority(java.lang.Integer)
   */
  public TaskQuery taskMinPriority(Integer minPriority) {
    if (minPriority == null) {
      throw new ActivitiException("Min Priority is null");
    }
    this.minPriority = minPriority;
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.task.TaskQuery#taskMaxPriority(java.lang.Integer)
   */
  public TaskQuery taskMaxPriority(Integer maxPriority) {
    if (maxPriority == null) {
      throw new ActivitiException("Max Priority is null");
    }
    this.maxPriority = maxPriority;
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.task.TaskQuery#taskAssignee(java.lang.String)
   */
  public TaskQueryImpl taskAssignee(String assignee) {
    if (assignee == null) {
      throw new ActivitiException("Assignee is null");
    }
    this.assignee = assignee;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.task.TaskQuery#taskOwner(java.lang.String)
   */
  public TaskQueryImpl taskOwner(String owner) {
    if (owner == null) {
      throw new ActivitiException("Owner is null");
    }
    this.owner = owner;
    return this;
  }
  
  /**
   * Task unnassigned.
   *
   * @return the task query
   * @see {@link #taskUnassigned}
   */
  @Deprecated
  public TaskQuery taskUnnassigned() {
    return taskUnassigned();
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.task.TaskQuery#taskUnassigned()
   */
  public TaskQuery taskUnassigned() {
    this.unassigned = true;
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.task.TaskQuery#taskDelegationState(org.activiti.engine.task.DelegationState)
   */
  public TaskQuery taskDelegationState(DelegationState delegationState) {
    if (delegationState == null) {
      this.noDelegationState = true;
    } else {
      this.delegationState = delegationState;
    }
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.task.TaskQuery#taskCandidateUser(java.lang.String)
   */
  public TaskQueryImpl taskCandidateUser(String candidateUser) {
    if (candidateUser == null) {
      throw new ActivitiException("Candidate user is null");
    }
    if (candidateGroup != null) {
      throw new ActivitiException("Invalid query usage: cannot set both candidateUser and candidateGroup");
    }
    if (candidateGroups != null) {
      throw new ActivitiException("Invalid query usage: cannot set both candidateUser and candidateGroupIn");
    }
    this.candidateUser = candidateUser;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.task.TaskQuery#taskInvolvedUser(java.lang.String)
   */
  public TaskQueryImpl taskInvolvedUser(String involvedUser) {
    if (involvedUser == null) {
      throw new ActivitiException("Involved user is null");
    }
    this.involvedUser = involvedUser;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.task.TaskQuery#taskCandidateGroup(java.lang.String)
   */
  public TaskQueryImpl taskCandidateGroup(String candidateGroup) {
    if (candidateGroup == null) {
      throw new ActivitiException("Candidate group is null");
    }
    if (candidateUser != null) {
      throw new ActivitiException("Invalid query usage: cannot set both candidateGroup and candidateUser");
    }
    if (candidateGroups != null) {
      throw new ActivitiException("Invalid query usage: cannot set both candidateGroup and candidateGroupIn");
    }
    this.candidateGroup = candidateGroup;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.task.TaskQuery#taskCandidateGroupIn(java.util.List)
   */
  public TaskQuery taskCandidateGroupIn(List<String> candidateGroups) {
    if(candidateGroups == null) {
      throw new ActivitiException("Candidate group list is null");
    }
    if(candidateGroups.size()== 0) {
      throw new ActivitiException("Candidate group list is empty");
    }
    
    if (candidateUser != null) {
      throw new ActivitiException("Invalid query usage: cannot set both candidateGroupIn and candidateUser");
    }
    if (candidateGroup != null) {
      throw new ActivitiException("Invalid query usage: cannot set both candidateGroupIn and candidateGroup");
    }
    
    this.candidateGroups = candidateGroups;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.task.TaskQuery#processInstanceId(java.lang.String)
   */
  public TaskQueryImpl processInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.task.TaskQuery#processInstanceBusinessKey(java.lang.String)
   */
  public TaskQueryImpl processInstanceBusinessKey(String processInstanceBusinessKey) {
    this.processInstanceBusinessKey = processInstanceBusinessKey;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.task.TaskQuery#executionId(java.lang.String)
   */
  public TaskQueryImpl executionId(String executionId) {
    this.executionId = executionId;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.task.TaskQuery#taskCreatedOn(java.util.Date)
   */
  public TaskQueryImpl taskCreatedOn(Date createTime) {
    this.createTime = createTime;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.task.TaskQuery#taskCreatedBefore(java.util.Date)
   */
  public TaskQuery taskCreatedBefore(Date before) {
    this.createTimeBefore = before;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.task.TaskQuery#taskCreatedAfter(java.util.Date)
   */
  public TaskQuery taskCreatedAfter(Date after) {
    this.createTimeAfter = after;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.task.TaskQuery#taskDefinitionKey(java.lang.String)
   */
  public TaskQuery taskDefinitionKey(String key) {
    this.key = key;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.task.TaskQuery#taskDefinitionKeyLike(java.lang.String)
   */
  public TaskQuery taskDefinitionKeyLike(String keyLike) {
    this.keyLike = keyLike;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.task.TaskQuery#taskVariableValueEquals(java.lang.String, java.lang.Object)
   */
  public TaskQuery taskVariableValueEquals(String variableName, Object variableValue) {
    variables.add(new TaskQueryVariableValue(variableName, variableValue, QueryOperator.EQUALS, true));
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.task.TaskQuery#taskVariableValueNotEquals(java.lang.String, java.lang.Object)
   */
  public TaskQuery taskVariableValueNotEquals(String variableName, Object variableValue) {
    variables.add(new TaskQueryVariableValue(variableName, variableValue, QueryOperator.NOT_EQUALS, true));
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.task.TaskQuery#processVariableValueEquals(java.lang.String, java.lang.Object)
   */
  public TaskQuery processVariableValueEquals(String variableName, Object variableValue) {
    variables.add(new TaskQueryVariableValue(variableName, variableValue, QueryOperator.EQUALS, false));
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.task.TaskQuery#processVariableValueNotEquals(java.lang.String, java.lang.Object)
   */
  public TaskQuery processVariableValueNotEquals(String variableName, Object variableValue) {
    variables.add(new TaskQueryVariableValue(variableName, variableValue, QueryOperator.NOT_EQUALS, false));
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.task.TaskQuery#processDefinitionKey(java.lang.String)
   */
  public TaskQuery processDefinitionKey(String processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.task.TaskQuery#processDefinitionId(java.lang.String)
   */
  public TaskQuery processDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.task.TaskQuery#processDefinitionName(java.lang.String)
   */
  public TaskQuery processDefinitionName(String processDefinitionName) {
    this.processDefinitionName = processDefinitionName;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.task.TaskQuery#dueDate(java.util.Date)
   */
  public TaskQuery dueDate(Date dueDate) {
    this.dueDate = dueDate;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.task.TaskQuery#dueBefore(java.util.Date)
   */
  public TaskQuery dueBefore(Date dueBefore) {
    this.dueBefore = dueBefore;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.task.TaskQuery#dueAfter(java.util.Date)
   */
  public TaskQuery dueAfter(Date dueAfter) {
    this.dueAfter = dueAfter;
    return this;
  }
  
  /**
   * Gets the candidate groups.
   *
   * @return the candidate groups
   */
  public List<String> getCandidateGroups() {
    if (candidateGroup!=null) {
      return Collections.singletonList(candidateGroup);
    } else if (candidateUser != null) {
      return getGroupsForCandidateUser(candidateUser);
    } else if(candidateGroups != null) {
      return candidateGroups;
    }
    return null;
  }
  
  /**
   * Gets the groups for candidate user.
   *
   * @param candidateUser the candidate user
   * @return the groups for candidate user
   */
  protected List<String> getGroupsForCandidateUser(String candidateUser) {
    // TODO: Discuss about removing this feature? Or document it properly and maybe recommend to not use it
    // and explain alternatives
    List<Group> groups = Context
      .getCommandContext()
      .getGroupManager()
      .findGroupsByUser(candidateUser);
    List<String> groupIds = new ArrayList<String>();
    for (Group group : groups) {
      groupIds.add(group.getId());
    }
    return groupIds;
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

  //ordering ////////////////////////////////////////////////////////////////
  
  /* (non-Javadoc)
   * @see org.activiti.engine.task.TaskQuery#orderByTaskId()
   */
  public TaskQuery orderByTaskId() {
    return orderBy(TaskQueryProperty.TASK_ID);
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.task.TaskQuery#orderByTaskName()
   */
  public TaskQuery orderByTaskName() {
    return orderBy(TaskQueryProperty.NAME);
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.task.TaskQuery#orderByTaskDescription()
   */
  public TaskQuery orderByTaskDescription() {
    return orderBy(TaskQueryProperty.DESCRIPTION);
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.task.TaskQuery#orderByTaskPriority()
   */
  public TaskQuery orderByTaskPriority() {
    return orderBy(TaskQueryProperty.PRIORITY);
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.task.TaskQuery#orderByProcessInstanceId()
   */
  public TaskQuery orderByProcessInstanceId() {
    return orderBy(TaskQueryProperty.PROCESS_INSTANCE_ID);
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.task.TaskQuery#orderByExecutionId()
   */
  public TaskQuery orderByExecutionId() {
    return orderBy(TaskQueryProperty.EXECUTION_ID);
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.task.TaskQuery#orderByTaskAssignee()
   */
  public TaskQuery orderByTaskAssignee() {
    return orderBy(TaskQueryProperty.ASSIGNEE);
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.task.TaskQuery#orderByTaskCreateTime()
   */
  public TaskQuery orderByTaskCreateTime() {
    return orderBy(TaskQueryProperty.CREATE_TIME);
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.task.TaskQuery#orderByDueDate()
   */
  public TaskQuery orderByDueDate() {
    return orderBy(TaskQueryProperty.DUE_DATE);
  }
  
  //results ////////////////////////////////////////////////////////////////
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.AbstractQuery#executeList(org.activiti.engine.impl.interceptor.CommandContext, org.activiti.engine.impl.Page)
   */
  public List<Task> executeList(CommandContext commandContext, Page page) {
    ensureVariablesInitialized();
    checkQueryOk();
    return commandContext
      .getTaskManager()
      .findTasksByQueryCriteria(this);
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.AbstractQuery#executeCount(org.activiti.engine.impl.interceptor.CommandContext)
   */
  public long executeCount(CommandContext commandContext) {
    ensureVariablesInitialized();
    checkQueryOk();
    return commandContext
      .getTaskManager()
      .findTaskCountByQueryCriteria(this);
  }
  
  //getters ////////////////////////////////////////////////////////////////

  /**
   * Gets the name.
   *
   * @return the name
   */
  public String getName() {
    return name;
  }
  
  /**
   * Gets the name like.
   *
   * @return the name like
   */
  public String getNameLike() {
    return nameLike;
  }
  
  /**
   * Gets the assignee.
   *
   * @return the assignee
   */
  public String getAssignee() {
    return assignee;
  }
  
  /**
   * Gets the unassigned.
   *
   * @return the unassigned
   */
  public boolean getUnassigned() {
    return unassigned;
  }
  
  /**
   * Gets the delegation state.
   *
   * @return the delegation state
   */
  public DelegationState getDelegationState() {
    return delegationState;
  }
  
  /**
   * Gets the no delegation state.
   *
   * @return the no delegation state
   */
  public boolean getNoDelegationState() {
    return noDelegationState;
  }
  
  /**
   * Gets the delegation state string.
   *
   * @return the delegation state string
   */
  public String getDelegationStateString() {
    return (delegationState!=null ? delegationState.toString() : null);
  }
  
  /**
   * Gets the candidate user.
   *
   * @return the candidate user
   */
  public String getCandidateUser() {
    return candidateUser;
  }
  
  /**
   * Gets the candidate group.
   *
   * @return the candidate group
   */
  public String getCandidateGroup() {
    return candidateGroup;
  }
  
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
   * Gets the task id.
   *
   * @return the task id
   */
  public String getTaskId() {
    return taskId;
  }
  
  /**
   * Gets the description.
   *
   * @return the description
   */
  public String getDescription() {
    return description;
  }
  
  /**
   * Gets the description like.
   *
   * @return the description like
   */
  public String getDescriptionLike() {
    return descriptionLike;
  }
  
  /**
   * Gets the priority.
   *
   * @return the priority
   */
  public Integer getPriority() {
    return priority;
  }
  
  /**
   * Gets the creates the time.
   *
   * @return the creates the time
   */
  public Date getCreateTime() {
    return createTime;
  }
  
  /**
   * Gets the creates the time before.
   *
   * @return the creates the time before
   */
  public Date getCreateTimeBefore() {
    return createTimeBefore;
  }
  
  /**
   * Gets the creates the time after.
   *
   * @return the creates the time after
   */
  public Date getCreateTimeAfter() {
    return createTimeAfter;
  }
  
  /**
   * Gets the key.
   *
   * @return the key
   */
  public String getKey() {
    return key;
  }
  
  /**
   * Gets the key like.
   *
   * @return the key like
   */
  public String getKeyLike() {
    return keyLike;
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
   * Gets the process definition key.
   *
   * @return the process definition key
   */
  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }
  
  /**
   * Gets the process definition id.
   *
   * @return the process definition id
   */
  public String getProcessDefinitionId() {
    return processDefinitionId;
  }
  
  /**
   * Gets the process definition name.
   *
   * @return the process definition name
   */
  public String getProcessDefinitionName() {
    return processDefinitionName;
  }

  
  /**
   * Gets the process instance business key.
   *
   * @return the process instance business key
   */
  public String getProcessInstanceBusinessKey() {
    return processInstanceBusinessKey;
  }
}
