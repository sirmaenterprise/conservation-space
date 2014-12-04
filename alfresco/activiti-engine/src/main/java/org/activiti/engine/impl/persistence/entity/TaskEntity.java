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
package org.activiti.engine.impl.persistence.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.db.PersistentObject;
import org.activiti.engine.impl.delegate.TaskListenerInvocation;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.task.TaskDefinition;
import org.activiti.engine.impl.util.ClockUtil;
import org.activiti.engine.task.DelegationState;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.IdentityLinkType;
import org.activiti.engine.task.Task;

// TODO: Auto-generated Javadoc
/**
 * The Class TaskEntity.
 *
 * @author Tom Baeyens
 * @author Joram Barrez
 * @author Falko Menge
 */
public class TaskEntity extends VariableScopeImpl implements Task, DelegateTask, Serializable, PersistentObject {

  /** The Constant DELETE_REASON_COMPLETED. */
  public static final String DELETE_REASON_COMPLETED = "completed";
  
  /** The Constant DELETE_REASON_DELETED. */
  public static final String DELETE_REASON_DELETED = "deleted";

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** The id. */
  protected String id;
  
  /** The revision. */
  protected int revision;

  /** The owner. */
  protected String owner;
  
  /** The assignee. */
  protected String assignee;
  
  /** The delegation state. */
  protected DelegationState delegationState;

  /** The parent task id. */
  protected String parentTaskId;

  /** The name. */
  protected String name;
  
  /** The description. */
  protected String description;
  
  /** The priority. */
  protected int priority = Task.PRIORITY_NORMAL;
  
  /** The create time. */
  protected Date createTime; // The time when the task has been created
  
  /** The due date. */
  protected Date dueDate;

  /** The is identity links initialized. */
  protected boolean isIdentityLinksInitialized = false;
  
  /** The task identity link entities. */
  protected List<IdentityLinkEntity> taskIdentityLinkEntities = new ArrayList<IdentityLinkEntity>();

  /** The execution id. */
  protected String executionId;
  
  /** The execution. */
  protected ExecutionEntity execution;

  /** The process instance id. */
  protected String processInstanceId;
  
  /** The process instance. */
  protected ExecutionEntity processInstance;

  /** The process definition id. */
  protected String processDefinitionId;

  /** The task definition. */
  protected TaskDefinition taskDefinition;
  
  /** The task definition key. */
  protected String taskDefinitionKey;

  /** The is deleted. */
  protected boolean isDeleted;

  /** The event name. */
  protected String eventName;

  /**
   * Instantiates a new task entity.
   */
  public TaskEntity() {
  }

  /**
   * Instantiates a new task entity.
   *
   * @param taskId the task id
   */
  public TaskEntity(String taskId) {
    this.id = taskId;
  }

  /**
   * creates and initializes a new persistent task.
   *
   * @param execution the execution
   * @return the task entity
   */
  public static TaskEntity createAndInsert(ActivityExecution execution) {
    TaskEntity task = create();
    task.insert((ExecutionEntity) execution);
    return task;
  }

  /**
   * Insert.
   *
   * @param execution the execution
   */
  public void insert(ExecutionEntity execution) {
    CommandContext commandContext = Context.getCommandContext();
    DbSqlSession dbSqlSession = commandContext.getDbSqlSession();
    dbSqlSession.insert(this);

    if(execution != null) {
      execution.addTask(this);
    }

    int historyLevel = Context.getProcessEngineConfiguration().getHistoryLevel();
    if (historyLevel>=ProcessEngineConfigurationImpl.HISTORYLEVEL_AUDIT) {
      HistoricTaskInstanceEntity historicTaskInstance = new HistoricTaskInstanceEntity(this, execution);
      dbSqlSession.insert(historicTaskInstance);
    }
  }

  /**
   * new task.  Embedded state and create time will be initialized.
   * But this task still will have to be persisted with
   * TransactionContext
   * .getCurrent()
   * .getPersistenceSession()
   * .insert(task);
   *
   * @return the task entity
   */
  public static TaskEntity create() {
    TaskEntity task = new TaskEntity();
    task.isIdentityLinksInitialized = true;
    task.createTime = ClockUtil.getCurrentTime();
    return task;
  }

  /**
   * Update.
   *
   * @param task the task
   */
  public void update(TaskEntity task) {
    setOwner(task.getOwner());
    setAssignee(task.getAssignee());
    setDelegationState(task.getDelegationState());
    setName(task.getName());
    setDescription(task.getDescription());
    setPriority(task.getPriority());
    setCreateTime(task.getCreateTime());
    setDueDate(task.getDueDate());
    setParentTaskId(task.getParentTaskId());
  }

  /**
   * Complete.
   */
  public void complete() {
    fireEvent(TaskListener.EVENTNAME_COMPLETE);

    Context
      .getCommandContext()
      .getTaskManager()
      .deleteTask(this, TaskEntity.DELETE_REASON_COMPLETED, false);

    if (executionId!=null) {
      ExecutionEntity execution = getExecution();
      execution.removeTask(this);
      execution.signal(null, null);
    }
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.task.Task#delegate(java.lang.String)
   */
  public void delegate(String userId) {
    setDelegationState(DelegationState.PENDING);
    if (getOwner() == null) {
      setOwner(getAssignee());
    }
    setAssignee(userId);
  }

  /**
   * Resolve.
   */
  public void resolve() {
    setDelegationState(DelegationState.RESOLVED);
    setAssignee(this.owner);
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.db.PersistentObject#getPersistentState()
   */
  public Object getPersistentState() {
    Map<String, Object> persistentState = new  HashMap<String, Object>();
    persistentState.put("assignee", this.assignee);
    persistentState.put("owner", this.owner);
    persistentState.put("name", this.name);
    persistentState.put("priority", this.priority);
    if (executionId != null) {
      persistentState.put("executionId", this.executionId);
    }
    if (createTime != null) {
      persistentState.put("createTime", this.createTime);
    }
    if(description != null) {
      persistentState.put("description", this.description);
    }
    if(dueDate != null) {
      persistentState.put("dueDate", this.dueDate);
    }
    if (parentTaskId != null) {
      persistentState.put("parentTaskId", this.parentTaskId);
    }
    if (delegationState != null) {
      persistentState.put("delegationState", this.delegationState);
    }
    return persistentState;
  }

  /**
   * Gets the revision next.
   *
   * @return the revision next
   */
  public int getRevisionNext() {
    return revision+1;
  }

  // variables ////////////////////////////////////////////////////////////////

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.persistence.entity.VariableScopeImpl#getParentVariableScope()
   */
  @Override
  protected VariableScopeImpl getParentVariableScope() {
    if (getExecution()!=null) {
      return execution;
    }
    return null;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.persistence.entity.VariableScopeImpl#initializeVariableInstanceBackPointer(org.activiti.engine.impl.persistence.entity.VariableInstanceEntity)
   */
  @Override
  protected void initializeVariableInstanceBackPointer(VariableInstanceEntity variableInstance) {
    variableInstance.setTaskId(id);
    variableInstance.setExecutionId(executionId);
    variableInstance.setProcessInstanceId(processInstanceId);
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.persistence.entity.VariableScopeImpl#loadVariableInstances()
   */
  @Override
  protected List<VariableInstanceEntity> loadVariableInstances() {
    return Context
      .getCommandContext()
      .getVariableInstanceManager()
      .findVariableInstancesByTaskId(id);
  }

  // execution ////////////////////////////////////////////////////////////////

  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.DelegateTask#getExecution()
   */
  public ExecutionEntity getExecution() {
    if ( (execution==null) && (executionId!=null) ) {
      this.execution = Context
        .getCommandContext()
        .getExecutionManager()
        .findExecutionById(executionId);
    }
    return execution;
  }

  /**
   * Sets the execution.
   *
   * @param execution the new execution
   */
  public void setExecution(DelegateExecution execution) {
    if (execution!=null) {
      this.execution = (ExecutionEntity) execution;
      this.executionId = this.execution.getId();
      this.processInstanceId = this.execution.getProcessInstanceId();
      this.processDefinitionId = this.execution.getProcessDefinitionId();

      int historyLevel = Context.getProcessEngineConfiguration().getHistoryLevel();
      if (historyLevel>=ProcessEngineConfigurationImpl.HISTORYLEVEL_AUDIT) {
        HistoricTaskInstanceEntity historicTaskInstance = Context
          .getCommandContext()
          .getDbSqlSession()
          .selectById(HistoricTaskInstanceEntity.class, id);
        historicTaskInstance.setExecutionId(executionId);
      }

    } else {
      this.execution = null;
      this.executionId = null;
      this.processInstanceId = null;
      this.processDefinitionId = null;

      throw new ActivitiException("huh?");
    }
  }

  // task assignment //////////////////////////////////////////////////////////

  /**
   * Adds the identity link.
   *
   * @param userId the user id
   * @param groupId the group id
   * @param type the type
   * @return the identity link entity
   */
  public IdentityLinkEntity addIdentityLink(String userId, String groupId, String type) {
    IdentityLinkEntity identityLinkEntity = IdentityLinkEntity.createAndInsert();
    getIdentityLinks().add(identityLinkEntity);
    identityLinkEntity.setTask(this);
    identityLinkEntity.setUserId(userId);
    identityLinkEntity.setGroupId(groupId);
    identityLinkEntity.setType(type);
    return identityLinkEntity;
  }

  /**
   * Delete identity link.
   *
   * @param userId the user id
   * @param groupId the group id
   * @param type the type
   */
  public void deleteIdentityLink(String userId, String groupId, String type) {
    List<IdentityLinkEntity> identityLinks = Context
      .getCommandContext()
      .getIdentityLinkManager()
      .findIdentityLinkByTaskUserGroupAndType(id, userId, groupId, type);

    for (IdentityLinkEntity identityLink: identityLinks) {
      Context
        .getCommandContext()
        .getDbSqlSession()
        .delete(IdentityLinkEntity.class, identityLink.getId());
    }
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.DelegateTask#getCandidates()
   */
  public Set<IdentityLink> getCandidates() {
    Set<IdentityLink> potentialOwners = new HashSet<IdentityLink>();
    for (IdentityLinkEntity identityLinkEntity : getIdentityLinks()) {
      if (IdentityLinkType.CANDIDATE.equals(identityLinkEntity.getType())) {
        potentialOwners.add(identityLinkEntity);
      }
    }
    return potentialOwners;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.DelegateTask#addCandidateUser(java.lang.String)
   */
  public void addCandidateUser(String userId) {
    addIdentityLink(userId, null, IdentityLinkType.CANDIDATE);
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.DelegateTask#addCandidateUsers(java.util.Collection)
   */
  public void addCandidateUsers(Collection<String> candidateUsers) {
    for (String candidateUser : candidateUsers) {
      addCandidateUser(candidateUser);
    }
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.DelegateTask#addCandidateGroup(java.lang.String)
   */
  public void addCandidateGroup(String groupId) {
    addIdentityLink(null, groupId, IdentityLinkType.CANDIDATE);
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.DelegateTask#addCandidateGroups(java.util.Collection)
   */
  public void addCandidateGroups(Collection<String> candidateGroups) {
    for (String candidateGroup : candidateGroups) {
      addCandidateGroup(candidateGroup);
    }
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.DelegateTask#addGroupIdentityLink(java.lang.String, java.lang.String)
   */
  public void addGroupIdentityLink(String groupId, String identityLinkType) {
    addIdentityLink(null, groupId, identityLinkType);
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.DelegateTask#addUserIdentityLink(java.lang.String, java.lang.String)
   */
  public void addUserIdentityLink(String userId, String identityLinkType) {
    addIdentityLink(userId, null, identityLinkType);
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.DelegateTask#deleteCandidateGroup(java.lang.String)
   */
  public void deleteCandidateGroup(String groupId) {
    deleteGroupIdentityLink(groupId, IdentityLinkType.CANDIDATE);
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.DelegateTask#deleteCandidateUser(java.lang.String)
   */
  public void deleteCandidateUser(String userId) {
    deleteUserIdentityLink(userId, IdentityLinkType.CANDIDATE);
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.DelegateTask#deleteGroupIdentityLink(java.lang.String, java.lang.String)
   */
  public void deleteGroupIdentityLink(String groupId, String identityLinkType) {
    if (groupId!=null) {
      deleteIdentityLink(null, groupId, identityLinkType);
    }
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.DelegateTask#deleteUserIdentityLink(java.lang.String, java.lang.String)
   */
  public void deleteUserIdentityLink(String userId, String identityLinkType) {
    if (userId!=null) {
      deleteIdentityLink(userId, null, identityLinkType);
    }
  }

  /**
   * Gets the identity links.
   *
   * @return the identity links
   */
  public List<IdentityLinkEntity> getIdentityLinks() {
    if (!isIdentityLinksInitialized) {
      taskIdentityLinkEntities = Context
        .getCommandContext()
        .getIdentityLinkManager()
        .findIdentityLinksByTaskId(id);
      isIdentityLinksInitialized = true;
    }

    return taskIdentityLinkEntities;
  }

  /**
   * Gets the activity instance variables.
   *
   * @return the activity instance variables
   */
  @SuppressWarnings("unchecked")
  public Map<String, Object> getActivityInstanceVariables() {
    if (execution!=null) {
      return execution.getVariables();
    }
    return Collections.EMPTY_MAP;
  }

  /**
   * Sets the execution variables.
   *
   * @param parameters the parameters
   */
  public void setExecutionVariables(Map<String, Object> parameters) {
    if (getExecution()!=null) {
      execution.setVariables(parameters);
    }
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return "Task["+id+"]";
  }

  // special setters //////////////////////////////////////////////////////////

  /* (non-Javadoc)
   * @see org.activiti.engine.task.Task#setName(java.lang.String)
   */
  public void setName(String taskName) {
    this.name = taskName;

    CommandContext commandContext = Context.getCommandContext();
    if (commandContext!=null) {
      commandContext
        .getHistoricTaskInstanceManager()
        .setTaskName(id, taskName);
    }
  }

  /* plain setter for persistence */
  /**
   * Sets the name without cascade.
   *
   * @param taskName the new name without cascade
   */
  public void setNameWithoutCascade(String taskName) {
    this.name = taskName;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.task.Task#setDescription(java.lang.String)
   */
  public void setDescription(String description) {
    this.description = description;

    CommandContext commandContext = Context.getCommandContext();
    if (commandContext!=null) {
      commandContext
        .getHistoricTaskInstanceManager()
        .setTaskDescription(id, description);
    }
  }

  /* plain setter for persistence */
  /**
   * Sets the description without cascade.
   *
   * @param description the new description without cascade
   */
  public void setDescriptionWithoutCascade(String description) {
    this.description = description;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.task.Task#setAssignee(java.lang.String)
   */
  public void setAssignee(String assignee) {
    if (assignee==null && this.assignee==null) {
      return;
    }
    if (assignee!=null && assignee.equals(this.assignee)) {
      return;
    }
    this.assignee = assignee;

    CommandContext commandContext = Context.getCommandContext();
    if (commandContext!=null) {
      commandContext
        .getHistoricTaskInstanceManager()
        .setTaskAssignee(id, assignee);

      // if there is no command context, then it means that the user is calling the
      // setAssignee outside a service method.  E.g. while creating a new task.
      if (commandContext!=null) {
        fireEvent(TaskListener.EVENTNAME_ASSIGNMENT);
      }
    }
  }

  /* plain setter for persistence */
  /**
   * Sets the assignee without cascade.
   *
   * @param assignee the new assignee without cascade
   */
  public void setAssigneeWithoutCascade(String assignee) {
    this.assignee = assignee;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.task.Task#setOwner(java.lang.String)
   */
  public void setOwner(String owner) {
    if (owner==null && this.owner==null) {
      return;
    }
    if (owner!=null && owner.equals(this.owner)) {
      return;
    }
    this.owner = owner;

    CommandContext commandContext = Context.getCommandContext();
    if (commandContext!=null) {
      commandContext
        .getHistoricTaskInstanceManager()
        .setTaskOwner(id, owner);
    }
  }

  /* plain setter for persistence */
  /**
   * Sets the owner without cascade.
   *
   * @param owner the new owner without cascade
   */
  public void setOwnerWithoutCascade(String owner) {
    this.owner = owner;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.task.Task#setDueDate(java.util.Date)
   */
  public void setDueDate(Date dueDate) {
    this.dueDate = dueDate;

    CommandContext commandContext = Context.getCommandContext();
    if (commandContext!=null) {
      commandContext
        .getHistoricTaskInstanceManager()
        .setTaskDueDate(id, dueDate);
    }
  }

  /**
   * Sets the due date without cascade.
   *
   * @param dueDate the new due date without cascade
   */
  public void setDueDateWithoutCascade(Date dueDate) {
    this.dueDate = dueDate;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.task.Task#setPriority(int)
   */
  public void setPriority(int priority) {
    this.priority = priority;

    CommandContext commandContext = Context.getCommandContext();
    if (commandContext!=null) {
      commandContext
        .getHistoricTaskInstanceManager()
        .setTaskPriority(id, priority);
    }
  }

  /**
   * Sets the priority without cascade.
   *
   * @param priority the new priority without cascade
   */
  public void setPriorityWithoutCascade(int priority) {
    this.priority = priority;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.task.Task#setParentTaskId(java.lang.String)
   */
  public void setParentTaskId(String parentTaskId) {
    this.parentTaskId = parentTaskId;

    CommandContext commandContext = Context.getCommandContext();
    if (commandContext!=null) {
      commandContext
        .getHistoricTaskInstanceManager()
        .setTaskParentTaskId(id, parentTaskId);
    }
  }

  /**
   * Sets the parent task id without cascade.
   *
   * @param parentTaskId the new parent task id without cascade
   */
  public void setParentTaskIdWithoutCascade(String parentTaskId) {
    this.parentTaskId = parentTaskId;
  }

  /**
   * Sets the task definition key without cascade.
   *
   * @param taskDefinitionKey the new task definition key without cascade
   */
  public void setTaskDefinitionKeyWithoutCascade(String taskDefinitionKey) {
       this.taskDefinitionKey = taskDefinitionKey;
  }

  /**
   * Fire event.
   *
   * @param taskEventName the task event name
   */
  public void fireEvent(String taskEventName) {
    TaskDefinition taskDefinition = getTaskDefinition();
    if (taskDefinition != null) {
      List<TaskListener> taskEventListeners = getTaskDefinition().getTaskListener(taskEventName);
      if (taskEventListeners != null) {
        for (TaskListener taskListener : taskEventListeners) {
          ExecutionEntity execution = getExecution();
          if (execution != null) {
            setEventName(taskEventName);
          }
          try {
            Context.getProcessEngineConfiguration()
              .getDelegateInterceptor()
              .handleInvocation(new TaskListenerInvocation(taskListener, (DelegateTask)this));
          }catch (Exception e) {
            throw new ActivitiException("Exception while invoking TaskListener: "+e.getMessage(), e);
          }
        }
      }
    }
  }

  // modified getters and setters /////////////////////////////////////////////

  /**
   * Sets the task definition.
   *
   * @param taskDefinition the new task definition
   */
  public void setTaskDefinition(TaskDefinition taskDefinition) {
    this.taskDefinition = taskDefinition;
    this.taskDefinitionKey = taskDefinition.getKey();

    CommandContext commandContext = Context.getCommandContext();
    if(commandContext != null) {
      int historyLevel = Context.getProcessEngineConfiguration().getHistoryLevel();
      if (historyLevel >= ProcessEngineConfigurationImpl.HISTORYLEVEL_AUDIT) {
        HistoricTaskInstanceEntity historicTaskInstance = commandContext.getDbSqlSession().selectById(HistoricTaskInstanceEntity.class, id);
        if (historicTaskInstance!=null) {
          historicTaskInstance.setTaskDefinitionKey(this.taskDefinitionKey);
        }
      }
    }
  }

  /**
   * Gets the task definition.
   *
   * @return the task definition
   */
  public TaskDefinition getTaskDefinition() {
    if (taskDefinition==null && taskDefinitionKey!=null &&processDefinitionId!=null) {
      ProcessDefinitionEntity processDefinition = Context
        .getProcessEngineConfiguration()
        .getDeploymentCache()
        .findDeployedProcessDefinitionById(processDefinitionId);
      taskDefinition = processDefinition.getTaskDefinitions().get(taskDefinitionKey);
    }
    return taskDefinition;
  }

  // getters and setters //////////////////////////////////////////////////////

  /* (non-Javadoc)
   * @see org.activiti.engine.task.Task#getId()
   */
  public String getId() {
    return id;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.db.PersistentObject#setId(java.lang.String)
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Gets the revision.
   *
   * @return the revision
   */
  public int getRevision() {
    return revision;
  }

  /**
   * Sets the revision.
   *
   * @param revision the new revision
   */
  public void setRevision(int revision) {
    this.revision = revision;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.task.Task#getName()
   */
  public String getName() {
    return name;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.task.Task#getDescription()
   */
  public String getDescription() {
    return description;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.task.Task#getDueDate()
   */
  public Date getDueDate() {
    return dueDate;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.task.Task#getPriority()
   */
  public int getPriority() {
    return priority;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.task.Task#getCreateTime()
   */
  public Date getCreateTime() {
    return createTime;
  }

  /**
   * Sets the creates the time.
   *
   * @param createTime the new creates the time
   */
  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.task.Task#getExecutionId()
   */
  public String getExecutionId() {
    return executionId;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.task.Task#getProcessInstanceId()
   */
  public String getProcessInstanceId() {
    return processInstanceId;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.task.Task#getProcessDefinitionId()
   */
  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  /**
   * Sets the process definition id.
   *
   * @param processDefinitionId the new process definition id
   */
  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.task.Task#getAssignee()
   */
  public String getAssignee() {
    return assignee;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.task.Task#getTaskDefinitionKey()
   */
  public String getTaskDefinitionKey() {
    return taskDefinitionKey;
  }

  /**
   * Sets the task definition key.
   *
   * @param taskDefinitionKey the new task definition key
   */
  public void setTaskDefinitionKey(String taskDefinitionKey) {
    this.taskDefinitionKey = taskDefinitionKey;

    CommandContext commandContext = Context.getCommandContext();
    if(commandContext != null) {
      int historyLevel = Context.getProcessEngineConfiguration().getHistoryLevel();
      if (historyLevel >= ProcessEngineConfigurationImpl.HISTORYLEVEL_AUDIT) {
        HistoricTaskInstanceEntity historicTaskInstance = commandContext.getDbSqlSession().selectById(HistoricTaskInstanceEntity.class, id);
        if (historicTaskInstance!=null) {
          historicTaskInstance.setTaskDefinitionKey(this.taskDefinitionKey);
        }
      }
    }
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.DelegateTask#getEventName()
   */
  public String getEventName() {
    return eventName;
  }
  
  /**
   * Sets the event name.
   *
   * @param eventName the new event name
   */
  public void setEventName(String eventName) {
    this.eventName = eventName;
  }
  
  /**
   * Sets the execution id.
   *
   * @param executionId the new execution id
   */
  public void setExecutionId(String executionId) {
    this.executionId = executionId;
  }
  
  /**
   * Gets the process instance.
   *
   * @return the process instance
   */
  public ExecutionEntity getProcessInstance() {
    return processInstance;
  }
  
  /**
   * Sets the process instance.
   *
   * @param processInstance the new process instance
   */
  public void setProcessInstance(ExecutionEntity processInstance) {
    this.processInstance = processInstance;
  }
  
  /**
   * Sets the execution.
   *
   * @param execution the new execution
   */
  public void setExecution(ExecutionEntity execution) {
    this.execution = execution;
  }
  
  /**
   * Sets the process instance id.
   *
   * @param processInstanceId the new process instance id
   */
  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.task.Task#getOwner()
   */
  public String getOwner() {
    return owner;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.task.Task#getDelegationState()
   */
  public DelegationState getDelegationState() {
    return delegationState;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.task.Task#setDelegationState(org.activiti.engine.task.DelegationState)
   */
  public void setDelegationState(DelegationState delegationState) {
    this.delegationState = delegationState;
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
   * Sets the delegation state string.
   *
   * @param delegationStateString the new delegation state string
   */
  public void setDelegationStateString(String delegationStateString) {
    this.delegationState = (delegationStateString!=null ? DelegationState.valueOf(DelegationState.class, delegationStateString) : null);
  }
  
  /**
   * Checks if is deleted.
   *
   * @return true, if is deleted
   */
  public boolean isDeleted() {
    return isDeleted;
  }
  
  /**
   * Sets the deleted.
   *
   * @param isDeleted the new deleted
   */
  public void setDeleted(boolean isDeleted) {
    this.isDeleted = isDeleted;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.task.Task#getParentTaskId()
   */
  public String getParentTaskId() {
    return parentTaskId;
  }
  
  /**
   * Gets the variable instances.
   *
   * @return the variable instances
   */
  public Map<String, VariableInstanceEntity> getVariableInstances() {
    ensureVariableInstancesInitialized();
    return variableInstances;
  }
}
