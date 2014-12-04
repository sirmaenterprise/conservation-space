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
package org.activiti.engine.task;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.query.Query;

// TODO: Auto-generated Javadoc
/**
 * Allows programmatic querying of {@link Task}s;.
 *
 * @author Joram Barrez
 * @author Falko Menge
 */
public interface TaskQuery extends Query<TaskQuery, Task>{

  /**
   * Only select tasks with the given task id (in practice, there will be
   * maximum one of this kind).
   *
   * @param taskId the task id
   * @return the task query
   */
  TaskQuery taskId(String taskId);

  /**
   * Only select tasks with the given name.
   *
   * @param name the name
   * @return the task query
   */
  TaskQuery taskName(String name);
  
  /**
   * Only select tasks with a name matching the parameter.
   * The syntax is that of SQL: for example usage: nameLike(%activiti%)
   *
   * @param nameLike the name like
   * @return the task query
   */
  TaskQuery taskNameLike(String nameLike);
  
  /**
   * Only select tasks with the given description.
   *
   * @param description the description
   * @return the task query
   */
  TaskQuery taskDescription(String description);
  
  /**
   * Only select tasks with a description matching the parameter .
   * The syntax is that of SQL: for example usage: descriptionLike(%activiti%)
   *
   * @param descriptionLike the description like
   * @return the task query
   */
  TaskQuery taskDescriptionLike(String descriptionLike);
  
  /**
   * Only select tasks with the given priority.
   *
   * @param priority the priority
   * @return the task query
   */
  TaskQuery taskPriority(Integer priority);

  /**
   * Only select tasks with the given priority or higher.
   *
   * @param minPriority the min priority
   * @return the task query
   */
  TaskQuery taskMinPriority(Integer minPriority);

  /**
   * Only select tasks with the given priority or lower.
   *
   * @param maxPriority the max priority
   * @return the task query
   */
  TaskQuery taskMaxPriority(Integer maxPriority);

  /**
   * Only select tasks which are assigned to the given user.
   *
   * @param assignee the assignee
   * @return the task query
   */
  TaskQuery taskAssignee(String assignee);
  
  /**
   * Only select tasks for which the given user is the owner.
   *
   * @param owner the owner
   * @return the task query
   */
  TaskQuery taskOwner(String owner);
  
  /**
   * Only select tasks which don't have an assignee.
   *
   * @return the task query
   */
  TaskQuery taskUnassigned();

  /**
   * Task unnassigned.
   *
   * @return the task query
   * @see {@link #taskUnassigned}
   */
  @Deprecated
  TaskQuery taskUnnassigned();

  /**
   * Only select tasks with the given {@link DelegationState}.
   *
   * @param delegationState the delegation state
   * @return the task query
   */
  TaskQuery taskDelegationState(DelegationState delegationState);

  /**
   * Only select tasks for which the given user is a candidate.
   *
   * @param candidateUser the candidate user
   * @return the task query
   */
  TaskQuery taskCandidateUser(String candidateUser);
  
  /**
   * Only select tasks for which there exist an {@link IdentityLink} with the given user.
   *
   * @param involvedUser the involved user
   * @return the task query
   */
  TaskQuery taskInvolvedUser(String involvedUser);

  /**
   * Only select tasks for which users in the given group are candidates.
   *
   * @param candidateGroup the candidate group
   * @return the task query
   */
  TaskQuery taskCandidateGroup(String candidateGroup);
  
  /**
   * Only select tasks for which the 'candidateGroup' is one of the given groups.
   *
   * @param candidateGroups the candidate groups
   * @return the task query
   * {@link #taskCandidateUser(String)} has been executed on the query instance.
   * When passed group list is empty or <code>null</code>.
   */
  TaskQuery taskCandidateGroupIn(List<String> candidateGroups);

  /**
   * Only select tasks for the given process instance id.
   *
   * @param processInstanceId the process instance id
   * @return the task query
   */
  TaskQuery processInstanceId(String processInstanceId);
  
  /**
   * Only select tasks foe the given business key.
   *
   * @param processInstanceBusinessKey the process instance business key
   * @return the task query
   */
  TaskQuery processInstanceBusinessKey(String processInstanceBusinessKey);  

  /**
   * Only select tasks for the given execution.
   *
   * @param executionId the execution id
   * @return the task query
   */
  TaskQuery executionId(String executionId);
  
  /**
   * Only select tasks that are created on the given date. *
   *
   * @param createTime the create time
   * @return the task query
   */
  TaskQuery taskCreatedOn(Date createTime);
  
  /**
   * Only select tasks that are created before the given date. *
   *
   * @param before the before
   * @return the task query
   */
  TaskQuery taskCreatedBefore(Date before);

  /**
   * Only select tasks that are created after the given date. *
   *
   * @param after the after
   * @return the task query
   */
  TaskQuery taskCreatedAfter(Date after);
  
  /**
   * Only select tasks with the given taskDefinitionKey.
   * The task definition key is the id of the userTask:
   * &lt;userTask id="xxx" .../&gt;
   *
   * @param key the key
   * @return the task query
   */
  TaskQuery taskDefinitionKey(String key);
  
  /**
   * Only select tasks with a taskDefinitionKey that match the given parameter.
   * The syntax is that of SQL: for example usage: taskDefinitionKeyLike("%activiti%").
   * The task definition key is the id of the userTask:
   * &lt;userTask id="xxx" .../&gt;
   *
   * @param keyLike the key like
   * @return the task query
   */
  TaskQuery taskDefinitionKeyLike(String keyLike);
  
  /**
   * Only select tasks which have a local task variable with the given name
   * set to the given value.
   *
   * @param variableName the variable name
   * @param variableValue the variable value
   * @return the task query
   */
  TaskQuery taskVariableValueEquals(String variableName, Object variableValue);
  
  /**
   * Only select tasks which have a local task variable with the given name, but
   * with a different value than the passed value.
   * Byte-arrays and {@link Serializable} objects (which are not primitive type wrappers)
   * are not supported.
   *
   * @param variableName the variable name
   * @param variableValue the variable value
   * @return the task query
   */
  TaskQuery taskVariableValueNotEquals(String variableName, Object variableValue);    
  
  /**
   * Only select tasks which have are part of a process that have a variable
   * with the given name set to the given value.
   *
   * @param variableName the variable name
   * @param variableValue the variable value
   * @return the task query
   */
  TaskQuery processVariableValueEquals(String variableName, Object variableValue);
  
  /**
   * Only select tasks which have a variable with the given name, but
   * with a different value than the passed value.
   * Byte-arrays and {@link Serializable} objects (which are not primitive type wrappers)
   * are not supported.
   *
   * @param variableName the variable name
   * @param variableValue the variable value
   * @return the task query
   */
  TaskQuery processVariableValueNotEquals(String variableName, Object variableValue);  
  
  /**
   * Only select tasks which are part of a process instance which has the given
   * process definition key.
   *
   * @param processDefinitionKey the process definition key
   * @return the task query
   */
  TaskQuery processDefinitionKey(String processDefinitionKey);
  
  /**
   * Only select tasks which are part of a process instance which has the given
   * process definition id.
   *
   * @param processDefinitionId the process definition id
   * @return the task query
   */
  TaskQuery processDefinitionId(String processDefinitionId);
  
  /**
   * Only select tasks which are part of a process instance which has the given
   * process definition name.
   *
   * @param processDefinitionName the process definition name
   * @return the task query
   */
  TaskQuery processDefinitionName(String processDefinitionName);
  
  /**
   * Only select tasks with the given due date.
   *
   * @param dueDate the due date
   * @return the task query
   */
  TaskQuery dueDate(Date dueDate);
  
  /**
   * Only select tasks which have a due date before the given date.
   *
   * @param dueDate the due date
   * @return the task query
   */
  TaskQuery dueBefore(Date dueDate);

  /**
   * Only select tasks which have a due date after the given date.
   *
   * @param dueDate the due date
   * @return the task query
   */
  TaskQuery dueAfter(Date dueDate);
  
  // ordering ////////////////////////////////////////////////////////////
  
  /**
   * Order by task id (needs to be followed by {@link #asc()} or {@link #desc()}).
   *
   * @return the task query
   */
  TaskQuery orderByTaskId();
  
  /**
   * Order by task name (needs to be followed by {@link #asc()} or {@link #desc()}).
   *
   * @return the task query
   */
  TaskQuery orderByTaskName();
  
  /**
   * Order by description (needs to be followed by {@link #asc()} or {@link #desc()}).
   *
   * @return the task query
   */
  TaskQuery orderByTaskDescription();
  
  /**
   * Order by priority (needs to be followed by {@link #asc()} or {@link #desc()}).
   *
   * @return the task query
   */
  TaskQuery orderByTaskPriority();
  
  /**
   * Order by assignee (needs to be followed by {@link #asc()} or {@link #desc()}).
   *
   * @return the task query
   */
  TaskQuery orderByTaskAssignee();
  
  /**
   * Order by the time on which the tasks were created (needs to be followed by {@link #asc()} or {@link #desc()}).
   *
   * @return the task query
   */
  TaskQuery orderByTaskCreateTime();
  
  /**
   * Order by process instance id (needs to be followed by {@link #asc()} or {@link #desc()}).
   *
   * @return the task query
   */
  TaskQuery orderByProcessInstanceId();
  
  /**
   * Order by execution id (needs to be followed by {@link #asc()} or {@link #desc()}).
   *
   * @return the task query
   */
  TaskQuery orderByExecutionId();
  
  /**
   * Order by due date (needs to be followed by {@link #asc()} or {@link #desc()}).
   *
   * @return the task query
   */
  TaskQuery orderByDueDate();
}
